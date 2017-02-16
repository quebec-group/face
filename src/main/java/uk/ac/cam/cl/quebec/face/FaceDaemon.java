package uk.ac.cam.cl.quebec.face;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.cam.cl.quebec.face.exceptions.FaceException;
import uk.ac.cam.cl.quebec.face.exceptions.InvalidArgumentException;
import uk.ac.cam.cl.quebec.face.messages.AddPhotoMessage;
import uk.ac.cam.cl.quebec.face.messages.Message;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;

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

    // Temporary fake queue
    private List<Message> tempQueue;
    private int currentMsg;

    public FaceDaemon(String queueUrl) throws FaceException {
        mQueueUrl = queueUrl;

        tempQueue = makeDummyMessageQueue();
        currentMsg = 0;

        connectToQueue();
    }

    private List<Message> makeDummyMessageQueue() {
        List<Message> queue = new ArrayList<>();

        queue.add(new AddPhotoMessage(0, 3, "img/training/0/0.jpg"));
        queue.add(new AddPhotoMessage(1, 3, "img/training/0/1.jpg"));
        Set<Integer> photos1 = new HashSet<>();
        photos1.add(0);
        queue.add(new ProcessVideoMessage(11, "img/video/1.mp4", photos1));

        return queue;
    }

    private void connectToQueue() throws FaceException
    {
        // TODO: Actually connect to the SQS queue here
    }

    private Message getJobFromQueue()
    {
        if (currentMsg < tempQueue.size()) {
            currentMsg++;
            return tempQueue.get(currentMsg-1);
        }
        throw new NotImplementedException();
    }

    private void run()
    {
        while (true)
        {
            Message job = getJobFromQueue();
            S3AssetDownloader downloader = new S3AssetDownloader();
            MessageProcessor processor = new MessageProcessor(downloader);

            try {
                job.visit(processor);
            }
            catch (FaceException e) {
                e.printStackTrace();
            }

            downloader.cleanupTempFiles();
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 1) {
            printUsage();
        }

        try {
            FaceDaemon daemon = new FaceDaemon(args[0]);
            daemon.run();
        }
        catch (InvalidArgumentException iae) {
            System.err.println("Invalid argument: " + iae.getMessage());
            System.err.println();
            printUsage();
        }
        catch (FaceException fe) {
            fe.printStackTrace();
        }
    }

    public static void printUsage()
    {
        System.err.println("Background daemon for \"Who's at my Party?\" face detection.");
        System.err.println("Takes 1 argument: <QueueUrl>");
        Runtime.getRuntime().exit(1);
    }
}
