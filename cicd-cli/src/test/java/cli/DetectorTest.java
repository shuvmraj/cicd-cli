package cli;

import cli.detector.DetectorService;
import cli.model.ProjectModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class DetectorTest {

    private final DetectorService detectorService = new DetectorService();
    private final Path samplesPath = Path.of("test/samples");
    @TempDir
    Path tempDir;

    @Test
    public void testReactDetection() {
        Path reactPath = samplesPath.resolve("react");
        ProjectModel model = detectorService.detect(reactPath);

        assertEquals("JavaScript", model.getLanguage());
        assertEquals("React", model.getFramework());
        assertEquals("npm", model.getBuildTool());
        assertEquals("Jest", model.getTestFramework());
        assertTrue(model.isDockerEnabled());
        assertFalse(model.isKubernetesEnabled());
        assertEquals("Docker", model.getDeploymentTarget());
        assertEquals("DIST", model.getArtifactType());
    }

    @Test
    public void testSpringBootDetection() {
        Path springPath = samplesPath.resolve("spring");
        ProjectModel model = detectorService.detect(springPath);

        assertEquals("Java", model.getLanguage());
        assertEquals("Spring Boot", model.getFramework());
        assertEquals("Maven", model.getBuildTool());
        assertEquals("JUnit", model.getTestFramework());
        assertTrue(model.isDockerEnabled());
        assertFalse(model.isKubernetesEnabled());
        assertEquals("Docker", model.getDeploymentTarget());
        assertEquals("JAR", model.getArtifactType());
    }

    @Test
    public void testDjangoDetection() {
        Path djangoPath = samplesPath.resolve("django");
        ProjectModel model = detectorService.detect(djangoPath);

        assertEquals("Python", model.getLanguage());
        assertEquals("Django", model.getFramework());
        assertEquals("pip", model.getBuildTool());
        assertEquals("pytest", model.getTestFramework());
        assertFalse(model.isDockerEnabled());
        assertEquals("Virtual Machine / Cloud Run", model.getDeploymentTarget());
    }

    @Test
    public void testNodeDetection() {
        Path nodePath = samplesPath.resolve("node");
        ProjectModel model = detectorService.detect(nodePath);

        assertEquals("JavaScript", model.getLanguage());
        assertEquals("Express", model.getFramework());
        assertEquals("npm", model.getBuildTool());
        assertEquals("Jest", model.getTestFramework());
    }

    @Test
    public void testHtmlDetection() {
        Path htmlPath = samplesPath.resolve("html");
        ProjectModel model = detectorService.detect(htmlPath);

        assertEquals("HTML/CSS/JavaScript", model.getLanguage());
        assertEquals("HTML/CSS/JavaScript", model.getFramework());
        assertEquals("None", model.getBuildTool());
        assertEquals("Static Hosting", model.getDeploymentTarget());
    }

    @Test
    public void testNodeDetectionUsesDevDependenciesTypescriptAndConfigOverrides() throws IOException {
        Files.writeString(tempDir.resolve("package.json"),
                "{\"devDependencies\":{\"react\":\"^18.0.0\"}}");
        Files.writeString(tempDir.resolve("tsconfig.json"), "{}");
        Files.writeString(tempDir.resolve("pnpm-lock.yaml"), "lockfileVersion: '9.0'");
        Files.writeString(tempDir.resolve("package-lock.json"), "{}");
        Files.writeString(tempDir.resolve(".cicd-config.json"),
                "{\"overrideLanguage\":\"TypeScript\",\"overrideFramework\":\"React App\","
                        + "\"customBuildCommand\":\"npm run custom-build\","
                        + "\"customTestCommand\":\"npm run custom-check\"}");

        ProjectModel model = detectorService.detect(tempDir);
        String generated = cli.generator.PipelineGeneratorFactory.getGenerator("github").generate(model);

        assertEquals("TypeScript", model.getLanguage());
        assertEquals("React App", model.getFramework());
        assertEquals("pnpm", model.getPackageManager());
        assertTrue(model.getDetectionWarnings().stream()
                .anyMatch(warning -> warning.contains("Multiple Node package-manager lockfiles")));
        assertTrue(generated.contains("npm run custom-build"));
        assertTrue(generated.contains("npm run custom-check"));
    }

    @Test
    public void testMalformedProjectConfigIsReported() throws IOException {
        Files.writeString(tempDir.resolve(".cicd-config.json"), "{\"overrideLanguage\":42}");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> detectorService.detect(tempDir));

        assertTrue(error.getMessage().contains("Could not load project configuration"));
    }
}
