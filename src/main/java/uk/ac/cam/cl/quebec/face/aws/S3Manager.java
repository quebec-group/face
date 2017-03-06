package uk.ac.cam.cl.quebec.face.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import uk.ac.cam.cl.quebec.face.config.Config;
import uk.ac.cam.cl.quebec.face.exceptions.AmazonException;
import uk.ac.cam.cl.quebec.face.exceptions.QuebecException;
import uk.ac.cam.cl.quebec.face.messages.S3DataHoldingMessage;
import uk.ac.cam.cl.quebec.face.storage.DirectoryStructure;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Downloads video and image files from s3 to temporary storage, for processing
 */
public class S3Manager {
    private List<File> tempFiles = new ArrayList<>();
    private final AmazonS3 s3;
    private Config config;

    public S3Manager(Config config) {
        this.config = config;
        s3 = new AmazonS3Client(CredentialsManager.getCredentials(config));
        s3.setRegion(CredentialsManager.getRegion());
    }

    public String downloadFile(S3DataHoldingMessage msg) throws QuebecException {
        File video = downloadS3File(msg.getS3Path());
        tempFiles.add(video);
        return video.getPath();
    }

    public void cleanupTempFiles() {
        tempFiles.forEach(File::delete);
    }

    public void uploadFile(File file, String name) {
        try {
            s3.putObject(config.S3Bucket, name, file);
        } catch (AmazonClientException e) {
            getMessageForException(e);
        }
    }

    private File downloadS3File(String url) throws QuebecException {
        try {
            S3Object object = s3.getObject(new GetObjectRequest(config.S3Bucket, url));
            File tmpDir = DirectoryStructure.getOrMakeTempDirectory(config);
            File file = File.createTempFile("s3tmp", "url", tmpDir);
            file.deleteOnExit();
            Files.copy(object.getObjectContent(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return file;
        } catch (AmazonClientException e) {
            throw new AmazonException(getMessageForException(e), e);
        } catch (IOException e) {
            throw new AmazonException("Saving file from S3 failed", e);
        }
    }


    private void getMessageForException(AmazonServiceException ase) {
        System.out.println("Caught an AmazonServiceException, which means your request made it "
                + "to Amazon S3, but was rejected with an error response for some reason.");
        System.out.println("Error Message:    " + ase.getMessage());
        System.out.println("HTTP Status Code: " + ase.getStatusCode());
        System.out.println("AWS Error Code:   " + ase.getErrorCode());
        System.out.println("Error Type:       " + ase.getErrorType());
        System.out.println("Request ID:       " + ase.getRequestId());
    }

    private String getMessageForException(AmazonClientException ace) {
        return Stream.of("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.",
                "Error Message: " + ace.getMessage())

                .collect(Collectors.joining(System.lineSeparator()));
    }
}
