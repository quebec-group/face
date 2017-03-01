package uk.ac.cam.cl.quebec.face.opencv;

import org.opencv.core.Rect;

/**
 * Results from inspecting an individual frame, before deciding which frames to use for training.
 */
public class FrameInspectionSummary implements Comparable<FrameInspectionSummary> {
    private double laplacianVariance;
    private Rect facePosition;
    private int frameNumber;

    public double getLaplacianVariance() {
        return laplacianVariance;
    }

    public Rect getFacePosition() {
        return facePosition;
    }

    void setLaplacianVariance(double laplacianVariance) {
        this.laplacianVariance = laplacianVariance;
    }

    void setFacePosition(Rect facePosition) {
        this.facePosition = facePosition;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    @Override
    public boolean equals(Object o) {
        FrameInspectionSummary s = (FrameInspectionSummary)o;
        if (s == null) {
            return false;
        }

        return s.frameNumber == this.frameNumber;
    }

    @Override
    public String toString() {
        return "FrameInspectionSummary{" +
                "frameNumber=" + frameNumber +
                '}';
    }

    public int compareTo(FrameInspectionSummary other) {
        return this.frameNumber - other.frameNumber;
    }
}
