package uk.ac.cam.cl.quebec.face.messages;

import org.json.simple.JSONObject;

/**
 * Messages with S3 data have the same structure, so this lets us have
 * one method to get a file in S3Manager.
 */
public abstract class S3DataHoldingMessage implements Message {
    public abstract String getS3Path();

    protected static int getInt(JSONObject params, String key) {
        if (params.containsKey(key) && params.get(key) instanceof Number) {
            return ((Number) params.get(key)).intValue();
        }

        return -1;
    }
}
