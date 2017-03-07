package uk.ac.cam.cl.quebec.face.aws;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.ac.cam.cl.quebec.face.config.Config;
import uk.ac.cam.cl.quebec.face.exceptions.AmazonException;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.TrainOnVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.WaitMessage;

import java.util.List;

public class MessageQueue {
    private Config config;

    private final AmazonSQSClient sqs;
    private final JSONParser parser = new JSONParser();

    public MessageQueue(Config config) {
        this.config = config;
        sqs = new AmazonSQSClient(CredentialsManager.getCredentials(config));
        sqs.setRegion(CredentialsManager.getRegion());
        Monitor.setConfig(config);
    }

    public uk.ac.cam.cl.quebec.face.messages.Message getMessage() throws AmazonException {
        List<Message> messages = sqs.receiveMessage(config.QueueUrl).getMessages();

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
                    System.err.println("Received json: " + json);
                    faceMessage = TrainOnVideoMessage.constructFromJson(json);
                    break;
                case "Event Video":
                    System.err.println("Received json: " + json);
                    faceMessage = ProcessVideoMessage.constructFromJson(json);
                    break;
                default:
                    throw new AmazonException("Received unknown message type. Type was " + messageType);
            }
        } catch (ParseException e) {
            throw new AmazonException("Failed to parse json. input follows: " + sqsMessage.getBody(), e);
        } finally {
            sqs.deleteMessage(config.QueueUrl, sqsMessage.getReceiptHandle());
        }

        return faceMessage;
    }

}
