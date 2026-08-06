package cli.rules;

import cli.model.ProjectModel;
import cli.model.ParsedPipeline;
import cli.validation.ValidationIssue;
import java.util.List;

/**
 * Common interface for pipeline validation and evaluation rules.
 */
public interface Rule {
    /**
     * Gets the simple, unique name of the rule.
     */
    String getName();

    /**
     * Gets the description of what the rule evaluates.
     */
    String getDescription();

    /**
     * Evaluates a project model and a parsed pipeline, returning a list of validation issues.
     */
    List<ValidationIssue> evaluate(ProjectModel project, ParsedPipeline pipeline);
}
