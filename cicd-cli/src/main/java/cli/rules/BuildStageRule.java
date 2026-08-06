package cli.rules;

import cli.model.ProjectModel;
import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.validation.Severity;
import cli.validation.ValidationIssue;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates the presence and correctness of the build stage.
 */
public class BuildStageRule implements Rule {
    @Override
    public String getName() {
        return "BuildStageRule";
    }

    @Override
    public String getDescription() {
        return "Verifies that compile/bundle-based projects contain a valid build stage with appropriate build commands.";
    }

    @Override
    public List<ValidationIssue> evaluate(ProjectModel project, ParsedPipeline pipeline) {
        List<ValidationIssue> issues = new ArrayList<>();
        String framework = project.getFramework();
        String buildTool = project.getBuildTool();

        boolean needsBuild = !"HTML/CSS/JavaScript".equals(framework) && !"None".equals(buildTool);

        // Find stage matching "build" or "compile"
        PipelineStage buildStage = null;
        for (PipelineStage stage : pipeline.getStages()) {
            String name = stage.getName().toLowerCase();
            if (name.contains("build") || name.contains("compile") || name.contains("package") || name.contains("publish")) {
                buildStage = stage;
                break;
            }
        }

        if (buildStage == null) {
            if (needsBuild) {
                issues.add(new ValidationIssue(
                        Severity.ERROR,
                        "Build stage is missing. Codebase requires building with " + buildTool + ".",
                        getName()
                ));
            } else {
                issues.add(new ValidationIssue(
                        Severity.INFO,
                        "No build stage detected (not strictly required for static/HTML codebases).",
                        getName()
                ));
            }
            return issues;
        }

        // Build stage exists, validate commands
        if (buildStage.getCommands().isEmpty()) {
            issues.add(new ValidationIssue(
                    Severity.ERROR,
                    "Build stage '" + buildStage.getName() + "' is empty. Add build commands.",
                    getName()
            ));
            return issues;
        }

        // Verify correct build tool commands are executed
        boolean hasCorrectCommand = false;
        String expectedKeyword = getExpectedKeyword(buildTool);

        if (expectedKeyword == null) {
            // No strict build tool keyword check needed
            return issues;
        }

        for (String cmd : buildStage.getCommands()) {
            if (cmd.toLowerCase().contains(expectedKeyword.toLowerCase())) {
                hasCorrectCommand = true;
                break;
            }
        }

        if (!hasCorrectCommand) {
            issues.add(new ValidationIssue(
                    Severity.WARNING,
                    "Build stage has commands, but none contain expected keyword: '" + expectedKeyword + "' for build system " + buildTool + ".",
                    getName()
            ));
        }

        return issues;
    }

    private String getExpectedKeyword(String buildTool) {
        if (buildTool == null) return null;
        switch (buildTool.toLowerCase()) {
            case "maven": return "mvn";
            case "gradle": return "gradle";
            case "npm": return "npm";
            case "yarn": return "yarn";
            case "pnpm": return "pnpm";
            case "composer": return "composer";
            case "dotnet": return "dotnet";
            case "poetry": return "poetry";
            case "pip": return "pip";
            default: return null;
        }
    }
}
