package cli.detector;

import cli.model.ProjectModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Detects Django applications.
 */
public class DjangoDetector implements ProjectDetector {
    @Override
    public boolean detect(Path projectDir, ProjectModel model) {
        Path managePy = projectDir.resolve("manage.py");
        Path reqTxt = projectDir.resolve("requirements.txt");
        Path pyproject = projectDir.resolve("pyproject.toml");

        boolean djangoFound = false;

        if (Files.exists(managePy)) {
            djangoFound = true;
        }

        if (!djangoFound && Files.exists(reqTxt)) {
            try {
                String content = Files.readString(reqTxt);
                if (content.toLowerCase().contains("django")) {
                    djangoFound = true;
                }
            } catch (IOException ignored) {}
        }

        if (!djangoFound && Files.exists(pyproject)) {
            try {
                String content = Files.readString(pyproject);
                if (content.toLowerCase().contains("django")) {
                    djangoFound = true;
                }
            } catch (IOException ignored) {}
        }

        if (djangoFound) {
            model.setLanguage("Python");
            model.setFramework("Django");
            model.setTestFramework("pytest");
            model.setArtifactType("DIST");
            
            // Detect python build systems
            if (Files.exists(projectDir.resolve("poetry.lock")) || Files.exists(projectDir.resolve("pyproject.toml"))) {
                model.setBuildTool("Poetry");
                model.setPackageManager("Poetry");
            } else {
                model.setBuildTool("pip");
                model.setPackageManager("pip");
            }
            return true;
        }

        return false;
    }
}
