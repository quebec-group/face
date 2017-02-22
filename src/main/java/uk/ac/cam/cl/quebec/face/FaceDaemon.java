package uk.ac.cam.cl.quebec.face;

import uk.ac.cam.cl.quebec.face.config.Config;
import uk.ac.cam.cl.quebec.face.config.ConfigLoader;
import uk.ac.cam.cl.quebec.face.config.ConfigValidationResult;
import uk.ac.cam.cl.quebec.face.config.ConfigValidator;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
import uk.ac.cam.cl.quebec.face.messages.TrainOnVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.Message;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;

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

    // Temporary fake queue
    private List<Message> tempQueue;
    private int currentMsg;

    public FaceDaemon(Config config) throws QuebecException {
        mConfig = config;

        tempQueue = makeDummyMessageQueue();
        currentMsg = 0;

        connectToQueue();
    }

    private List<Message> makeDummyMessageQueue() {
        List<Message> queue = new ArrayList<>();

        queue.add(new TrainOnVideoMessage(0, 0, "img/training/0/0.jpg"));
        queue.add(new TrainOnVideoMessage(1, 0, "img/training/0/1.jpg"));
        queue.add(new TrainOnVideoMessage(2, 0, "img/training/0/2.jpg"));
        queue.add(new TrainOnVideoMessage(3, 0, "img/training/0/3.jpg"));
        queue.add(new TrainOnVideoMessage(4, 0, "img/training/0/4.jpg"));
        queue.add(new TrainOnVideoMessage(10, 1, "img/training/1/0.jpg"));
        queue.add(new TrainOnVideoMessage(11, 1, "img/training/1/1.jpg"));
        queue.add(new TrainOnVideoMessage(12, 1, "img/training/1/2.jpg"));
        queue.add(new TrainOnVideoMessage(20, 2, "img/training/2/0.jpg"));
        queue.add(new TrainOnVideoMessage(21, 2, "img/training/2/1.jpg"));
        queue.add(new TrainOnVideoMessage(22, 2, "img/training/2/2.jpg"));

        Set<Integer> photos1 = new HashSet<>();
        photos1.add(0);
        queue.add(new ProcessVideoMessage(11, 1, "img/video/1.mp4", photos1));

        return queue;
    }

    private void connectToQueue() throws QuebecException
    {
        // TODO: Actually connect to the SQS queue here
    }

    // Blocks on queue, waiting for messages until timeout (secs) expires
    private Message getJobFromQueue(int timeout) throws QuebecException
    {
        if (currentMsg < tempQueue.size()) {
            currentMsg++;
            return tempQueue.get(currentMsg-1);
        }

        try {
            File stopFile = new File(mConfig.StopFilePath);
            stopFile.createNewFile();
        }
        catch (IOException e) {
            throw new QuebecException("Error creating stop file to terminate daemon.");
        }
        return null;
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
        S3AssetDownloader downloader = new S3AssetDownloader();
        try {
            Message job = getJobFromQueue(timeout);
            if (job == null) {
                return;
            }

            MessageProcessor processor = new MessageProcessor(downloader);
            job.visit(processor);
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
