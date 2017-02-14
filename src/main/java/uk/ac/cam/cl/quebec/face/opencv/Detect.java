package uk.ac.cam.cl.quebec.face.opencv;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

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
        CascadeClassifier classifier = getClassifier();

        MatOfRect rects = new MatOfRect();
        classifier.detectMultiScale(image, rects);

        System.out.println(image.size().toString());
        System.out.println(rects.toArray().length);

        // Select the largest detected face
        Rect ret = new Rect(0, 0, 0, 0);
        for (Rect r : rects.toList()) {
            if (r.area() > ret.area()) {
                ret = r;
            }
        }

        return ret;
    }
}
