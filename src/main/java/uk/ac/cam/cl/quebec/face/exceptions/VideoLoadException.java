package uk.ac.cam.cl.quebec.face.exceptions;

/**
 * Created by plott on 14/02/2017.
 */
public class VideoLoadException extends QuebecException {
    public VideoLoadException(String msg, Exception innerException) {
        super(msg, innerException);
    }
    public VideoLoadException(String msg) {
        super(msg);
    }
}
