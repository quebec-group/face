package uk.ac.cam.cl.quebec.face;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.cam.cl.quebec.face.exceptions.FaceException;
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
    private String mQueueUser;
    private String mQueuePass;

    private boolean setQueueUrl(String url)
    {
        mQueueUrl = url;
        return true;
    }
    private boolean setQueueUser(String user)
    {
        mQueueUser = user;
        return true;
    }
    private boolean setQueuePass(String password)
    {
        mQueuePass = password;
        return true;
    }

    // Temporary fake queue
    List<Message> tempQueue = new ArrayList<>();
    int currentMsg = 0;

    private void connectToQueue()
    {
        tempQueue.add(new AddPhotoMessage(0, 3, "img/training/0/0.jpg"));
        tempQueue.add(new AddPhotoMessage(1, 3, "img/training/0/1.jpg"));
        Set<Integer> photos1 = new HashSet<>();
        photos1.add(0);
        tempQueue.add(new ProcessVideoMessage(11, "img/video/1.mp4", photos1));
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
        if (args.length != 3)
            printUsage();

        FaceDaemon daemon = new FaceDaemon();

        if (!daemon.setQueueUrl(args[0]))
            printUsage();

        if (!daemon.setQueueUser(args[1]))
            printUsage();

        if (!daemon.setQueuePass(args[2]))
            printUsage();

        try
        {
            daemon.connectToQueue();
        }
        catch (Exception e) // TODO: Make this more specific when we know what exceptions can be thrown
        {
        }

        daemon.run();
    }

    public static void printUsage()
    {
        System.err.println("Background daemon for \"Who's at my Party?\" face detection.");
        System.err.println("Takes 3 arguments: <QueueUrl> <QueueUser> <QueuePass>");
        Runtime.getRuntime().exit(1);
    }
}
