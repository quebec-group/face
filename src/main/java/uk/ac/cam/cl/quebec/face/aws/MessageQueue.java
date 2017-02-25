package uk.ac.cam.cl.quebec.face.aws;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.TrainOnVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.WaitMessage;

import java.util.List;

public class MessageQueue {
    private static String VIDEO_QUEUE = "https://sqs.eu-west-1.amazonaws.com/926867918335/processing-queue";

    private final AmazonSQSClient sqs;
    private final JSONParser parser = new JSONParser();

    public MessageQueue() {
        sqs = new AmazonSQSClient(CredentialsManager.getCredentials());
        sqs.setRegion(CredentialsManager.getRegion());
    }

    public uk.ac.cam.cl.quebec.face.messages.Message getMessage() {
        List<Message> messages = sqs.receiveMessage(VIDEO_QUEUE).getMessages();

        if (messages.isEmpty()) {
            return new WaitMessage();
        }

        Message sqsMessage = messages.get(0);
        uk.ac.cam.cl.quebec.face.messages.Message faceMessage = null;

        try {
            JSONObject json = (JSONObject) parser.parse(sqsMessage.getBody());

            String messageType = (String) json.get("type");
            switch (messageType) {
                case "Training Video":
                    faceMessage = TrainOnVideoMessage.constructFromJson(json);
                    break;
                case "Event Video":
                    faceMessage = ProcessVideoMessage.constructFromJson(json);
                    break;
                default:
                    faceMessage = new WaitMessage();
                    break;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            faceMessage = new WaitMessage();
        } finally {
            sqs.deleteMessage(VIDEO_QUEUE, sqsMessage.getReceiptHandle());
        }

        return faceMessage;
    }

}
