package cli;

import cli.generator.PipelineGenerator;
import cli.generator.PipelineGeneratorFactory;
import cli.model.ProjectModel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TemplateEngineTest {

    @Test
    public void testGitHubGenerator() {
        ProjectModel project = new ProjectModel.Builder()
                .language("Java")
                .framework("Spring Boot")
                .buildTool("Maven")
                .testFramework("JUnit")
                .dockerEnabled(true)
                .kubernetesEnabled(true)
                .deploymentTarget("Kubernetes")
                .artifactType("JAR")
                .build();

        PipelineGenerator generator = PipelineGeneratorFactory.getGenerator("github");
        String pipeline = generator.generate(project);

        assertNotNull(pipeline);
        assertTrue(pipeline.contains("mvn clean package -DskipTests"));
        assertTrue(pipeline.contains("mvn test"));
        assertTrue(pipeline.contains("kubectl apply -f deployment.yaml"));
        assertTrue(pipeline.contains("docker-build:"));
    }

    @Test
    public void testGitLabGenerator() {
        ProjectModel project = new ProjectModel.Builder()
                .language("JavaScript")
                .framework("React")
                .buildTool("npm")
                .testFramework("Jest")
                .dockerEnabled(false)
                .deploymentTarget("Static Hosting")
                .artifactType("DIST")
                .build();

        PipelineGenerator generator = PipelineGeneratorFactory.getGenerator("gitlab");
        String pipeline = generator.generate(project);
        System.out.println("DEBUG GitLab Pipeline:\n" + pipeline);

        assertNotNull(pipeline);
        assertTrue(pipeline.contains("npm install && npm run build"));
        assertTrue(pipeline.contains("npm test"));
        assertFalse(pipeline.contains("docker-build-push:"));
    }
}
