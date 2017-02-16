package uk.ac.cam.cl.quebec.face.exceptions;

/**
 * Command line arguments were invalid
 */
public class InvalidArgumentException extends FaceException {
    public InvalidArgumentException(String msg) {
        super(msg);
    }
}
