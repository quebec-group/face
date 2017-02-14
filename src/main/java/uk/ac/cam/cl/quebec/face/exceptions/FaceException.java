package uk.ac.cam.cl.quebec.face.exceptions;

/**
 * Base class for all exceptions we throw.
 */
public class FaceException extends Exception {
    public FaceException(String message) {
        super(message);
    }
}
