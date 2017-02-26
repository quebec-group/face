package uk.ac.cam.cl.quebec.face.messages;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.ac.cam.cl.quebec.face.MessageVisitor;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;

import java.util.Set;
import java.util.HashSet;

/**
 * Message sent to us when a new video is uploaded.
 */
public class ProcessVideoMessage extends S3DataHoldingMessage
{
    private String eventId;
    private int videoId;
    private String S3Path;
    private Set<String> usersToMatch;

    private ProcessVideoMessage() {}

    public ProcessVideoMessage(String eventId, int videoId, String S3Path, Set<String> usersToMatch) {
        this.eventId = eventId;
        this.videoId = videoId;
        this.usersToMatch = usersToMatch;
        this.S3Path = S3Path;
    }

    public Set<String> getRecognitionUserSet() {
        return usersToMatch;
    }

    public int getVideoId() {
        return videoId;
    }

    public String getS3Path() {
        return S3Path;
    }

    public String getEventId() {
        return eventId;
    }

    public static ProcessVideoMessage constructFromJson(JSONObject json) {
        ProcessVideoMessage message = new ProcessVideoMessage();

        message.S3Path = (String) json.get("S3ID");
        message.eventId = (String) json.get("eventID");

        //TODO Make sure its actually this
        String basename = message.S3Path.substring(0, message.S3Path.lastIndexOf('.'));
        message.videoId = Integer.parseInt(basename.substring(basename.length() - 6));

        message.usersToMatch = new HashSet<>();
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
