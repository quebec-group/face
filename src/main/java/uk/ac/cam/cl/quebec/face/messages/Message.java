package uk.ac.cam.cl.quebec.face.messages;

import uk.ac.cam.cl.quebec.face.MessageVisitor;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;

/**
 * Created by plott on 14/02/2017.
 */
public interface Message {
    void visit(MessageVisitor visitor) throws QuebecException;
}
