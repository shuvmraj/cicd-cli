package cli.generator;

import cli.model.ProjectModel;
import java.io.IOException;
import java.util.Map;

/**
 * Generates GitHub Actions workflows.
 */
public class GitHubGenerator extends BaseGenerator {
    @Override
    public String generate(ProjectModel project) {
        try {
            Map<String, Object> context = buildContext(project);
            return templateEngine.render("/templates/github/actions-template.yml", context);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate GitHub Actions pipeline: " + e.getMessage(), e);
        }
    }
}
