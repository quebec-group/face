package uk.ac.cam.cl.quebec.face.opencv;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
import uk.ac.cam.cl.quebec.face.exceptions.VideoLoadException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Functions which perform actions on videos for us
 */
public class Videos {
    public static List<FrameInspectionSummary> inspectAllFrames(String videoPath)
            throws QuebecException {

        VideoCapture video = new VideoCapture(videoPath);
        if (!video.isOpened()) {
            throw new VideoLoadException("Failed to open video file for reading");
        }

        List<FrameInspectionSummary> frames = new ArrayList<>();

        Mat frame = new Mat();
        int frameNumber = 0;
        while (video.read(frame)) {
            FrameInspectionSummary summary = new FrameInspectionSummary();

            summary.setLaplacianVariance(Images.varianceOfLaplacian(frame));

            Mat greyscale = Images.makeGreyscale(frame);
            summary.setFacePosition(Detect.singleInGreyscaleImage(greyscale));

            summary.setFrameNumber(frameNumber);
            frameNumber++;

            frames.add(summary);
        }

        video.release();
        return frames;
    }
}
