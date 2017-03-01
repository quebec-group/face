package uk.ac.cam.cl.quebec.face;

import org.opencv.core.*;
import org.opencv.face.FaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import uk.ac.cam.cl.quebec.face.aws.EventProcessedLambdaInput;
import uk.ac.cam.cl.quebec.face.aws.LambdaCaller;
import uk.ac.cam.cl.quebec.face.aws.ProfileProcessedLambdaInput;
import uk.ac.cam.cl.quebec.face.config.Config;
import uk.ac.cam.cl.quebec.face.exceptions.StorageException;
import uk.ac.cam.cl.quebec.face.exceptions.VideoLoadException;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
import uk.ac.cam.cl.quebec.face.messages.TrainOnVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;
import uk.ac.cam.cl.quebec.face.opencv.*;
import uk.ac.cam.cl.quebec.face.storage.DirectoryStructure;
import uk.ac.cam.cl.quebec.face.storage.TrainingFiles;
import uk.ac.cam.cl.quebec.face.aws.S3Manager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Central visitor which handles correct processing of all messages.
 */
public class MessageProcessor implements MessageVisitor
{
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private static final double recognitionThreshold = 50;
    private static final int minDetectWidth = 30;
    private static final int minDetectHeight = 30;
    private static final int minNumberOfFramesMatching = 5;

    private S3Manager s3Manager;
    private Config config;

    public MessageProcessor(Config config, S3Manager downloader) {
        this.config = config;
        s3Manager = downloader;
    }

    public void accept(TrainOnVideoMessage msg) throws QuebecException
    {
        System.err.println("Processing TrainOnVideoMessage: " + Integer.toString(msg.getVideoId()));

        // Fetch video from S3
        String videoPath = s3Manager.downloadFile(msg);

        // Perform the training
        VideoTrainer trainer = new VideoTrainer(config,msg.getUserId(), videoPath);
        trainer.train();

        // Ping lambda to say we're done
        sendTrainingResultsToLambda(msg.getUserId(), msg.getVideoId());
    }

    public void accept(ProcessVideoMessage msg) throws QuebecException
    {
        System.err.println("Processing ProcessVideoMessage: " + msg.getVideoId());

        // Fetch video from s3
        String videoFileName = s3Manager.downloadFile(msg);

        // We create a recogniser here because creation has some interesting failure modes,
        // which will make it pointless to perform the face detection step

        // Setup mapping so that OpenCV can identify people
        AtomicInteger ai = new AtomicInteger();
        Map<Integer, String> userMappings = msg.getRecognitionUserSet()
                .stream()
                .collect(Collectors.toMap(u -> ai.getAndIncrement(), u -> u));

        // Prepare face recogniser
        FaceRecognizer recognizer = TrainingFiles.createRecogniserForUsers(config, userMappings);

        // Load video
        VideoCapture video = new VideoCapture(videoFileName);
        if (!video.isOpened()) {
            throw new VideoLoadException("Failed to open video file for reading");
        }

        // Detect all faces in all frames
        List<Mat> faces = Detect.multipleInVideo(video, 0.0)
                .stream()
                .filter(m -> m.cols() > minDetectWidth && m.rows() > minDetectHeight)
                .collect(Collectors.toList());

        System.err.println("Have " + Integer.toString(faces.size()) + " faces to process.");

        // Recognise people!
        List<Map.Entry<Integer, Double>> matches = faces.stream()
                .map(f -> getBestMatch(f, recognizer))
                .filter(p -> p.getValue() > recognitionThreshold)
                .collect(Collectors.toList());

        // Count matching frames
        Map<Integer, Integer> numberOfMatches = new HashMap<>();
        for (Map.Entry<Integer, Double> e : matches) {
            numberOfMatches.compute(e.getKey(), (k, v) -> (v == null) ? 1 : v + 1);
        }

        // Translate internal ids to user ids
        Map<String, Integer> numberOfMatchesById = numberOfMatches
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> userMappings.get(e.getKey()), Map.Entry::getValue));

        System.err.println(matches.toString());

        List<String> usersInVideo = numberOfMatchesById
                .entrySet().stream()
                .filter(e -> e.getValue() > minNumberOfFramesMatching)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        System.err.println(usersInVideo);

        // TODO: Store results of processing persistently on disk

        // Upload results of processing to Amazon
        sendEventResultsToLambda(msg.getEventId(), usersInVideo);
    }

    private Map.Entry<Integer, Double> getBestMatch(Mat face, FaceRecognizer recognizer) {
        int labs[] = new int[1];
        double confidence[] = new double[1];
        recognizer.predict(face, labs, confidence);
        return new AbstractMap.SimpleEntry<>(labs[0], confidence[0]);
    }

    private String uploadProfilePhoto(String userId, Mat photo) throws StorageException {
        File tmpDir = DirectoryStructure.getOrMakeTempDirectory(config);
        File profilePhoto;
        try {
            profilePhoto = File.createTempFile("profile", ".jpg", tmpDir);
        }
        catch (IOException e) {
            throw new StorageException("Could not create temp file for profile photo", e);
        }

        Imgcodecs.imwrite(profilePhoto.getAbsolutePath(), photo);
        String s3FileName = "protected/" + userId + "/photos/profilePhoto.jpg";
        s3Manager.uploadFile(profilePhoto, s3FileName);

        profilePhoto.delete();
        return s3FileName;
    }

    private void sendTrainingResultsToLambda(String userId, int videoId) {
        ProfileProcessedLambdaInput results = new ProfileProcessedLambdaInput();
        results.setUserID(userId);
        results.setVideoID(videoId);
        LambdaCaller c = new LambdaCaller(config);
        c.callProfileProcessedLambda(results);
    }

    private void sendEventResultsToLambda(int eventId, List<String> usersInVideo) {
        EventProcessedLambdaInput results = new EventProcessedLambdaInput();
        results.setEventID(eventId);
        results.setMembers(usersInVideo);
        LambdaCaller c = new LambdaCaller(config);
        c.callEventProcessedLambda(results);
    }
}
