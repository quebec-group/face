package uk.ac.cam.cl.quebec.face.config;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import uk.ac.cam.cl.quebec.face.config.ConfigValidationResult.Severity;

/**
 * Validates a Config object before it is used as configuration.
 */
public class ConfigValidator {
    private Config config;

    public ConfigValidator(Config config) {
        this.config = config;
    }
    public List<ConfigValidationResult> validate() {
        LinkedList<ConfigValidationResult> results = new LinkedList<>();

        results.addAll(checkStringsNotEmpty("QueueUrl", "StopFilePath", "DataDir", "TempFileDir"));

        return results;
    }

    private LinkedList<ConfigValidationResult> checkStringsNotEmpty(String... fieldNames) {
        LinkedList<ConfigValidationResult> results = new LinkedList<>();

        for (String name : fieldNames) {
            try {
                Field f = Config.class.getField(name);

                try {
                    if (f.get(config) == null || f.get(config).equals("")) {
                        results.add(new ConfigValidationResult(Severity.ERROR, f,
                                "Value cannot be empty."));
                    }
                }
                catch (IllegalAccessException iae) {
                    results.add(new ConfigValidationResult(Severity.CRIT, f,
                            "Validator cannot access field to validate."));
                }
            }
            catch (NoSuchFieldException nsfe) {
                results.add(new ConfigValidationResult(Severity.ERROR, null,
                        "Value does not exist, but cannot be empty."));
            }
        }

        return results;
    }
}
