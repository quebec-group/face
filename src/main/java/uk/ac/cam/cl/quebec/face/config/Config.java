package uk.ac.cam.cl.quebec.face.config;

/**
 * Created by plott on 21/02/2017.
 */
public class Config {
    public final String QueueUrl;
    public final String StopFilePath;
    public final String DataDir;
    public final String TempFileDir;

    Config() {
        QueueUrl = "";
        StopFilePath = "";
        DataDir = "";
        TempFileDir = "";
    }

    @Override
    public String toString() {
        return "Config{" +
                "QueueUrl='" + QueueUrl + '\'' +
                ", StopFilePath='" + StopFilePath + '\'' +
                ", DataDir='" + DataDir + '\'' +
                ", TempFileDir='" + TempFileDir + '\'' +
                '}';
    }
}
