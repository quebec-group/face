package uk.ac.cam.cl.quebec.face;

import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.TrainOnVideoMessage;

/**
 * Simple visitor interface for messages from AWS.
 */
public interface MessageVisitor {
    void accept(ProcessVideoMessage msg) throws QuebecException;
    void accept(TrainOnVideoMessage msg) throws QuebecException;
}
