package uk.ac.cam.cl.quebec.face.exceptions;

/**
 * Exception thrown for a configuration file error
 */
public class ConfigException extends QuebecException {
    public ConfigException(String msg, Exception innerException) {
        super(msg, innerException);
    }
    public ConfigException(String msg) {
        super(msg);
    }
}
