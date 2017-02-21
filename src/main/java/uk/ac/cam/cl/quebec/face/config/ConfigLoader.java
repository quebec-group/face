package uk.ac.cam.cl.quebec.face.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.ac.cam.cl.quebec.face.exceptions.ConfigException;

import java.io.*;

/**
 * Loads a config file into the application
 */
public class ConfigLoader {
    public static Config load(String fileName) throws ConfigException {
        try {
            File config = new File(fileName);
            Reader fileReader = new FileReader(config);

            Gson gson = new GsonBuilder().create();
            return gson.fromJson(fileReader, Config.class);
        }
        catch (IOException fnfe) {
            throw new ConfigException("Error opening/reading config file: " + fnfe.getMessage());
        }
    }
}
