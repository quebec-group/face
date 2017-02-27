package uk.ac.cam.cl.quebec.face.aws;

import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.invoke.LambdaFunction;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;

public class LambdaCaller {
    private AWSLambdaClient client;
    private EventProcessedLambdaService eventService;
    private ProfileProcessedLambdaService profileService;


    private interface EventProcessedLambdaService {
        @LambdaFunction(functionName="eventProcessed")
        LambdaOutput run(EventProcessedLambdaInput input);
    }

    private interface ProfileProcessedLambdaService {
        @LambdaFunction(functionName="profileProcessed")
        LambdaOutput run(ProfileProcessedLambdaInput input);
    }


    public LambdaCaller() {
        client = new AWSLambdaClient(CredentialsManager.getCredentials());
        client.setRegion(CredentialsManager.getRegion());

        eventService = LambdaInvokerFactory.build(EventProcessedLambdaService.class, client);
        profileService = LambdaInvokerFactory.build(ProfileProcessedLambdaService.class, client);
    }

    public LambdaOutput callEventProcessedLambda(EventProcessedLambdaInput input) {
        return eventService.run(input);
    }

    public LambdaOutput callProfileProcessedLambda(ProfileProcessedLambdaInput input) {
        return profileService.run(input);
    }
}
