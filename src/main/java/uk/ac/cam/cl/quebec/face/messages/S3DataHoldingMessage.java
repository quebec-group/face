package uk.ac.cam.cl.quebec.face.messages;

/**
 * Created by plott on 26/02/2017.
 */
public abstract class S3DataHoldingMessage implements Message {
    public abstract String getS3Path();
}
