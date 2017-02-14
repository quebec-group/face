package uk.ac.cam.cl.quebec.face;

import uk.ac.cam.cl.quebec.face.messages.AddPhotoMessage;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;

/**
 * Created by plott on 14/02/2017.
 */
public interface MessageVisitor {
    void accept(ProcessVideoMessage msg);
    void accept(AddPhotoMessage msg);
}