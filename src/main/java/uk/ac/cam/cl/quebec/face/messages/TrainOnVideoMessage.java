package uk.ac.cam.cl.quebec.face.messages;

import uk.ac.cam.cl.quebec.face.MessageVisitor;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;

/**
 * Message sent to us when a new video is uploaded for training purposes.
 */
public class TrainOnVideoMessage implements Message
{
    private int videoId;
    private int userId;
    private String localFilePath;

    public int getVideoId() { return videoId; }
    public String getLocalFilePath() { return localFilePath; }

    public int getUserId() {
        return userId;
    }

    public TrainOnVideoMessage(int videoId, int userId, String filePath)
    {
        this.localFilePath = filePath;
        this.videoId = videoId;
        this.userId = userId;
    }

    @Override
    public void visit(MessageVisitor visitor) throws QuebecException {
        visitor.accept(this);
    }
}
