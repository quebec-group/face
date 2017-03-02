package uk.ac.cam.cl.quebec.face;

import uk.ac.cam.cl.quebec.face.config.Config;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class for logging within the daemon
 */
public class Logging {
    private static Logger logger;

    public static void setupLogger(Config config) throws QuebecException {
        if (logger != null) {
            throw new QuebecException("Logger already created");
        }

        logger = Logger.getLogger("uk.ac.cam.cl.quebec.face");

        if (!config.LogFile.equals("")) {
            try {
                logger.addHandler(new FileHandler(config.LogFile));
            }
            catch (IOException e) {
                throw new QuebecException("Failed to open log file", e);
            }
        }

        logger.setLevel(Level.ALL);
    }

    public static Logger getLogger() throws QuebecException {
        if (logger == null) {
            throw new QuebecException("Logger has not yet bee created");
        }
        return logger;
    }
}
