package cli;

import cli.detector.DetectorService;
import cli.model.ProjectModel;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class DetectorTest {

    private final DetectorService detectorService = new DetectorService();
    private final Path samplesPath = Path.of("test/samples");

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
}
