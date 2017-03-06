package uk.ac.cam.cl.quebec.face.exceptions;

/**
 * Error thrown when we have an image/frame which has bad values
 */
public class BadImageFormatException extends QuebecException {
    public BadImageFormatException(String message, Exception innerException) {
        super(message, innerException);
    }
    public BadImageFormatException(String message) {
        super(message);
    }
}
