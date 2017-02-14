package uk.ac.cam.cl.quebec.face.messages;

import uk.ac.cam.cl.quebec.face.MessageVisitor;
import uk.ac.cam.cl.quebec.face.exceptions.FaceException;

import java.util.Set;

/**
 * Message sent to us when a new video is uploaded.
 */
public class ProcessVideoMessage implements Message
{
    public ProcessVideoMessage(int videoId, String S3FilePath, Set<Integer> recognitionImageSet) {
        this.videoId = videoId;
        this.recognitionImageSet = recognitionImageSet;
        this.S3FilePath = S3FilePath;
    }

    public Set<Integer> getRecognitionImageSet() {
        return recognitionImageSet;
    }

    public int getVideoId() {
        return videoId;
    }

    public String getS3FilePath() {
        return S3FilePath;
    }

    private int videoId;
    private String S3FilePath;
    private Set<Integer> recognitionImageSet;

    @Override
    public void visit(MessageVisitor visitor) throws FaceException {
        visitor.accept(this);
    }
}
