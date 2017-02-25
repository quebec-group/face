package uk.ac.cam.cl.quebec.face.messages;

import uk.ac.cam.cl.quebec.face.MessageVisitor;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;

import java.util.Set;

/**
 * Message sent to us when a new video is uploaded.
 */
public class ProcessVideoMessage implements Message
{
    public ProcessVideoMessage(int eventId, int videoId, String localFilePath, Set<String> recognitionImageSet) {
        this.eventId = eventId;
        this.videoId = videoId;
        this.recognitionUserSet = recognitionImageSet;
        this.localFilePath = localFilePath;
    }

    public Set<String> getRecognitionUserSet() {
        return recognitionUserSet;
    }

    public int getVideoId() {
        return videoId;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public int getEventId() {
        return eventId;
    }

    private int eventId;
    private int videoId;
    private String localFilePath;
    private Set<String> recognitionUserSet;

    @Override
    public void visit(MessageVisitor visitor) throws QuebecException {
        visitor.accept(this);
    }
}
