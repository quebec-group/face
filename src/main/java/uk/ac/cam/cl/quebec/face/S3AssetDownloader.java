package uk.ac.cam.cl.quebec.face;

import uk.ac.cam.cl.quebec.face.messages.AddPhotoMessage;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Downloads video and image files from s3 to temporary storage, for processing
 */
public class S3AssetDownloader
{
    private List<String> tempFiles = new ArrayList<>();

    public String downloadVideo(ProcessVideoMessage msg) {
        return msg.getS3FilePath();
    }

    public String downloadImage(AddPhotoMessage msg) {
        return msg.getS3FilePath();
    }

    public void cleanupTempFiles() {
        tempFiles.forEach(file -> new File(file).delete());
    }
}
