package uk.ac.cam.cl.quebec.face.config;


/**
 * Class which holds config for the whole daemon.
 */
public class Config {
    public final String QueueUrl;
    public final String StopFilePath;
    public final String DataDir;
    public final String TempFileDir;
    public final String S3Bucket;
    public final String AwsCredentialsFile;
    public final String LogFile;

    Config() {
        QueueUrl = "";
        StopFilePath = "";
        DataDir = "";
        TempFileDir = "";
        S3Bucket = "";
        AwsCredentialsFile = "";
        LogFile = "";
    }

    @Override
    public String toString() {
        return "Config{" +
                "QueueUrl='" + QueueUrl + '\'' +
                ", StopFilePath='" + StopFilePath + '\'' +
                ", DataDir='" + DataDir + '\'' +
                ", TempFileDir='" + TempFileDir + '\'' +
                ", S3Bucket='" + S3Bucket + '\'' +
                ", AwsCredentialsFile='" + AwsCredentialsFile + '\'' +
                ", LogFile='" + LogFile + '\'' +
                '}';
    }
}
