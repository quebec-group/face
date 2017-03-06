package uk.ac.cam.cl.quebec.face.messages;

import uk.ac.cam.cl.quebec.face.MessageVisitor;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;

/**
 * A message passed to use from AWS Lambda via SQS
 */
public interface Message {
    void visit(MessageVisitor visitor) throws QuebecException;
}
