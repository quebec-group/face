package uk.ac.cam.cl.quebec.face.messages;

import uk.ac.cam.cl.quebec.face.MessageVisitor;
import uk.ac.cam.cl.quebec.face.exceptions.FaceException;

/**
 * Message sent to us when a new photo is uploaded.
 */
public class AddPhotoMessage implements Message
{
    private int photoId;
    private int userId;
    private String S3FilePath;

    public int getPhotoId() { return photoId; }
    public String getS3FilePath() { return S3FilePath; }

    public int getUserId() {
        return userId;
    }

    public AddPhotoMessage(int photoId, int userId, String filePath)
    {
        this.S3FilePath = filePath;
        this.photoId = photoId;
        this.userId = userId;
    }

    @Override
    public void visit(MessageVisitor visitor) throws FaceException {
        visitor.accept(this);
    }
}
