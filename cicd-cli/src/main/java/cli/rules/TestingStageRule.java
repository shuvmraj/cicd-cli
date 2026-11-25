package cli.rules;

import cli.model.ProjectModel;
import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.validation.Severity;
import cli.validation.ValidationIssue;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates the presence of a testing stage and verifies test execution commands.
 */
public class TestingStageRule implements Rule {
    @Override
    public String getName() {
        return "TestingStageRule";
    }

    @Override
    public String getDescription() {
        return "Verifies that the pipeline includes a test execution stage with corresponding testing commands.";
    }

    @Override
    public List<ValidationIssue> evaluate(ProjectModel project, ParsedPipeline pipeline) {
        List<ValidationIssue> issues = new ArrayList<>();
        String testFramework = project.getTestFramework();
        if (testFramework == null || "none".equalsIgnoreCase(testFramework)) {
            issues.add(new ValidationIssue(
                    Severity.INFO,
                    "No test framework was detected; test-stage requirements were not evaluated.",
                    getName()
            ));
            return issues;
        }

        List<PipelineStage> testStages = new ArrayList<>();
        boolean hasTestCommand = false;
        for (PipelineStage stage : pipeline.getStages()) {
            String name = (stage.getName() + " " + stage.getStageName()).toLowerCase();
            if (name.contains("test") || name.contains("spec") || name.contains("verify")) {
                testStages.add(stage);
            }
            for (String command : stage.getCommands()) {
                if (matchesTestCommand(testFramework, command)) {
                    hasTestCommand = true;
                }
            }
        }

        if (!testStages.isEmpty()) {
            boolean emptyTestStage = testStages.stream().anyMatch(stage -> stage.getCommands().isEmpty());
            if (emptyTestStage) {
                issues.add(new ValidationIssue(
                        Severity.WARNING,
                        "A test/verify stage has no parsed shell commands; review action-based test steps manually.",
                        getName()
                ));
            }
        } else if (!hasTestCommand) {
            issues.add(new ValidationIssue(
                    Severity.ERROR,
                    "No test stage or recognizable test command was found for " + testFramework + ".",
                    getName()
            ));
            return issues;
        }

        if (!hasTestCommand) {
            issues.add(new ValidationIssue(
                    Severity.WARNING,
                    "A test-like stage exists, but no command matching test framework '"
                            + testFramework + "' was recognized.",
                    getName()
            ));
        }

        return issues;
    }

    private boolean matchesTestCommand(String testFramework, String command) {
        String text = command.toLowerCase();
        switch (testFramework.toLowerCase()) {
            case "junit":
                return (text.contains("mvn") || text.contains("gradle")) && containsWord(text, "test")
                        || (text.contains("mvn") && containsWord(text, "verify"));
            case "jest":
                return text.contains("jest") || isPackageManagerTestCommand(text);
            case "vitest":
                return text.contains("vitest") || isPackageManagerTestCommand(text);
            case "karma/protractor":
                return text.contains("karma") || text.contains("protractor")
                        || isPackageManagerTestCommand(text);
            case "pytest":
                return text.contains("pytest");
            case "phpunit":
                return text.contains("phpunit") || text.contains("composer test");
            case "xunit":
                return text.contains("dotnet test");
            default:
                return containsWord(text, "test");
        }
    }

    private boolean isPackageManagerTestCommand(String command) {
        return (command.contains("npm ") || command.contains("yarn ")
                || command.contains("pnpm ")) && containsWord(command, "test");
    }

    private boolean containsWord(String text, String word) {
        return text.matches("(?s).*\\b" + word + "\\b.*");
    }
}
