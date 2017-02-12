package uk.ac.cam.cl.quebec.face.trial;

import org.opencv.core.*;

import org.opencv.face.Face;

import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

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
        CascadeClassifier classifier = new CascadeClassifier("/usr/local/share/OpenCV/haarcascades/haarcascade_frontalface_default.xml");
        for (int i = 1; i < 8; i++)
            recogniseFaces(classifier, Integer.toString(i));

        Face f = new Face();
        f.createBIF();
    }

    private static void recogniseFaces(CascadeClassifier classifier, String name)
    {
        Mat input = Imgcodecs.imread("img/" + name + ".jpg");
        Mat greyscale = new Mat();
        System.out.println(input.channels());
        Imgproc.cvtColor(input, greyscale, Imgproc.COLOR_BGR2GRAY);

        MatOfRect rects = new MatOfRect();
        classifier.detectMultiScale(greyscale, rects);

        for (Rect r : rects.toList())
            Imgproc.rectangle(input, r.tl(), r.br(), new Scalar(255, 0, 0), 2);

        Imgcodecs.imwrite("img/" + name + "-out.jpg", input);
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
