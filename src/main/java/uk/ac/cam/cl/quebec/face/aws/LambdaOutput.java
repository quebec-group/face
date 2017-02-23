package uk.ac.cam.cl.quebec.face.aws;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LambdaOutput {
    private boolean succeeded;
    private String errorMessage = "";


    public LambdaOutput(String response) {
        JSONParser parser = new JSONParser();

        try {
            JSONObject json = (JSONObject) parser.parse(response);

            succeeded = (Boolean) json.getOrDefault("succeeded", Boolean.FALSE);

            if (!succeeded) {
                errorMessage = (String) json.getOrDefault("errorMessage", "");
            }

        } catch (ParseException e) {
            e.printStackTrace();
            succeeded = false;
            errorMessage = e.getMessage();
        }
    }

    public boolean didSucceed() {
        return succeeded;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return super.toString() + "\n\tSucceeded? " + succeeded + "\n\tError message: " + errorMessage;
    }
}
