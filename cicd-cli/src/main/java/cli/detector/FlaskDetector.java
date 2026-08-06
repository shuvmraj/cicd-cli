package cli.detector;

import cli.model.ProjectModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Detects Flask applications.
 */
public class FlaskDetector implements ProjectDetector {
    @Override
    public boolean detect(Path projectDir, ProjectModel model) {
        // Prevent overlap with Django
        if (Files.exists(projectDir.resolve("manage.py"))) {
            return false;
        }

        Path reqTxt = projectDir.resolve("requirements.txt");
        Path pyproject = projectDir.resolve("pyproject.toml");
        Path appPy = projectDir.resolve("app.py");

        boolean flaskFound = false;

        if (Files.exists(reqTxt)) {
            try {
                String content = Files.readString(reqTxt);
                if (content.toLowerCase().contains("flask")) {
                    flaskFound = true;
                }
            } catch (IOException ignored) {}
        }

        if (!flaskFound && Files.exists(pyproject)) {
            try {
                String content = Files.readString(pyproject);
                if (content.toLowerCase().contains("flask")) {
                    flaskFound = true;
                }
            } catch (IOException ignored) {}
        }

        if (!flaskFound && Files.exists(appPy)) {
            try {
                String content = Files.readString(appPy);
                if (content.contains("import Flask") || content.contains("from flask")) {
                    flaskFound = true;
                }
            } catch (IOException ignored) {}
        }

        if (flaskFound) {
            model.setLanguage("Python");
            model.setFramework("Flask");
            model.setTestFramework("pytest");
            model.setArtifactType("DIST");

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
