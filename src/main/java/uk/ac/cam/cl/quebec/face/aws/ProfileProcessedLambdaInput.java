package uk.ac.cam.cl.quebec.face.aws;

public class ProfileProcessedLambdaInput {

    private String userID;
    private String S3ID;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getS3ID() {
        return S3ID;
    }

    public void setS3ID(String s3ID) {
        S3ID = s3ID;
    }
}