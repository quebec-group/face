package uk.ac.cam.cl.quebec.face.opencv;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.face.Face;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.videoio.VideoCapture;
import uk.ac.cam.cl.quebec.face.Logging;
import uk.ac.cam.cl.quebec.face.config.Config;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
import uk.ac.cam.cl.quebec.face.exceptions.StorageException;
import uk.ac.cam.cl.quebec.face.exceptions.VideoLoadException;
import uk.ac.cam.cl.quebec.face.storage.TrainingFiles;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Business logic for training on videos, to separate this from aws integration
 */
public class VideoTrainer {

    private static final double maxFramesPerVideo = 10.0;

    private Config config;
    private String userId;
    private String videoPath;

    private static Mat singletonZeroLabel = new Mat(1, 1, CvType.CV_32SC1, new Scalar(0));

    public VideoTrainer(Config config, String userId, String videoPath) {
        this.config = config;
        this.userId = userId;
        this.videoPath = videoPath;
    }

    public void train() throws QuebecException {
        // We perform two passes of the video.
        // On the first, we analyse frames so that we can make decisions about which to use.
        // On the second, we do the actual face training.

        // First pass
        List<FrameInspectionSummary> allFrames = Videos.inspectAllFrames(videoPath);
        Logging.getLogger().info(allFrames.toString());

        List<Double> variances = allFrames.stream()
                .map(FrameInspectionSummary::getLaplacianVariance)
                .collect(Collectors.toList());
        double minVariance = variances.stream().min(Comparator.naturalOrder()).get();
        double maxVariance = variances.stream().max(Comparator.naturalOrder()).get();
        // Take frames in the top 20% of the range of variances found
        double varianceThreshold = maxVariance - (maxVariance - minVariance) / 5;

        List<FrameInspectionSummary> interestingFrames = allFrames.stream()
                .filter(f -> f.getLaplacianVariance() > varianceThreshold)
        //        .filter(f -> f.getFacePosition().area() > 1000)
                .collect(Collectors.toList());

        System.err.println(interestingFrames.size() + " frames accepted as viable for training. Choosing " + (int)maxFramesPerVideo);

        // Decide which frames to use
        List<FrameInspectionSummary> framesToUse = selectFramesFromCandidates(interestingFrames, allFrames.size());

        // Second pass
        trainOnSelectedFrames(framesToUse);
    }

    private int findClosestFrameIn(List<FrameInspectionSummary> availableFrames, int idealFrameNum, int previousFrameIndex) throws QuebecException {
        Logger logger = Logging.getLogger();

        final int frameNumber = idealFrameNum;
        Function<Integer, Integer> frameError = index -> Math.abs(availableFrames.get(index).getFrameNumber() - frameNumber);
        Function<Integer, Boolean> frameErrorCondition = index -> frameError.apply(index - 1) < frameError.apply(index);

        int closestIndex = previousFrameIndex;
        while (true) {
            logger.info("Trying index " + closestIndex);
            logger.info("Frame number is " + availableFrames.get(closestIndex) + ", we were looking for " + idealFrameNum);
            if (closestIndex - 1 >= 0 && frameErrorCondition.apply(closestIndex)) {
                closestIndex--;
            } else if (closestIndex + 1 < availableFrames.size() && !frameErrorCondition.apply(closestIndex + 1)) {
                closestIndex++;
            } else {
                return closestIndex;
            }
        }
    }

    private List<FrameInspectionSummary> selectFramesFromCandidates(List<FrameInspectionSummary> candidates, int framesInVideo) throws QuebecException {
        // We are aiming for a particular number of frames, uniformly distributed
        List<FrameInspectionSummary> framesToUse = new ArrayList<>();

        if (candidates.size() > maxFramesPerVideo) {
            List<FrameInspectionSummary> availableFrames = new LinkedList<>(candidates);
            double increment = ((double)framesInVideo) / maxFramesPerVideo;
            int currentIndex = 0;
            for (int frameNum = (int) (increment / 2); frameNum < framesInVideo; frameNum += increment) {
                // Find nearest frame to frameNum which is available
                int closestIndex = findClosestFrameIn(availableFrames, frameNum, currentIndex);

                framesToUse.add(availableFrames.get(closestIndex));
                availableFrames.remove(closestIndex);
                if (closestIndex > 0) {
                    closestIndex--;
                }
                currentIndex = closestIndex;
            }
        }
        else {
            framesToUse = candidates;
        }
        return framesToUse;
    }

    private void trainOnSelectedFrames(List<FrameInspectionSummary> frames) throws QuebecException {
        // We're going to take 1 pass over the video, so make sure the frame numbers are in order
        frames.sort(Comparator.naturalOrder());

        VideoCapture video = new VideoCapture(videoPath);
        if (!video.isOpened()) {
            throw new VideoLoadException("Failed to open video file for reading");
        }

        int currentFrame = 0;
        for (FrameInspectionSummary frame : frames) {
            Mat img = new Mat();
            // Loop through the frames until we get to one we want to train on
            while (video.read(img)) {
                if (currentFrame == frame.getFrameNumber()) {
                    break;
                }
                currentFrame++;
            } 
            Logging.getLogger().info("Processing frame #" + currentFrame + ", while looking for " + frame.getFrameNumber());

            if (currentFrame != frame.getFrameNumber()) {
                throw new StorageException("Ran out of video while trying to read frames for training");
            }

            // Get the face in the image
            Mat colourFace = img/*.submat(frames.get(processed).getFacePosition())*/;
            Logging.getLogger().info("Size of training image is " + colourFace.size().toString());
            Mat face = Images.makeGreyscale(colourFace);

            // Train a recogniser on the new face
            LBPHFaceRecognizer recognizer = Face.createLBPHFaceRecognizer();
            recognizer.train(Collections.singletonList(face), singletonZeroLabel);
            TrainingFiles.save(recognizer, config, userId);

            currentFrame++;
        }
    }
}
