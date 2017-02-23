package uk.ac.cam.cl.quebec.face.messages;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.ac.cam.cl.quebec.face.MessageVisitor;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Message sent to us when a new video is uploaded.
 */
public class ProcessVideoMessage implements Message
{
    private int eventId;
    private int videoId;
    private String S3Path;
    private List<String> usersToMatch;
    private Set<Integer> recognitionImageSet;

    private ProcessVideoMessage() {}

    public ProcessVideoMessage(int eventId, int videoId, String S3Path, Set<Integer> recognitionImageSet) {
        this.eventId = eventId;
        this.videoId = videoId;
        this.recognitionImageSet = recognitionImageSet;
        this.S3Path = S3Path;
    }

    public Set<Integer> getRecognitionImageSet() {
        return recognitionImageSet;
    }

    public int getVideoId() {
        return videoId;
    }

    public String getS3Path() {
        return S3Path;
    }

    public int getEventId() {
        return eventId;
    }

    public static ProcessVideoMessage constructFromJson(JSONObject json) {
        ProcessVideoMessage message = new ProcessVideoMessage();

        message.S3Path = (String) json.get("S3ID");
        message.eventId = Integer.parseInt((String) json.get("eventId"));

        //TODO Make sure its actually this
        message.videoId = Integer.parseInt(message.S3Path.substring(message.S3Path.length() - 6));

        message.usersToMatch = new ArrayList<>();
        JSONArray usersToMatch = (JSONArray) json.get("usersToMatch");

        for (Object user : usersToMatch) {
            if (user instanceof String) {
                message.usersToMatch.add((String) user);
            }
        }

        return message;
    }

    @Override
    public void visit(MessageVisitor visitor) throws QuebecException {
        visitor.accept(this);
    }
}
