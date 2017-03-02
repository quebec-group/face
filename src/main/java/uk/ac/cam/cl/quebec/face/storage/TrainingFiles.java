package uk.ac.cam.cl.quebec.face.storage;

import org.opencv.core.Mat;
import org.opencv.face.Face;
import org.opencv.face.LBPHFaceRecognizer;
import uk.ac.cam.cl.quebec.face.config.Config;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
import uk.ac.cam.cl.quebec.face.exceptions.StorageException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * High level manager for user training files. Delegates work to DirectoryStructure and OpenCvYaml
 */
public class TrainingFiles {
    public static void save(LBPHFaceRecognizer recognizer, Config config, String userId) throws QuebecException {
        // Ensure we are saving a training file for a single image
        List<Mat> histograms = recognizer.getHistograms();
        if (histograms.size() != 1) {
            throw new IllegalArgumentException("Can only save a training file with exactly 1 histogram");
        }

        // Create a temporary file for OpenCV to save to
        File tempDirectory = DirectoryStructure.getOrMakeTempDirectory(config);
        File tempFile;
        try {
            tempFile = File.createTempFile("training", ".yaml", tempDirectory);
        }
        catch (IOException e) {
            throw new StorageException("Could not create temporary file to save training file.");
        }

        // Save histogram to the temporary file
        recognizer.save(tempFile.getAbsolutePath());

        // Create a file to store the actual histogram in
        RecogniserConfigSlug slug = new RecogniserConfigSlug(recognizer);
        File userDirectory = DirectoryStructure.getOrMakeUserDirectory(config, userId, slug);
        File histogramFile;
        try {
            histogramFile = File.createTempFile("hist", ".yaml", userDirectory);
        }
        catch (IOException e) {
            throw new StorageException("Could not create file in directory "
                    + userDirectory.getAbsolutePath()
                    + " to store training histogram in.");
        }

        // Copy just the histogram data from temporary file into the user directory
        OpenCvYaml.copyHistogramToUserFile(tempFile, histogramFile);

        // Delete temporary file - we're done with it
        tempFile.delete();
    }

    public static LBPHFaceRecognizer createRecogniserForUsers(Config config, Map<Integer, String> userMappings) throws QuebecException {
        Set<String> possibleSlugs = null;
        // Don't initialise just yet in case we fail
        LBPHFaceRecognizer recognizer;

        List<Integer> toRemove = new ArrayList<>();
        for (Map.Entry<Integer, String> user : userMappings.entrySet()) {
            Set<String> userSlugs = DirectoryStructure.findAllConfigSlugsForUser(config, user.getValue());

            if (userSlugs.isEmpty()) {
                toRemove.add(user.getKey());
                continue;
            }

            if (possibleSlugs == null) {
                possibleSlugs = userSlugs;
            }
            else {
                // take intersection
                possibleSlugs.retainAll(userSlugs);
            }
        }
        // Remove users for which we have no data
        toRemove.forEach(userMappings::remove);

        if (possibleSlugs == null) {
            throw new StorageException("No users were provided to look for");
        }
        Optional<String> optionalChosenSlug = possibleSlugs.stream().max(Comparator.naturalOrder());
        if (!optionalChosenSlug.isPresent()) {
            throw new StorageException("Cannot find common recogniser settings for all users");
        }
        RecogniserConfigSlug chosenSlug = new RecogniserConfigSlug(optionalChosenSlug.get());

        // Create a temporary file for training by concatenation
        File tempDir = DirectoryStructure.getOrMakeTempDirectory(config);
        try {
            File trainingFile = File.createTempFile("train", ".yaml", tempDir);
            System.err.println(trainingFile.getAbsolutePath());
            FileWriter output = new FileWriter(trainingFile);

            // Write headers
            OpenCvYaml.setupTrainingHeaders(output, chosenSlug);

            List<Integer> labels = new LinkedList<>();
            for (Map.Entry<Integer, String> e : userMappings.entrySet()) {
                File userDir = DirectoryStructure.getOrMakeUserDirectory(config, e.getValue(), chosenSlug);
                String userDirPath = userDir.getAbsolutePath();
                String[] userFiles = userDir.list();
                if (userFiles == null) {
                    throw new StorageException("Unable to read user directory " + userDirPath);
                }

                // Copy histograms in
                List<String> fullPaths = Arrays.stream(userFiles)
                        .map(p -> userDirPath + "/" + p)
                        .collect(Collectors.toList());
                concatenateFiles(fullPaths, output);

                // Sort out labels
                labels.addAll(Collections.nCopies(fullPaths.size(), e.getKey()));
            }

            // Write labels
            OpenCvYaml.outputTrainingLabels(output, labels);
            output.close();

            // Use the training file!
            recognizer = Face.createLBPHFaceRecognizer();
            recognizer.load(trainingFile.getAbsolutePath());

            // We're done with that temp file now
            trainingFile.delete();
        }
        catch (IOException e) {
            throw new StorageException("Cannot create temporary file for training");
        }

        return recognizer;
    }

    private static void concatenateFiles(List<String> inputs, FileWriter output) throws StorageException {
        try {
            for (String name : inputs) {
                Reader reader = Files.newBufferedReader(Paths.get(name));
                transfer(reader, output);
            }
        }
        catch (IOException e) {
            throw new StorageException("Error copying data to temporary file for recognition.");
        }
    }

    private static void transfer(final Reader source, final Writer destination) throws IOException {
        char[] buffer = new char[1024 * 16];
        int len = 0;
        while ((len = source.read(buffer)) >= 0) {
            destination.write(buffer, 0, len);
        }
    }
}
