package uk.ac.cam.cl.quebec.face;

import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
import uk.ac.cam.cl.quebec.face.messages.TrainOnVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;

/**
 * Created by plott on 14/02/2017.
 */
public interface MessageVisitor {
    void accept(ProcessVideoMessage msg) throws QuebecException;
    void accept(TrainOnVideoMessage msg) throws QuebecException;
}
