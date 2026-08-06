package cli.validation;

import cli.model.ProjectModel;
import cli.model.ParsedPipeline;
import cli.rules.RuleEngine;
import java.util.List;

/**
 * Validation Engine that coordinates the RuleEngine to validate pipelines.
 */
public class ValidationEngine {
    private final RuleEngine ruleEngine;

    public ValidationEngine() {
        this.ruleEngine = new RuleEngine();
    }

    /**
     * Performs pipeline validation against codebase context.
     * @param project the project model
     * @param pipeline the parsed pipeline
     * @return a ValidationReport containing findings and status
     */
    public ValidationReport validate(ProjectModel project, ParsedPipeline pipeline) {
        List<ValidationIssue> issues = ruleEngine.evaluate(project, pipeline);
        return new ValidationReport(issues);
    }
}
