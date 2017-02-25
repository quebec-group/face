package uk.ac.cam.cl.quebec.face.storage;

import org.opencv.face.LBPHFaceRecognizer;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Encoding and decoding of the settings used when creating a training file into a directory name
 */
public class RecogniserConfigSlug implements Comparable<RecogniserConfigSlug> {
    private final int slugFields = 4;
    private Integer fields[] = new Integer[slugFields];

    public int getRadius() {
        return fields[0];
    }
    public int getNeighbors() {
        return fields[1];
    }
    public int getGridX() {
        return fields[2];
    }
    public int getGridY() {
        return fields[3];
    }

    public RecogniserConfigSlug(LBPHFaceRecognizer recognizer) {
        fields[0] = recognizer.getRadius();
        fields[1] = recognizer.getNeighbors();
        fields[2] = recognizer.getGridX();
        fields[3] = recognizer.getGridY();
    }
    public RecogniserConfigSlug(String slug) {
        fields = Arrays.stream(slug.split("-"))
                .map(Integer::parseInt)
                .collect(Collectors.toList())
                .toArray(fields);
    }

    @Override
    public String toString() {
        return Arrays.stream(fields)
                .map(Object::toString)
                .collect(Collectors.joining("-"));
    }

    @Override
    public int compareTo(RecogniserConfigSlug other) {
        for (int i = 0; i < slugFields; i++) {
            if (!fields[i].equals(other.fields[i])) {
                return fields[i].compareTo(other.fields[i]);
            }
        }
        return 0;
    }
}
