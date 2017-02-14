package uk.ac.cam.cl.quebec.face;

import uk.ac.cam.cl.quebec.face.messages.AddPhotoMessage;
import uk.ac.cam.cl.quebec.face.messages.ProcessVideoMessage;

/**
 * Downloads video and image files from s3 to temporary storage, for processing
 */
public class S3AssetDownloader
{
    public static String downloadVideo(ProcessVideoMessage msg)
    {
        return msg.getS3FilePath();
    }

    public static String downloadImage(AddPhotoMessage msg)
    {
        return msg.getS3FilePath();
    }
}
