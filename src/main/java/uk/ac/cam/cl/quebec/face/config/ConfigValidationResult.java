package uk.ac.cam.cl.quebec.face.config;

import java.lang.reflect.Field;
import java.util.Comparator;

/**
 * Class encapsulating a non-successful result from validation
 */
public class ConfigValidationResult {
    public enum Severity {
        INFO,
        WARN,
        ERROR,
        CRIT
    }

    public final Severity severity;
    public final Field affectedField;
    public final String message;

    public ConfigValidationResult(Severity severity, Field field, String message) {
        this.severity = severity;
        this.affectedField = field;
        this.message = message;
    }

    @Override
    public String toString() {
        String result = "";

        switch (severity) {
            case CRIT:
                result += "Critical";
                break;
            case ERROR:
                result += "Error";
                break;
            case WARN:
                result += "Warning";
                break;
            case INFO:
                result += "Info";
        }
        result += ": ";
        result += affectedField.getName();
        result += " - ";
        result += message;
        return result;
    }

    public static class SeverityRankingComparator implements Comparator<ConfigValidationResult> {
        public int compare(ConfigValidationResult o1, ConfigValidationResult o2) {
            return getRank(o1.severity) - getRank(o2.severity);
        }

        private int getRank(ConfigValidationResult.Severity severity) {
            switch (severity) {
                case CRIT:
                    return 0;
                case ERROR:
                    return 1;
                case WARN:
                    return 2;
                case INFO:
                    return 3;
                default:
                    throw new IllegalArgumentException("Not a severity: " + severity.toString());
            }
        }
    }
}
