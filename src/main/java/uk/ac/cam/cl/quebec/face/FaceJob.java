package uk.ac.cam.cl.quebec.face;

import java.util.Set;

/**
 * Encapsulates the notion of a job to be performed by the face recognition engine
 */
public class FaceJob
{
    private String videoFileNameInS3;
    private Set<String> imagesToMatchAgainstInS3;
    
    private String videoFileName;
    private Set<String> imagesToMatchAgainst;
}
