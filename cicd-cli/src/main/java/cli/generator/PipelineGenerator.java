package cli.generator;

import cli.model.ProjectModel;

/**
 * Strategy interface for generating platform-specific CI/CD pipelines.
 */
public interface PipelineGenerator {
    /**
     * Generates pipeline configuration content from a project model.
     */
    String generate(ProjectModel project);
}
