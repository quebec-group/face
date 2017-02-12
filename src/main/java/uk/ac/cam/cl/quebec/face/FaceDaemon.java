package uk.ac.cam.cl.quebec.face;

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

    private void connectToQueue()
    {

    }

    private FaceJob getJobFromQueue()
    {
        return new FaceJob();
    }

    private void run()
    {
        while (true)
        {
            FaceJob job = getJobFromQueue();

            S3AssetDownloader.downloadVideo(job);
            // Maybe image download could be asynchronous - if
            // we have useful work to do on the video first
            S3AssetDownloader.downloadImages(job);

            // Process the images...
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
