package uk.ac.cam.cl.quebec.face.opencv;

import org.opencv.core.Mat;
import org.opencv.face.FaceRecognizer;
import org.opencv.videoio.VideoCapture;
import uk.ac.cam.cl.quebec.face.config.Config;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
import uk.ac.cam.cl.quebec.face.exceptions.VideoLoadException;
import uk.ac.cam.cl.quebec.face.storage.TrainingFiles;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Business logic for recognising people in videos, to separate this from aws integration
 */
public class VideoRecogniser {

    private static final double recognitionThreshold = 50;
    private static final int minDetectWidth = 30;
    private static final int minDetectHeight = 30;
    private static final int minNumberOfFramesMatching = 5;
    private static final double minFaceDetectArea = 0.0;

    private Config config;
    private Set<String> recognitionUserSet;
    private String videoFileName;

    public VideoRecogniser(Config config, Set<String> recognitionUserSet, String videoFileName) {
        this.config = config;
        this.recognitionUserSet = recognitionUserSet;
        this.videoFileName = videoFileName;
    }

    public List<String> recognise() throws QuebecException {
        // We create a recogniser here because creation has some interesting failure modes,
        // which will make it pointless to perform the face detection step

        // Setup mapping so that OpenCV can identify people
        Map<Integer, String> userMappings = generateUserMappings(recognitionUserSet);

        // Prepare face recogniser
        FaceRecognizer recognizer = TrainingFiles.createRecogniserForUsers(config, userMappings);

        // Detect all faces throughout the video
        List<Mat> faces = findFacesInVideo(videoFileName);

        System.err.println("Have " + Integer.toString(faces.size()) + " faces to process.");

        // Recognise people!
        List<Map.Entry<Integer, Double>> matches = matchFacesToUserIds(recognizer, faces);

        System.err.println(matches.toString());

        List<String> usersInVideo = findRecognisedUsers(matches, userMappings);

        System.err.println(usersInVideo);

        // TODO: Store results of processing persistently on disk

        return usersInVideo;
    }

    private Map<Integer, String> generateUserMappings(Set<String> users) {
        // For OpenCV, users need to be expressed as integers.
        // Here, we generate a 1-to-1 mapping to fulfill this requirement
        AtomicInteger ai = new AtomicInteger();
        return users.stream()
                .collect(Collectors.toMap(u -> ai.getAndIncrement(), u -> u));
    }

    private List<Mat> findFacesInVideo(String videoPath) throws QuebecException {
        // Load video
        VideoCapture video = new VideoCapture(videoPath);
        if (!video.isOpened()) {
            throw new VideoLoadException("Failed to open video file for reading");
        }

        // Detect all faces in all frames
        return Detect.multipleInVideo(video, minFaceDetectArea)
                .stream()
                .filter(m -> m.cols() > minDetectWidth && m.rows() > minDetectHeight)
                .collect(Collectors.toList());
    }

    private List<Map.Entry<Integer, Double>> matchFacesToUserIds(FaceRecognizer recognizer, List<Mat> faces) {
        return faces.stream()
                .map(f -> getBestMatch(f, recognizer))
                .filter(p -> p.getValue() > recognitionThreshold)
                .collect(Collectors.toList());
    }

    private List<String> findRecognisedUsers(List<Map.Entry<Integer, Double>> frameMatches, Map<Integer, String> userMappings) {
        // Count matching frames
        Map<Integer, Integer> numberOfMatches = new HashMap<>();
        for (Map.Entry<Integer, Double> e : frameMatches) {
            numberOfMatches.compute(e.getKey(), (k, v) -> (v == null) ? 1 : v + 1);
        }

        // Filter valid matches, and translate internal ids to user ids
        return numberOfMatches
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > minNumberOfFramesMatching)
                .map(e -> new AbstractMap.SimpleEntry<>(userMappings.get(e.getKey()), e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map.Entry<Integer, Double> getBestMatch(Mat face, FaceRecognizer recognizer) {
        int labs[] = new int[1];
        double confidence[] = new double[1];
        recognizer.predict(face, labs, confidence);
        return new AbstractMap.SimpleEntry<>(labs[0], confidence[0]);
    }
}
