package uk.ac.cam.cl.quebec.face.messages;

import uk.ac.cam.cl.quebec.face.MessageVisitor;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;

/**
 * Message sent to us when a new video is uploaded for training purposes.
 */
public class TrainOnVideoMessage implements Message
{
    private int videoId;
    private String userId;
    private String S3FilePath;

    public int getVideoId() { return videoId; }
    public String getS3FilePath() { return S3FilePath; }
    public String getUserId() {
        return userId;
    }

    public TrainOnVideoMessage(int videoId, String userId, String filePath)
    {
        this.S3FilePath = filePath;
        this.videoId = videoId;
        this.userId = userId;
    }

    @Override
    public void visit(MessageVisitor visitor) throws QuebecException {
        visitor.accept(this);
    }
}
