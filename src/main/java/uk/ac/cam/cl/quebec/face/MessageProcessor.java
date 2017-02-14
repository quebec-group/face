package uk.ac.cam.cl.quebec.face;

import uk.ac.cam.cl.quebec.face.messages.AddPhotoMessage;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;

/**
 * Created by plott on 14/02/2017.
 */
public class MessageProcessor implements MessageVisitor
{
    public void accept(AddPhotoMessage msg)
    {
        System.out.println("Processing AddPhotoMessage: " + Integer.toString(msg.getPhotoId()));
    }

    public void accept(ProcessVideoMessage msg)
    {
        System.out.println("Processing ProcessVideoMessage: " + Integer.toString(msg.getVideoId()));
    }
}
