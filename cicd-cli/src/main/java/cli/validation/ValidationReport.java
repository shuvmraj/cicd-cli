package cli.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Report containing results of pipeline validation.
 */
public class ValidationReport {
    private final List<ValidationIssue> issues;
    private final int errorsCount;
    private final int warningsCount;
    private final int infosCount;
    private final String status; // PASSED or FAILED

    public ValidationReport(List<ValidationIssue> issues) {
        this.issues = issues != null ? issues : new ArrayList<>();
        
        int errs = 0;
        int warns = 0;
        int infos = 0;
        for (ValidationIssue issue : this.issues) {
            if (issue.getSeverity() == Severity.ERROR) {
                errs++;
            } else if (issue.getSeverity() == Severity.WARNING) {
                warns++;
            } else if (issue.getSeverity() == Severity.INFO) {
                infos++;
            }
        }
        
        this.errorsCount = errs;
        this.warningsCount = warns;
        this.infosCount = infos;
        this.status = errs > 0 ? "FAILED" : "PASSED";
    }

    public List<ValidationIssue> getIssues() {
        return issues;
    }

    public int getErrorsCount() {
        return errorsCount;
    }

    public int getWarningsCount() {
        return warningsCount;
    }

    public int getInfosCount() {
        return infosCount;
    }

    public String getStatus() {
        return status;
    }
}
