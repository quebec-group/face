package uk.ac.cam.cl.quebec.face;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
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

    public FaceDaemon(String queueUrl) throws QuebecException {
        mQueueUrl = queueUrl;

        tempQueue = makeDummyMessageQueue();
        currentMsg = 0;

        connectToQueue();
    }

    private List<Message> makeDummyMessageQueue() {
        List<Message> queue = new ArrayList<>();

        queue.add(new AddPhotoMessage(0, 0, "img/training/0/0.jpg"));
        queue.add(new AddPhotoMessage(1, 0, "img/training/0/1.jpg"));
        queue.add(new AddPhotoMessage(2, 0, "img/training/0/2.jpg"));
        queue.add(new AddPhotoMessage(3, 0, "img/training/0/3.jpg"));
        queue.add(new AddPhotoMessage(4, 0, "img/training/0/4.jpg"));
        queue.add(new AddPhotoMessage(10, 1, "img/training/1/0.jpg"));
        queue.add(new AddPhotoMessage(11, 1, "img/training/1/1.jpg"));
        queue.add(new AddPhotoMessage(12, 1, "img/training/1/2.jpg"));
      
        Set<Integer> photos1 = new HashSet<>();
        photos1.add(0);
        queue.add(new ProcessVideoMessage(11, "img/video/1.mp4", photos1));

        return queue;
    }

    private void connectToQueue() throws QuebecException
    {
        // TODO: Actually connect to the SQS queue here
    }

    private Message getJobFromQueue() throws FaceException
    {
        if (currentMsg < tempQueue.size()) {
            currentMsg++;
            return tempQueue.get(currentMsg-1);
        }
        throw new FaceException("End of message queue");
    }

    private void run()
    {
        while (true)
        {
            S3AssetDownloader downloader = new S3AssetDownloader();
            try {
                MessageProcessor processor = new MessageProcessor(downloader);
                Message job = getJobFromQueue();

                job.visit(processor);
            }
            catch (QuebecException e) {
                if (e.getMessage().equals("End of message queue")) {
                    break;
                }
                e.printStackTrace();
            }
            finally {
                downloader.cleanupTempFiles();
            }
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
        catch (QuebecException fe) {
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
