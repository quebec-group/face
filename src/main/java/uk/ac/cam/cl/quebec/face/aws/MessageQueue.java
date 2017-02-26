package uk.ac.cam.cl.quebec.face.aws;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.ac.cam.cl.quebec.face.config.Config;
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
    }

    public uk.ac.cam.cl.quebec.face.messages.Message getMessage() {
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
            sqs.deleteMessage(config.QueueUrl, sqsMessage.getReceiptHandle());
        }

        return faceMessage;
    }

}
