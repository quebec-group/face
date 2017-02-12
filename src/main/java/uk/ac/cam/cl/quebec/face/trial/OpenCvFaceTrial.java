package uk.ac.cam.cl.quebec.face.trial;

import org.opencv.core.*;
import org.opencv.face.Face;
import org.opencv.face.FaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Initial trials of getting OpenCV to recognise faces with java
 */
public class OpenCvFaceTrial
{
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private static List<Mat> readFromDirGreyscale(String dir, int number)
    {
        List<Mat> list = new ArrayList<>();
        for (int i = 0; i < number; i++)
        {
            Mat colour = Imgcodecs.imread(dir + Integer.toString(i) + ".jpg");
            Mat greyscale = new Mat();
            Imgproc.cvtColor(colour, greyscale, Imgproc.COLOR_BGR2GRAY);
            list.add(greyscale);
        }
        return list;
    }

    public static void main(String[] args)
    {
        List<Mat> training = readFromDirGreyscale("img/training/0/", 5);
        Mat labels = new Mat(1, 5, CvType.CV_32SC1);
        labels.setTo(new Scalar(0));

        List<Mat> recognise = readFromDirGreyscale("img/recognise/", 4);


        FaceRecognizer rec = Face.createLBPHFaceRecognizer();
        rec.train(training, labels);

        rec.save("recogniser-0");

        for (int i = 0; i < recognise.size(); i++)
        {
            int labs[] = new int[1];
            double confidence[] = new double[1];
            rec.predict(recognise.get(i), labs, confidence);
            System.out.println("Image " + Integer.toString(i) + " has label " + Integer.toString(labs[0]) +
                    " with confidence " + Double.toString(confidence[0]));
        }
    }
}
