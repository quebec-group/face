package uk.ac.cam.cl.quebec.face.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import uk.ac.cam.cl.quebec.face.config.Config;

public class CredentialsManager {
    private static AWSCredentials credentials;
    private static String credentialsPath = "/Users/callum/Desktop/SqsCredentials.txt";
    private static Region region = Region.getRegion(Regions.EU_WEST_1);

    /*
     * Create your credentials file at credentialsPath
     * and save the following lines after replacing the underlined values with your own.
     *
     * [default]
     * aws_access_key_id = YOUR_ACCESS_KEY_ID
     * aws_secret_access_key = YOUR_SECRET_ACCESS_KEY
     */

    private CredentialsManager() {}

    static AWSCredentials getCredentials(Config config) {
        if (credentials == null) {
            try {
                credentials = new ProfileCredentialsProvider(config.AwsCredentialsFile,
                        "default").getCredentials();
            } catch (Exception e) {
                throw new AmazonClientException(
                        "Cannot load the credentials from the credential profiles file. " +
                                "Please make sure that your credentials file is at the correct " +
                                "location (~/.aws/credentials), and is in valid format.",
                        e);
            }
        }

        return credentials;
    }

    public static Region getRegion() {
        return region;
    }
}
