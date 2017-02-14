package uk.ac.cam.cl.quebec.face.messages;

import uk.ac.cam.cl.quebec.face.MessageVisitor;

/**
 * Message sent to us when a new photo is uploaded.
 */
public class AddPhotoMessage implements Message
{
    private int photoId;
    private String S3FilePath;

    public int getPhotoId() { return photoId; }
    public String getS3FilePath() { return S3FilePath; }

    public AddPhotoMessage(int photoId, String filePath)
    {
        this.S3FilePath = filePath;
        this.photoId = photoId;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.accept(this);
    }
}
