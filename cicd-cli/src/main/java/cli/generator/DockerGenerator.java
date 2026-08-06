package cli.generator;

import cli.model.ProjectModel;
import java.io.IOException;
import java.util.Map;

/**
 * Generates highly optimized Dockerfiles for various technology stacks.
 */
public class DockerGenerator extends BaseGenerator {

    @Override
    public String generate(ProjectModel project) {
        String templatePath = resolveDockerTemplate(project);
        try {
            Map<String, Object> context = buildContext(project);
            return templateEngine.render(templatePath, context);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Dockerfile: " + e.getMessage(), e);
        }
    }

    private String resolveDockerTemplate(ProjectModel project) {
        String framework = project.getFramework();
        if (framework == null) {
            return "/templates/docker/html.template";
        }

        switch (framework) {
            case "Spring Boot":
                return "/templates/docker/springboot.template";
            case "React":
            case "Angular":
            case "Vue":
            case "Next.js":
                return "/templates/docker/react.template";
            case "Express":
                return "/templates/docker/node.template";
            case "Django":
            case "Flask":
                return "/templates/docker/python.template";
            case "Laravel":
                return "/templates/docker/php.template";
            case "ASP.NET Core":
                return "/templates/docker/dotnet.template";
            case "HTML/CSS/JavaScript":
            default:
                return "/templates/docker/html.template";
        }
    }
}
