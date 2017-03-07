package uk.ac.cam.cl.quebec.face.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class Monitor {
    private static String MONITOR_QUEUE = "https://sqs.eu-west-1.amazonaws.com/926867918335/monitoring";
    private AmazonSQS sqs;

    private static Monitor instance;

    public static Monitor getInstance() {
        if (instance == null) {
            instance = new Monitor();
        }
        return instance;
    }

    private Monitor() {
        sqs = new AmazonSQSClient(CredentialsManager.getCredentials());
        sqs.setRegion(CredentialsManager.getRegion());
    }

    public void faceCall(String data) {
        sqs.sendMessage(MONITOR_QUEUE, "faceOut:"+data);
    }

}
