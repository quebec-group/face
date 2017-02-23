package uk.ac.cam.cl.quebec.face.aws;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

    public uk.ac.cam.cl.quebec.face.messages.Message getMessageIfAvailable() {
        List<Message> messages = sqs.receiveMessage(VIDEO_QUEUE).getMessages();

        if (messages.isEmpty()) {
            return new WaitMessage();
        }

        Message sqsMessage = messages.get(0);
        uk.ac.cam.cl.quebec.face.messages.Message faceMessage = null;
        try {
            JSONObject json = (JSONObject) parser.parse(sqsMessage.getBody());

            String messageType = (String) json.get("type");
            if (messageType.equals("Training Video")) {

            } else if (messageType.equals("Event Video")) {

            } else {
                faceMessage = new WaitMessage();
            }

        } catch (ParseException e) {
            e.printStackTrace();
            faceMessage = new WaitMessage();
        } finally {
            sqs.deleteMessage(VIDEO_QUEUE, sqsMessage.getReceiptHandle());
            return faceMessage;
        }
    }

    public List<Message> getMessages(String queue) {
        System.out.println("Receiving messages from MyQueue.\n");
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queue);
        return sqs.receiveMessage(receiveMessageRequest).getMessages();
    }

    public JSONObject getJsonFromMessage(Message message) {
        JSONObject json = null;
        try {
             json = (JSONObject) parser.parse(message.getBody());
        } catch (ParseException e) {
            e.printStackTrace();
            json = new JSONObject();
        }

        return json;
    }

    public void handleMessage(String queue, String handle) {
        sqs.deleteMessage(new DeleteMessageRequest(queue, handle));
    }
}
