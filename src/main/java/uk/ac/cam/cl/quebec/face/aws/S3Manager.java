package uk.ac.cam.cl.quebec.face.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import uk.ac.cam.cl.quebec.face.messages.TrainOnVideoMessage;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
/**
 * Downloads video and image files from s3 to temporary storage, for processing
 */
public class S3Manager {
    private List<File> tempFiles = new ArrayList<>();
    private final AmazonS3 s3;
    private static String BUCKET = "quebec-userfiles-mobilehub-1062763500";

    public S3Manager() {
        s3 = new AmazonS3Client(CredentialsManager.getCredentials());
        s3.setRegion(CredentialsManager.getRegion());
    }

    public String downloadVideo(ProcessVideoMessage msg) {
        File video = downloadS3File(msg.getS3Path());
        tempFiles.add(video);
        return video.getPath();
    }

    public String downloadImage(TrainOnVideoMessage msg) {
        File video = downloadS3File(msg.getS3Path());
        tempFiles.add(video);
        return video.getPath();
    }

    public void cleanupTempFiles() {
        tempFiles.forEach(File::delete);
    }

    public void uploadFile(File file, String name) {
        try {
            s3.putObject(BUCKET, name, file);
        } catch (AmazonClientException e) {
            handleException(e);
        }
    }

    private File downloadS3File(String url){


        File file = null;

        try {
            S3Object object = s3.getObject(new GetObjectRequest(BUCKET, url));
            // handle object.getObjectContent() somehow
            file = File.createTempFile("", "url");
            file.deleteOnExit();
            Files.copy(object.getObjectContent(), file.toPath());

        } catch (AmazonClientException e) {
            handleException(e);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Saving file failed");
        }

        return file;
    }


    private void handleException(AmazonServiceException ase) {
        System.out.println("Caught an AmazonServiceException, which means your request made it "
                + "to Amazon S3, but was rejected with an error response for some reason.");
        System.out.println("Error Message:    " + ase.getMessage());
        System.out.println("HTTP Status Code: " + ase.getStatusCode());
        System.out.println("AWS Error Code:   " + ase.getErrorCode());
        System.out.println("Error Type:       " + ase.getErrorType());
        System.out.println("Request ID:       " + ase.getRequestId());
    }

    private void handleException(AmazonClientException ace) {
        System.out.println("Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with S3, "
                + "such as not being able to access the network.");
        System.out.println("Error Message: " + ace.getMessage());
    }
}
