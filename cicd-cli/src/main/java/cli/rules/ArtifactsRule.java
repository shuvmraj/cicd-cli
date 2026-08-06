package cli.rules;

import cli.model.ProjectModel;
import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.validation.Severity;
import cli.validation.ValidationIssue;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates that build outputs (JAR, WAR, DIST, STATIC) are saved/archived in the pipeline.
 */
public class ArtifactsRule implements Rule {
    @Override
    public String getName() {
        return "ArtifactsRule";
    }

    @Override
    public String getDescription() {
        return "Verifies that production artifacts are saved or archived for deployment steps.";
    }

    @Override
    public List<ValidationIssue> evaluate(ProjectModel project, ParsedPipeline pipeline) {
        List<ValidationIssue> issues = new ArrayList<>();
        String artifactType = project.getArtifactType();

        if ("None".equals(artifactType) || artifactType == null) {
            return issues;
        }

        // Scan stages for artifact upload/archival indicators
        boolean artifactsArchived = false;
        
        // Let's check for standard upload tasks or keywords in commands
        for (PipelineStage stage : pipeline.getStages()) {
            for (String cmd : stage.getCommands()) {
                String lowerCmd = cmd.toLowerCase();
                if (lowerCmd.contains("upload-artifact") || 
                    lowerCmd.contains("archiveartifacts") || 
                    lowerCmd.contains("publishbuildartifacts") || 
                    lowerCmd.contains("publishpipelineartifact") ||
                    lowerCmd.contains("artifacts:") || // GitLab block parsed inside command list
                    lowerCmd.contains("tar -czf") || 
                    lowerCmd.contains("zip -r")) {
                    artifactsArchived = true;
                    break;
                }
            }
            if (artifactsArchived) break;
        }

        // Or if the pipeline model config hasCache set, but we specifically need build artifact storage
        if (!artifactsArchived) {
            issues.add(new ValidationIssue(
                    Severity.WARNING,
                    "Project outputs artifact type: '" + artifactType + "', but no artifact archiving step (e.g. actions/upload-artifact, archiveArtifacts, or tar/zip packaging) was detected. Build outputs may be lost.",
                    getName()
            ));
        } else {
            issues.add(new ValidationIssue(
                    Severity.INFO,
                    "Pipeline archives build outputs correctly.",
                    getName()
            ));
        }

        return issues;
    }
}
