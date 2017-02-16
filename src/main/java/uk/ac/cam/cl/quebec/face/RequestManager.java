package uk.ac.cam.cl.quebec.face;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;

public class RequestManager {

    public static String profileQueueURL = "https://sqs.eu-west-1.amazonaws.com/926867918335/profile-picture-uploads";
    private final AmazonSQSClient sqs;
    private final JSONParser parser = new JSONParser();

    public RequestManager() {
        sqs = new AmazonSQSClient(CredentialsManager.getCredentials());
        Region region = Region.getRegion(Regions.EU_WEST_1);
        sqs.setRegion(region);
    }

    public void sendMessageToQueue(String queue, String message) {
        sqs.sendMessage(new SendMessageRequest(queue, message));
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
