package uk.ac.cam.cl.quebec.face.aws;

public class LambdaOutput {
    private boolean succeeded;
    private String errorMessage = "";

    public boolean getSucceeded() {
        return succeeded;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return super.toString() + "\n\tSucceeded? " + succeeded + "\n\tError message: " + errorMessage;
    }
}