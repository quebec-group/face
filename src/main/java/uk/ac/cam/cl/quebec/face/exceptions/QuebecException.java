package uk.ac.cam.cl.quebec.face.exceptions;

/**
 * Base class for all exceptions we throw.
 */
public class QuebecException extends Exception {
    public QuebecException(String message, Exception innerException) {
        super(message, innerException);
    }
    public QuebecException(String message) {
        super(message);
    }
}
