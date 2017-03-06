package uk.ac.cam.cl.quebec.face;

import uk.ac.cam.cl.quebec.face.aws.MessageQueue;
import uk.ac.cam.cl.quebec.face.aws.S3Manager;
import uk.ac.cam.cl.quebec.face.config.Config;
import uk.ac.cam.cl.quebec.face.config.ConfigLoader;
import uk.ac.cam.cl.quebec.face.config.ConfigValidationResult;
import uk.ac.cam.cl.quebec.face.config.ConfigValidator;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
import uk.ac.cam.cl.quebec.face.messages.TrainOnVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.Message;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.WaitMessage;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Main class for the FaceDaemon detection daemon.
 * This class polls a queue to be given jobs, taking input and returning results to s3.
 */
public class FaceDaemon
{
    private Config mConfig;

    private MessageQueue queue;

    public FaceDaemon(Config config) throws QuebecException {
        mConfig = config;
        queue = new MessageQueue(config);
    }

    // Blocks on queue, waiting for messages until timeout (secs) expires
    private Message getJobFromQueue(int timeout) throws QuebecException {
        // Wait and see if we get a message before timeout
        Message nextMessage = queue.getMessage();
        while (timeout != 0 && nextMessage instanceof WaitMessage) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new QuebecException("Interrupted while waiting for a message to process");
            }
            timeout--;
            nextMessage = queue.getMessage();
        }

        return nextMessage;
    }

    private void run()
    {
        File stopFile = new File(mConfig.StopFilePath);
        while (true)
        {
            if (stopFile.exists()) {
                // Die gracefully
                stopFile.delete();
                break;
            }

            fetchAndProcessMessage(30);
        }
    }

    private void fetchAndProcessMessage(int timeout) {
        S3Manager downloader = new S3Manager(mConfig);
        try {
            Message job = getJobFromQueue(timeout);
            if (job instanceof WaitMessage) {
                return;
            }

            MessageProcessor processor = new MessageProcessor(mConfig, downloader);
            job.visit(processor);

            System.err.println("Done");
        }
        catch (QuebecException e) {
            e.printStackTrace();
        }
        finally {
            downloader.cleanupTempFiles();
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 1) {
            printUsage();
        }

        try {
            // Load config file
            Config config = ConfigLoader.load(args[0]);

            // Validate the provided config
            ConfigValidator validator = new ConfigValidator(config);
            List<ConfigValidationResult> validationResults = validator.validate();

            if (validationResults.size() != 0) {
                // Print out any messages from validation
                validationResults.forEach(System.out::println);

                // Die now if there was a config error - warning and info are okay
                ConfigValidationResult worst = validationResults.stream()
                        .max(new ConfigValidationResult.SeverityRankingComparator())
                        .get();
                if (worst.severity == ConfigValidationResult.Severity.ERROR
                        || worst.severity == ConfigValidationResult.Severity.CRIT) {
                    return;
                }
            }

            Logging.setupLogger(config);

            FaceDaemon daemon = new FaceDaemon(config);
            daemon.run();
        }
        catch (QuebecException fe) {
            fe.printStackTrace();
        }
    }

    public static void printUsage()
    {
        System.err.println("Background daemon for \"Who's at my Party?\" face detection.");
        System.err.println("Takes 1 argument: <ConfigFile>");
        Runtime.getRuntime().exit(1);
    }
}
