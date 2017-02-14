package uk.ac.cam.cl.quebec.face.messages;

import uk.ac.cam.cl.quebec.face.MessageVisitor;

import java.util.Set;

/**
 * Message sent to us when a new video is uploaded.
 */
public class ProcessVideoMessage implements Message
{
    public ProcessVideoMessage(int videoId, Set<Integer> recognitionImageSet) {
        this.videoId = videoId;
        this.recognitionImageSet = recognitionImageSet;
    }

    public Set<Integer> getRecognitionImageSet() {

        return recognitionImageSet;
    }

    public int getVideoId() {

        return videoId;
    }

    private int videoId;
    private Set<Integer> recognitionImageSet;

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.accept(this);
    }
}
