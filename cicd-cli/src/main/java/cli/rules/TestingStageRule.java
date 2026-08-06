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

        // 1. Locate test stage
        PipelineStage testStage = null;
        for (PipelineStage stage : pipeline.getStages()) {
            String name = stage.getName().toLowerCase();
            if (name.contains("test") || name.contains("spec") || name.contains("verify") || name.contains("lint")) {
                testStage = stage;
                break;
            }
        }

        if (testStage == null) {
            if ("None".equals(testFramework)) {
                issues.add(new ValidationIssue(
                        Severity.INFO,
                        "No test framework configured for static website; skipping test stage requirement.",
                        getName()
                ));
            } else {
                issues.add(new ValidationIssue(
                        Severity.ERROR,
                        "Testing stage is missing. A production-ready pipeline must run unit/integration tests.",
                        getName()
                ));
            }
            return issues;
        }

        // 2. Validate commands
        if (testStage.getCommands().isEmpty()) {
            issues.add(new ValidationIssue(
                    Severity.ERROR,
                    "Testing stage '" + testStage.getName() + "' is empty. Add execution commands.",
                    getName()
            ));
            return issues;
        }

        // Check if commands match expected test executors
        boolean hasTestCmd = false;
        String expectedKeyword = getExpectedTestKeyword(testFramework);

        if (expectedKeyword == null) {
            return issues;
        }

        for (String cmd : testStage.getCommands()) {
            if (cmd.toLowerCase().contains(expectedKeyword.toLowerCase()) || cmd.toLowerCase().contains("test")) {
                hasTestCmd = true;
                break;
            }
        }

        if (!hasTestCmd) {
            issues.add(new ValidationIssue(
                    Severity.WARNING,
                    "Testing stage is configured, but no execution command matches the project's test framework: " + testFramework + " (expected: '" + expectedKeyword + "').",
                    getName()
            ));
        }

        return issues;
    }

    private String getExpectedTestKeyword(String testFramework) {
        if (testFramework == null) return null;
        switch (testFramework.toLowerCase()) {
            case "junit": return "test"; // maven/gradle test
            case "jest": return "test";
            case "vitest": return "vitest";
            case "karma/protractor": return "test";
            case "pytest": return "pytest";
            case "phpunit": return "phpunit";
            case "xunit": return "test"; // dotnet test
            default: return "test";
        }
    }
}
