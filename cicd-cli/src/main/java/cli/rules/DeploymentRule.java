package cli.rules;

import cli.model.ProjectModel;
import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.validation.Severity;
import cli.validation.ValidationIssue;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates deployment/delivery configurations (kubectl, helm, etc.) in the pipeline.
 */
public class DeploymentRule implements Rule {
    @Override
    public String getName() {
        return "DeploymentRule";
    }

    @Override
    public String getDescription() {
        return "Verifies that the pipeline contains a deployment stage matching the project's target environment.";
    }

    @Override
    public List<ValidationIssue> evaluate(ProjectModel project, ParsedPipeline pipeline) {
        List<ValidationIssue> issues = new ArrayList<>();
        String deploymentTarget = project.getDeploymentTarget();

        // 1. Locate deploy stage
        PipelineStage deployStage = null;
        for (PipelineStage stage : pipeline.getStages()) {
            String name = stage.getName().toLowerCase();
            if (name.contains("deploy") || name.contains("publish") || name.contains("release") || name.contains("cd")) {
                deployStage = stage;
                break;
            }
        }

        if (deployStage == null) {
            issues.add(new ValidationIssue(
                    Severity.WARNING,
                    "Deployment stage is missing. Continuous Delivery / Deployment is recommended.",
                    getName()
            ));
            return issues;
        }

        // 2. Validate commands based on target deployment
        if (deployStage.getCommands().isEmpty()) {
            issues.add(new ValidationIssue(
                    Severity.ERROR,
                    "Deployment stage '" + deployStage.getName() + "' is empty.",
                    getName()
            ));
            return issues;
        }

        if ("Kubernetes".equalsIgnoreCase(deploymentTarget)) {
            boolean hasK8sCmd = false;
            for (String cmd : deployStage.getCommands()) {
                String c = cmd.toLowerCase();
                if (c.contains("kubectl") || c.contains("helm") || c.contains("k8s")) {
                    hasK8sCmd = true;
                    break;
                }
            }
            if (!hasK8sCmd) {
                issues.add(new ValidationIssue(
                        Severity.ERROR,
                        "Kubernetes deployment target is configured, but no kubectl or helm deploy commands were found in stage '" + deployStage.getName() + "'.",
                        getName()
                ));
            }
        } else if ("Docker".equalsIgnoreCase(deploymentTarget)) {
            boolean hasDockerCmd = false;
            for (String cmd : deployStage.getCommands()) {
                String c = cmd.toLowerCase();
                if (c.contains("docker run") || c.contains("docker-compose up") || c.contains("docker stack") || c.contains("docker service")) {
                    hasDockerCmd = true;
                    break;
                }
            }
            if (!hasDockerCmd) {
                issues.add(new ValidationIssue(
                        Severity.WARNING,
                        "Docker deployment target is configured, but no docker run or docker-compose execution commands were found in stage '" + deployStage.getName() + "'.",
                        getName()
                ));
            }
        }

        return issues;
    }
}
