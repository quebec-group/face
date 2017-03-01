package uk.ac.cam.cl.quebec.face.exceptions;

/**
 * Created by plott on 14/02/2017.
 */
public class BadImageFormatException extends QuebecException {
    public BadImageFormatException(String message, Exception innerException) {
        super(message, innerException);
    }
    public BadImageFormatException(String message) {
        super(message);
    }
}
