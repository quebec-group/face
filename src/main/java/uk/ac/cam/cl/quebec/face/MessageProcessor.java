package uk.ac.cam.cl.quebec.face;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import uk.ac.cam.cl.quebec.face.aws.EventProcessedLambdaInput;
import uk.ac.cam.cl.quebec.face.aws.LambdaCaller;
import uk.ac.cam.cl.quebec.face.aws.ProfileProcessedLambdaInput;
import uk.ac.cam.cl.quebec.face.config.Config;
import uk.ac.cam.cl.quebec.face.exceptions.StorageException;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
import uk.ac.cam.cl.quebec.face.messages.TrainOnVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;
import uk.ac.cam.cl.quebec.face.opencv.*;
import uk.ac.cam.cl.quebec.face.storage.DirectoryStructure;
import uk.ac.cam.cl.quebec.face.aws.S3Manager;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Central visitor which handles correct processing of all messages.
 */
public class MessageProcessor implements MessageVisitor
{
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private S3Manager s3Manager;
    private Config config;

    public MessageProcessor(Config config, S3Manager downloader) {
        this.config = config;
        s3Manager = downloader;
    }

    public void accept(TrainOnVideoMessage msg) throws QuebecException
    {
        System.err.println("Processing TrainOnVideoMessage: " + Integer.toString(msg.getVideoId()));

        Logging.getLogger().info(msg.toString());

        // Fetch video from S3
        String videoPath = s3Manager.downloadFile(msg);

        // Perform the training
        VideoTrainer trainer = new VideoTrainer(config,msg.getUserId(), videoPath);
        trainer.train();

        // Ping lambda to say we're done
        sendTrainingResultsToLambda(msg.getUserId(), msg.getVideoId());
    }

    public void accept(ProcessVideoMessage msg) throws QuebecException
    {
        System.err.println("Processing ProcessVideoMessage: " + Integer.toString(msg.getVideoId()));

        Logging.getLogger().info(msg.toString());

        // Fetch video from s3
        String videoFileName = s3Manager.downloadFile(msg);

        // Recognise people
        VideoRecogniser recogniser = new VideoRecogniser(config, msg.getRecognitionUserSet(), videoFileName);
        List<String> usersInVideo = recogniser.recognise();

        // Upload results of processing to Amazon
        sendEventResultsToLambda(msg.getEventId(), usersInVideo);
    }

    private String uploadProfilePhoto(String userId, Mat photo) throws StorageException {
        File tmpDir = DirectoryStructure.getOrMakeTempDirectory(config);
        File profilePhoto;
        try {
            profilePhoto = File.createTempFile("profile", ".jpg", tmpDir);
        }
        catch (IOException e) {
            throw new StorageException("Could not create temp file for profile photo", e);
        }

        Imgcodecs.imwrite(profilePhoto.getAbsolutePath(), photo);
        String s3FileName = "protected/" + userId + "/photos/profilePhoto.jpg";
        s3Manager.uploadFile(profilePhoto, s3FileName);

        profilePhoto.delete();
        return s3FileName;
    }

    private void sendTrainingResultsToLambda(String userId, int videoId) {
        ProfileProcessedLambdaInput results = new ProfileProcessedLambdaInput();
        results.setUserID(userId);
        results.setVideoID(videoId);
        LambdaCaller c = new LambdaCaller(config);
        c.callProfileProcessedLambda(results);
    }

    private void sendEventResultsToLambda(int eventId, List<String> usersInVideo) {
        EventProcessedLambdaInput results = new EventProcessedLambdaInput();
        results.setEventID(eventId);
        results.setMembers(usersInVideo);
        LambdaCaller c = new LambdaCaller(config);
        c.callEventProcessedLambda(results);
    }
}
