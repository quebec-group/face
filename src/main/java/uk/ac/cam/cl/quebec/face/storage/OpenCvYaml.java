package uk.ac.cam.cl.quebec.face.storage;

import uk.ac.cam.cl.quebec.face.exceptions.StorageException;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to alter OpenCV's save files for FaceRecognisers.
 * This is very tightly coupled to how OpenCV saves things,
 * so is in a class to itself.
 * ---Any version change to OpenCV could break this.---
 */
class OpenCvYaml {
    static void copyHistogramToUserFile(File saveFile, File userFile) throws StorageException {
        try {
            try (BufferedReader input = new BufferedReader(new FileReader(saveFile))) {
                try (BufferedWriter output = new BufferedWriter(new FileWriter(userFile))) {
                    // The first 6 lines are header information, which we don't care about here
                    for (int i = 0; i < 6; i++) {
                        input.readLine();
                    }

                    // The next line should read "histograms:"
                    if (!input.readLine().equals("histograms:")) {
                        throw new StorageException("OpenCV save file format is wrong.");
                    }

                    // Now copy every line verbatim, until we see the line "labels: !!opencv-matrix"
                    for (String line = input.readLine(); !line.equals("labels: !!opencv-matrix"); line = input.readLine()) {
                        output.write(line);
                        output.newLine();
                    }

                    // Now we're done with the files. They'll be closed by the try-with blocks.
                }
            }
        }
        catch (IOException e) {
            throw new StorageException("Error copying data between " +
                    saveFile.getAbsolutePath() + " and " + userFile.getAbsolutePath());
        }
    }

    static void setupTrainingHeaders(Writer output, RecogniserConfigSlug slug)
            throws StorageException {
        String[] lines = {
                "%YAML:1.0",
                "---",
                "neighbors: " + Integer.toString(slug.getNeighbors()),
                "radius: " + Integer.toString(slug.getRadius()),
                "grid_x: " + Integer.toString(slug.getGridX()),
                "grid_y: " + Integer.toString(slug.getGridY()),
                "histograms:"
        };

        writeLines(output, lines);
    }

    static void outputTrainingLabels(Writer output, List<Integer> labels) throws StorageException {
        String labelString = labels.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        String[] lines = {
                "labels: !!opencv-matrix",
                "      rows: " + labels.size(),
                "      cols: 1",
                "      dt: i",
                "      data: [ " + labelString + " ]",
                "labelsInfo: []"
        };
        writeLines(output, lines);
    }

    private static void writeLines(Writer output, String[] lines) throws StorageException {
        try {
            BufferedWriter out = new BufferedWriter(output);

            for (String l : lines) {
                out.write(l);
                out.newLine();
            }

            out.flush();
        }
        catch (IOException e) {
            throw new StorageException("Error writing headers to temporary file for recognition.");
        }
    }
}
