package uk.ac.cam.cl.quebec.face.exceptions;

/**
 * Thrown when we have an error interacting with AWS
 */
public class AmazonException extends QuebecException {
    public AmazonException(String msg, Exception innerException) {
        super(msg, innerException);
    }
    public AmazonException(String msg) {
        super(msg);
    }
}
