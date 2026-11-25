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
        Path gradleKtsBuild = projectDir.resolve("build.gradle.kts");

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
            } catch (IOException e) {
                model.addDetectionWarning("Could not read pom.xml: " + e.getMessage());
            }
        }

        for (Path gradleFile : new Path[]{gradleBuild, gradleKtsBuild}) {
          if (Files.exists(gradleFile)) {
            try {
                String content = Files.readString(gradleFile);
                if (content.contains("spring-boot") || content.contains("org.springframework.boot")) {
                    model.setLanguage("Java");
                    model.setFramework("Spring Boot");
                    model.setBuildTool("Gradle");
                    model.setTestFramework("JUnit");
                    model.setPackageManager("Gradle");
                    model.setArtifactType("JAR");
                    return true;
                }
            } catch (IOException e) {
                model.addDetectionWarning("Could not read " + gradleFile.getFileName() + ": " + e.getMessage());
            }
          }
        }

        return false;
    }
}
