package uk.ac.cam.cl.quebec.face.opencv;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import uk.ac.cam.cl.quebec.face.exceptions.BadImageFormatException;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by plott on 14/02/2017.
 */
public class Detect {
    private static final String classifierPath = "/usr/local/share/OpenCV/haarcascades/haarcascade_frontalface_default.xml";

    private static CascadeClassifier classifier = null;
    private static CascadeClassifier getClassifier() {
        if (classifier == null) {
            classifier = new CascadeClassifier(classifierPath);
        }
        return classifier;
    }

    public static Rect singleInGreyscaleImage(Mat image) {
        List<Rect> rects = multipleInGreyscaleImage(image, 0);

        // Select the largest detected face
        Rect ret = new Rect(0, 0, 0, 0);
        for (Rect r : rects) {
            if (r.area() > ret.area()) {
                ret = r;
            }
        }

        return ret;
    }

    public static List<Rect> multipleInGreyscaleImage(Mat image, double minArea) {
        CascadeClassifier classifier = getClassifier();

        MatOfRect rects = new MatOfRect();
        classifier.detectMultiScale(image, rects);

        List<Rect> filtered = rects.toList();
        filtered.removeIf(r -> r.area() < minArea);
        return filtered;
    }

    private static Mat makeGreyscale(Mat in) throws QuebecException {
        switch (in.channels()) {
            case 1: {
                // Already monochrome
                return in;
            }
            case 3: {
                // Convert from rgb
                Mat greyscale = new Mat();
                Imgproc.cvtColor(in, greyscale, Imgproc.COLOR_BGR2GRAY);
                return greyscale;
            }
            default: {
                throw new BadImageFormatException("Number of colour channels in image is wrong.");
            }
        }
    }

    public static List<Mat> multipleInVideo(VideoCapture video, double minArea) throws QuebecException {
        List<Mat> retVal = new LinkedList<>();

        Mat frame = new Mat();
        while (video.read(frame)) {
            Mat greyscale = makeGreyscale(frame);
            retVal.addAll(
                    multipleInGreyscaleImage(greyscale, minArea)
                            .stream()
                            .map(r -> greyscale.submat(r).clone())
                            .collect(Collectors.toList()));
            frame.release();
            greyscale.release();
        }

        return retVal;
    }
}
