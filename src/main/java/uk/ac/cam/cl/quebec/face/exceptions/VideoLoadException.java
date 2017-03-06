package uk.ac.cam.cl.quebec.face.exceptions;

/**
 * Problem loading video file from local storage
 */
public class VideoLoadException extends QuebecException {
    public VideoLoadException(String msg, Exception innerException) {
        super(msg, innerException);
    }
    public VideoLoadException(String msg) {
        super(msg);
    }
}
