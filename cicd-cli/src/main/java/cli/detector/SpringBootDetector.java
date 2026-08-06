package cli.detector;

import cli.model.ProjectModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Detects Spring Boot applications.
 */
public class SpringBootDetector implements ProjectDetector {
    @Override
    public boolean detect(Path projectDir, ProjectModel model) {
        Path pomXml = projectDir.resolve("pom.xml");
        Path gradleBuild = projectDir.resolve("build.gradle");

        if (Files.exists(pomXml)) {
            try {
                String content = Files.readString(pomXml);
                if (content.contains("spring-boot")) {
                    model.setLanguage("Java");
                    model.setFramework("Spring Boot");
                    model.setBuildTool("Maven");
                    model.setTestFramework("JUnit");
                    model.setPackageManager("Maven");
                    model.setArtifactType("JAR");
                    return true;
                }
            } catch (IOException ignored) {}
        }

        if (Files.exists(gradleBuild)) {
            try {
                String content = Files.readString(gradleBuild);
                if (content.contains("spring-boot") || content.contains("org.springframework.boot")) {
                    model.setLanguage("Java");
                    model.setFramework("Spring Boot");
                    model.setBuildTool("Gradle");
                    model.setTestFramework("JUnit");
                    model.setPackageManager("Gradle");
                    model.setArtifactType("JAR");
                    return true;
                }
            } catch (IOException ignored) {}
        }

        return false;
    }
}
