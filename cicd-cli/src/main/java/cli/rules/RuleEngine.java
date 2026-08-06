package cli.rules;

import cli.model.ProjectModel;
import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.validation.Severity;
import cli.validation.ValidationIssue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared Rule Engine executing individual validation rules.
 */
public class RuleEngine {
    private final List<Rule> rules;

    public RuleEngine() {
        rules = new ArrayList<>();
        rules.add(new BuildStageRule());
        rules.add(new TestingStageRule());
        rules.add(new DockerRule());
        rules.add(new SecretsRule());
        rules.add(new ArtifactsRule());
        rules.add(new DependenciesRule());
        rules.add(new DeploymentRule());
    }

    /**
     * Evaluates all rules against the project context and parsed pipeline.
     */
    public List<ValidationIssue> evaluate(ProjectModel project, ParsedPipeline pipeline) {
        List<ValidationIssue> issues = new ArrayList<>();

        // 1. Convert Parser-level Syntax Errors/Warnings
        for (String err : pipeline.getErrors()) {
            issues.add(new ValidationIssue(Severity.ERROR, "Syntax/Structure: " + err, "Parser"));
        }
        for (String warn : pipeline.getWarnings()) {
            issues.add(new ValidationIssue(Severity.WARNING, "Syntax/Structure: " + warn, "Parser"));
        }

        // 2. Structural Checks: Duplicate stages
        Set<String> stageNames = new HashSet<>();
        for (PipelineStage stage : pipeline.getStages()) {
            String name = stage.getName().trim().toLowerCase();
            if (stageNames.contains(name)) {
                issues.add(new ValidationIssue(
                        Severity.ERROR,
                        "Duplicate stage name detected: '" + stage.getName() + "'. Each stage must have a unique identifier.",
                        "StructureRule"
                ));
            } else {
                stageNames.add(name);
            }
        }

        // 3. Structural Checks: Cache
        if (!pipeline.isHasCache() && !"HTML/CSS/JavaScript".equals(project.getFramework())) {
            issues.add(new ValidationIssue(
                    Severity.WARNING,
                    "Cache not configured. Adding dependency caching speeds up build durations significantly.",
                    "CacheRule"
            ));
        }

        // 4. Run Modular Rules
        for (Rule rule : rules) {
            try {
                List<ValidationIssue> ruleIssues = rule.evaluate(project, pipeline);
                if (ruleIssues != null) {
                    issues.addAll(ruleIssues);
                }
            } catch (Exception e) {
                issues.add(new ValidationIssue(
                        Severity.ERROR,
                        "Rule '" + rule.getName() + "' crashed during evaluation: " + e.getMessage(),
                        rule.getName()
                ));
            }
        }

        return issues;
    }
}
