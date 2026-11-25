package cli.rules;

import cli.model.ProjectModel;
import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.validation.Severity;
import cli.validation.ValidationIssue;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates Docker build/push configurations and image naming patterns.
 */
public class DockerRule implements Rule {
    private static final Pattern DOCKER_BUILD_TAG_PATTERN = Pattern.compile("docker\\s+build\\s+.*-t\\s+([^\\s]+)");
    // Docker image name format: lowercase letters, digits, _, -, . and /; tag can include uppercase
    private static final Pattern IMAGE_NAME_FORMAT_PATTERN = Pattern.compile("^([a-z0-9_.-]+(?:/[a-z0-9_.-]+)*):([a-zA-Z0-9_.-]+)$");

    @Override
    public String getName() {
        return "DockerRule";
    }

    @Override
    public String getDescription() {
        return "Checks container builder stages, verifies Docker image naming structures, and checks caching details.";
    }

    @Override
    public List<ValidationIssue> evaluate(ProjectModel project, ParsedPipeline pipeline) {
        List<ValidationIssue> issues = new ArrayList<>();

        boolean dockerStageExists = false;
        PipelineStage dockerStage = null;
        for (PipelineStage stage : pipeline.getStages()) {
            String name = (stage.getName() + " " + stage.getStageName()).toLowerCase();
            boolean usesDockerAction = stage.getSteps().stream()
                    .anyMatch(step -> step.getUses() != null
                            && step.getUses().toLowerCase().contains("docker/build-push-action"));
            if (name.contains("docker") || name.contains("image") || name.contains("container")
                    || name.contains("registry") || usesDockerAction) {
                dockerStageExists = true;
                dockerStage = stage;
                break;
            }
        }

        if (project.isDockerEnabled()) {
            if (!dockerStageExists) {
                issues.add(new ValidationIssue(
                        Severity.WARNING,
                        "Dockerfile or docker-compose detected in codebase, but no container build/push stage is declared in the pipeline.",
                        getName()
                ));
            } else {
                issues.add(new ValidationIssue(
                        Severity.INFO,
                        "Docker containerization detected and integration stage configured.",
                        getName()
                ));
            }
        }

        if (dockerStage != null) {
            boolean hasDockerBuild = false;
            boolean hasDockerPush = false;
            boolean usesCacheOption = false;

            for (PipelineStage stage : pipeline.getStages()) {
              for (String cmd : stage.getCommands()) {
                String normalized = cmd.toLowerCase();
                if (normalized.contains("docker build") || normalized.contains("build-push-action") || normalized.contains("kaniko")) {
                    hasDockerBuild = true;
                }
                if (normalized.contains("docker push") || normalized.contains("build-push-action") || normalized.contains("kaniko")) {
                    hasDockerPush = true;
                }
                if (normalized.contains("--cache-from") || normalized.contains("cache-to") || normalized.contains("cache=true")) {
                    usesCacheOption = true;
                }

                // Verify Docker image tag formats in CLI commands
                Matcher tagMatcher = DOCKER_BUILD_TAG_PATTERN.matcher(cmd);
                if (tagMatcher.find()) {
                    String tag = tagMatcher.group(1).replace("\"", "").replace("'", "");
                    // If tag contains variables like $VERSION or ${{ github.sha }}, skip strict regex validation
                    if (!tag.contains("$") && !tag.contains("{") && !tag.contains("}")) {
                        Matcher formatMatcher = IMAGE_NAME_FORMAT_PATTERN.matcher(tag);
                        if (!formatMatcher.matches()) {
                            issues.add(new ValidationIssue(
                                    Severity.ERROR,
                                    "Invalid Docker image naming format: '" + tag + "'. Names must be lowercase (can contain '/', '-', '_', '.'). Tags can be alphanumeric.",
                                    getName()
                            ));
                        }
                    }
                  }
                  for (var step : stage.getSteps()) {
                    String reference = step.getUses();
                    if (reference != null && reference.toLowerCase().contains("docker/build-push-action")) {
                        hasDockerBuild = true;
                        hasDockerPush = true;
                    }
                  }
                }
            }

            if (!hasDockerBuild) {
                issues.add(new ValidationIssue(
                        Severity.WARNING,
                        "Docker stage is configured, but no 'docker build' or action task found.",
                        getName()
                ));
            }

            if (!hasDockerPush && project.isDockerEnabled()) {
                issues.add(new ValidationIssue(
                        Severity.WARNING,
                        "Docker image built but no registry upload ('docker push') or build-and-push task detected.",
                        getName()
                ));
            }

            if (!usesCacheOption && hasDockerBuild) {
                issues.add(new ValidationIssue(
                        Severity.INFO,
                        "Docker build doesn't appear to leverage layer caching (e.g. --cache-from). Consider adding to optimize performance.",
                        getName()
                ));
            }
        }

        return issues;
    }
}
