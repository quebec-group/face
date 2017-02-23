package uk.ac.cam.cl.quebec.face.messages;

import org.json.simple.JSONObject;
import uk.ac.cam.cl.quebec.face.MessageVisitor;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;

/**
 * Message sent to us when a new video is uploaded for training purposes.
 */
public class TrainOnVideoMessage implements Message
{
    private int videoId;
    private String userId;
    private String S3Path;

    public int getVideoId() { return videoId; }
    public String getS3Path() { return S3Path; }

    public String getUserId() {
        return userId;
    }

    private TrainOnVideoMessage() {}

    public TrainOnVideoMessage(int videoId, String userId, String filePath)
    {
        this.S3Path = filePath;
        this.videoId = videoId;
        this.userId = userId;
    }

    public static TrainOnVideoMessage constructFromJson(JSONObject json) {
        TrainOnVideoMessage message = new TrainOnVideoMessage();

        message.S3Path = (String) json.get("S3ID");

        message.userId = (String) json.get("userID");
        //TODO Make sure its actually this
        message.videoId = Integer.parseInt(message.S3Path.substring(message.S3Path.length() - 6));

        return message;
    }

    @Override
    public void visit(MessageVisitor visitor) throws QuebecException {
        visitor.accept(this);
    }
}
