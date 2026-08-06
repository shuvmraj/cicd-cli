package cli.generator;

import cli.model.ProjectModel;
import java.io.IOException;
import java.util.Map;

/**
 * Generates Jenkinsfiles.
 */
public class JenkinsGenerator extends BaseGenerator {
    @Override
    public String generate(ProjectModel project) {
        try {
            Map<String, Object> context = buildContext(project);
            return templateEngine.render("/templates/jenkins/Jenkinsfile.template", context);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Jenkinsfile: " + e.getMessage(), e);
        }
    }
}
