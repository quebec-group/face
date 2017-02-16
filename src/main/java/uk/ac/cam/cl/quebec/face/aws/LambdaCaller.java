package uk.ac.cam.cl.quebec.face.aws;

import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.invoke.LambdaFunction;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;

public class LambdaCaller {

    public interface VideoCallbackService {
        @LambdaFunction(functionName="videoCallback")
        VideoCallbackOutput run(VideoCallbackInput input);

    }

    public static void main(String[] args) {
        AWSLambdaClient client = new AWSLambdaClient(CredentialsManager.getCredentials());
        client.setRegion(CredentialsManager.getRegion());

        VideoCallbackService service = LambdaInvokerFactory.build(VideoCallbackService.class,
                client);
        VideoCallbackInput request = new VideoCallbackInput();
        VideoCallbackOutput result = service.run(request);
        System.out.println(result);
    }

}
