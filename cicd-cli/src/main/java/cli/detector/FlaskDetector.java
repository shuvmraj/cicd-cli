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
            } catch (IOException e) {
                model.addDetectionWarning("Could not read requirements.txt: " + e.getMessage());
            }
        }

        if (!flaskFound && Files.exists(pyproject)) {
            try {
                String content = Files.readString(pyproject);
                if (content.toLowerCase().contains("flask")) {
                    flaskFound = true;
                }
            } catch (IOException e) {
                model.addDetectionWarning("Could not read pyproject.toml: " + e.getMessage());
            }
        }

        if (!flaskFound && Files.exists(appPy)) {
            try {
                String content = Files.readString(appPy);
                if (content.contains("import Flask") || content.contains("from flask")) {
                    flaskFound = true;
                }
            } catch (IOException e) {
                model.addDetectionWarning("Could not read app.py: " + e.getMessage());
            }
        }

        if (flaskFound) {
            model.setLanguage("Python");
            model.setFramework("Flask");
            model.setTestFramework("pytest");
            model.setArtifactType("DIST");

            boolean usesPoetry = Files.exists(projectDir.resolve("poetry.lock"));
            if (!usesPoetry && Files.exists(pyproject)) {
                try {
                    usesPoetry = Files.readString(pyproject).toLowerCase().contains("[tool.poetry]");
                } catch (IOException e) {
                    model.addDetectionWarning("Could not inspect Poetry configuration: " + e.getMessage());
                }
            }
            if (usesPoetry) {
                model.setBuildTool("Poetry");
                model.setPackageManager("Poetry");
            } else if (Files.exists(reqTxt)) {
                model.setBuildTool("pip");
                model.setPackageManager("pip");
            } else {
                model.setBuildTool("None");
                model.setPackageManager("None");
                model.addDetectionWarning("Python build tool was not identified; set a custom build command in .cicd-config.json.");
            }
            return true;
        }

        return false;
    }
}
