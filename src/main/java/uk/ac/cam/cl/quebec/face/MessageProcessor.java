package uk.ac.cam.cl.quebec.face;

import org.opencv.core.*;
import org.opencv.face.Face;
import org.opencv.face.FaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import uk.ac.cam.cl.quebec.face.exceptions.BadImageFormatException;
import uk.ac.cam.cl.quebec.face.exceptions.FaceException;
import uk.ac.cam.cl.quebec.face.exceptions.VideoLoadException;
import uk.ac.cam.cl.quebec.face.messages.AddPhotoMessage;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;
import uk.ac.cam.cl.quebec.face.opencv.Detect;

import java.io.File;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;

/**
 * Central visitor which handles correct processing of all messages.
 */
public class MessageProcessor implements MessageVisitor
{
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private static final String localTrainingFile = "/tmp/opencv-training.yaml";
    private static final double recognitionThreshold = 1000;

    private S3AssetDownloader s3Downloader;

    public MessageProcessor(S3AssetDownloader downloader) {
        s3Downloader = downloader;
    }

    public void accept(AddPhotoMessage msg) throws FaceException
    {
        System.out.println("Processing AddPhotoMessage: " + Integer.toString(msg.getPhotoId()));
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

        // Train the global recogniser on the new face
        // TODO: This is a concurrency bug - will be fixed when we upgrade to final system
        FaceRecognizer recognizer = Face.createLBPHFaceRecognizer();
        if (new File(localTrainingFile).exists()) {
            recognizer.load(localTrainingFile);
        }

        Mat labels = new Mat(1, 1, CvType.CV_32SC1);
        labels.setTo(new Scalar(msg.getUserId()));

        recognizer.update(Collections.singletonList(face), labels);
        recognizer.save(localTrainingFile);
    }

    public void accept(ProcessVideoMessage msg) throws FaceException
    {
        System.out.println("Processing ProcessVideoMessage: " + Integer.toString(msg.getVideoId()));

        // Fetch video from s3
        String videoFileName = s3Downloader.downloadVideo(msg);

        // Load video
        VideoCapture video = new VideoCapture(videoFileName);
        if (!video.isOpened()) {
            throw new VideoLoadException("Failed to open video file for reading");
        }

        // Detect all faces in all frames
        List<Mat> faces = Detect.multipleInVideo(video, 0.0);

        System.out.println("Have " + Integer.toString(faces.size()) + " faces to process.");

        // Prepare face recogniser
        FaceRecognizer recognizer = Face.createLBPHFaceRecognizer();
        recognizer.load(localTrainingFile);

        // Recognise people!
        List<Integer> people = faces.stream()
                .map(f -> getBestMatch(f, recognizer))
                .filter(p -> p.getValue() < recognitionThreshold)
                .map(p -> p.getKey())
                .distinct()
                .collect(Collectors.toList());

        System.out.println(people.toString());
    }

    private Map.Entry<Integer, Double> getBestMatch(Mat face, FaceRecognizer recognizer) {
        int labs[] = new int[1];
        double confidence[] = new double[1];
        recognizer.predict(face, labs, confidence);
        return new AbstractMap.SimpleEntry<>(labs[0], confidence[0]);
    }
}
