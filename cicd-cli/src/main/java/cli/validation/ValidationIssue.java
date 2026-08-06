package cli.validation;

/**
 * Models a single validation finding (error, warning, info) produced by a rule.
 */
public class ValidationIssue {
    private final Severity severity;
    private final String message;
    private final String ruleName;

    public ValidationIssue(Severity severity, String message, String ruleName) {
        this.severity = severity;
        this.message = message;
        this.ruleName = ruleName;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public String getRuleName() {
        return ruleName;
    }

    @Override
    public String toString() {
        return "[" + severity + "] (" + ruleName + ") - " + message;
    }
}
