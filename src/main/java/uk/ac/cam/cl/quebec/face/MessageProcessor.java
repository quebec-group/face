package uk.ac.cam.cl.quebec.face;

import org.opencv.core.*;
import org.opencv.face.Face;
import org.opencv.face.FaceRecognizer;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import uk.ac.cam.cl.quebec.face.config.Config;
import uk.ac.cam.cl.quebec.face.exceptions.BadImageFormatException;
import uk.ac.cam.cl.quebec.face.exceptions.VideoLoadException;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
import uk.ac.cam.cl.quebec.face.messages.TrainOnVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;
import uk.ac.cam.cl.quebec.face.opencv.Detect;
import uk.ac.cam.cl.quebec.face.storage.TrainingFiles;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;

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

    private S3AssetDownloader s3Downloader;
    private Config config;

    private static Mat singletonZeroLabel = new Mat(1, 1, CvType.CV_32SC1, new Scalar(0));

    public MessageProcessor(Config config, S3AssetDownloader downloader) {
        this.config = config;
        s3Downloader = downloader;
    }

    public void accept(TrainOnVideoMessage msg) throws QuebecException
    {
        System.err.println("Processing TrainOnVideoMessage: " + Integer.toString(msg.getVideoId()));
        // Fetch image from S3
        String imgPath = s3Downloader.downloadImage(msg);

        // Load it into memory - all our OpenCV operations operate on greyscale images
        Mat imageInput = Imgcodecs.imread(imgPath, CV_LOAD_IMAGE_GRAYSCALE);
        if (imageInput.empty()) {
            throw new BadImageFormatException("Error opening file " + imgPath);
        }

        // Detect the single largest face
        Rect facePosition = Detect.singleInGreyscaleImage(imageInput);
        Mat face = imageInput.submat(facePosition);

        // Train a recogniser on the new face
        LBPHFaceRecognizer recognizer = Face.createLBPHFaceRecognizer();
        recognizer.train(Collections.singletonList(face), singletonZeroLabel);
        TrainingFiles.save(recognizer, config, msg.getUserId());
    }

    public void accept(ProcessVideoMessage msg) throws QuebecException
    {
        System.err.println("Processing ProcessVideoMessage: " + Integer.toString(msg.getVideoId()));

        // Fetch video from s3
        String videoFileName = s3Downloader.downloadVideo(msg);

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
        System.err.println(numberOfMatchesById.entrySet().stream()
                .filter(e -> e.getValue() > minNumberOfFramesMatching)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));

    }

    private Map.Entry<Integer, Double> getBestMatch(Mat face, FaceRecognizer recognizer) {
        int labs[] = new int[1];
        double confidence[] = new double[1];
        recognizer.predict(face, labs, confidence);
        return new AbstractMap.SimpleEntry<>(labs[0], confidence[0]);
    }
}
