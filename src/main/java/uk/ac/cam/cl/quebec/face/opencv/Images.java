package uk.ac.cam.cl.quebec.face.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgproc.Imgproc;
import uk.ac.cam.cl.quebec.face.exceptions.BadImageFormatException;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;

/**
 * Helper functions for image processing which don't really fit anywhere else
 */
public class Images {
    public static Mat makeGreyscale(Mat in) throws QuebecException {
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

    public static double varianceOfLaplacian(Mat img) {
        Mat laplacianMat = new Mat();
        Imgproc.Laplacian(img, laplacianMat, -1);

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stdDev = new MatOfDouble();
        Core.meanStdDev(laplacianMat, mean, stdDev);

        double dev[] = stdDev.get(0, 0);
        return dev[0] * dev[0];
    }
}
