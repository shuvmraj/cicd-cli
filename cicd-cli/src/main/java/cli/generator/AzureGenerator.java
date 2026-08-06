package cli.generator;

import cli.model.ProjectModel;
import java.io.IOException;
import java.util.Map;

/**
 * Generates Azure DevOps Pipelines configurations.
 */
public class AzureGenerator extends BaseGenerator {
    @Override
    public String generate(ProjectModel project) {
        try {
            Map<String, Object> context = buildContext(project);
            return templateEngine.render("/templates/azure/azure-pipelines-template.yml", context);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Azure DevOps pipeline: " + e.getMessage(), e);
        }
    }
}
