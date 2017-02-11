package uk.ac.cam.cl.quebec.face.trial;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.face.Face;

/**
 * Initial trials of getting OpenCV to work with java
 */
public class OpenCvTrial
{
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args)
    {
        openCvExampleTest();
        basicFaceTest();
    }

    private static void basicFaceTest()
    {
        Face f = new Face();
        f.createBIF();
    }

    private static void openCvExampleTest()
    {
        System.out.println("Welcome to OpenCV " + Core.VERSION);
        Mat m = new Mat(5, 10, CvType.CV_8UC1, new Scalar(0));
        System.out.println("OpenCV Mat: " + m);
        Mat mr1 = m.row(1);
        mr1.setTo(new Scalar(1));
        Mat mc5 = m.col(5);
        mc5.setTo(new Scalar(5));
        System.out.println("OpenCV Mat data:\n" + m.dump());
    }
}
