package uk.ac.cam.cl.quebec.face;

import uk.ac.cam.cl.quebec.face.aws.S3AssetDownloader;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
import uk.ac.cam.cl.quebec.face.exceptions.InvalidArgumentException;
import uk.ac.cam.cl.quebec.face.messages.TrainOnVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.Message;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main class for the FaceDaemon detection daemon.
 * This class polls a queue to be given jobs, taking input and returning results to s3.
 */
public class FaceDaemon
{
    private String mQueueUrl;
    private String mStopFilePath;

    // Temporary fake queue
    private List<Message> tempQueue;
    private int currentMsg;

    public FaceDaemon(String queueUrl, String stopFilePath) throws QuebecException {
        mQueueUrl = queueUrl;
        mStopFilePath = stopFilePath;

        tempQueue = makeDummyMessageQueue();
        currentMsg = 0;

        connectToQueue();
    }

    private List<Message> makeDummyMessageQueue() {
        List<Message> queue = new ArrayList<>();

        queue.add(new TrainOnVideoMessage(0, "0", "img/training/0/0.jpg"));
        queue.add(new TrainOnVideoMessage(1, "0", "img/training/0/1.jpg"));
        queue.add(new TrainOnVideoMessage(2, "0", "img/training/0/2.jpg"));
        queue.add(new TrainOnVideoMessage(3, "0", "img/training/0/3.jpg"));
        queue.add(new TrainOnVideoMessage(4, "0", "img/training/0/4.jpg"));
        queue.add(new TrainOnVideoMessage(10, "1", "img/training/1/0.jpg"));
        queue.add(new TrainOnVideoMessage(11, "1", "img/training/1/1.jpg"));
        queue.add(new TrainOnVideoMessage(12, "1", "img/training/1/2.jpg"));
        queue.add(new TrainOnVideoMessage(20, "2", "img/training/2/0.jpg"));
        queue.add(new TrainOnVideoMessage(21, "2", "img/training/2/1.jpg"));
        queue.add(new TrainOnVideoMessage(22, "2", "img/training/2/2.jpg"));

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
            File stopFile = new File(mStopFilePath);
            stopFile.createNewFile();
        }
        catch (IOException e) {
            throw new QuebecException("Error creating stop file to terminate daemon.");
        }
        return null;
    }

    private void run()
    {
        File stopFile = new File(mStopFilePath);
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
        if (args.length != 2) {
            printUsage();
        }

        try {
            FaceDaemon daemon = new FaceDaemon(args[0], args[1]);
            daemon.run();
        }
        catch (InvalidArgumentException iae) {
            System.err.println("Invalid argument: " + iae.getMessage());
            System.err.println();
            printUsage();
        }
        catch (QuebecException fe) {
            fe.printStackTrace();
        }
    }

    public static void printUsage()
    {
        System.err.println("Background daemon for \"Who's at my Party?\" face detection.");
        System.err.println("Takes 2 arguments: <QueueUrl> <StopFile>");
        Runtime.getRuntime().exit(1);
    }
}
