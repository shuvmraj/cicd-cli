package cli.detector;

import cli.model.ProjectModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Detects HTML/CSS/JS static applications.
 */
public class HtmlDetector implements ProjectDetector {
    @Override
    public boolean detect(Path projectDir, ProjectModel model) {
        // Only run HTML detection if no package.json or pom.xml or manage.py is present
        // (to prevent misdetecting React/Vue/Spring/Django as generic HTML)
        if (Files.exists(projectDir.resolve("package.json")) ||
            Files.exists(projectDir.resolve("pom.xml")) ||
            Files.exists(projectDir.resolve("build.gradle")) ||
            Files.exists(projectDir.resolve("manage.py")) ||
            Files.exists(projectDir.resolve("composer.json")) ||
            Files.exists(projectDir.resolve("artisan"))) {
            return false;
        }

        Path indexHtml = projectDir.resolve("index.html");
        if (Files.exists(indexHtml)) {
            setHtmlModel(model);
            return true;
        }

        // Check if there are any .html files in the root
        try (Stream<Path> list = Files.list(projectDir)) {
            boolean hasHtml = list.anyMatch(path -> path.toString().endsWith(".html"));
            if (hasHtml) {
                setHtmlModel(model);
                return true;
            }
        } catch (IOException ignored) {}

        return false;
    }

    private void setHtmlModel(ProjectModel model) {
        model.setLanguage("HTML/CSS/JavaScript");
        model.setFramework("HTML/CSS/JavaScript");
        model.setBuildTool("None");
        model.setTestFramework("None");
        model.setPackageManager("None");
        model.setArtifactType("STATIC");
    }
}
