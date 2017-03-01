package uk.ac.cam.cl.quebec.face.exceptions;

/**
 * Exception thrown when an error occurs trying to persist data to local storage.
 */
public class StorageException extends QuebecException {
    public StorageException(String message) {
        super(message);
    }
}
