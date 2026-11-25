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
            } catch (IOException e) {
                model.addDetectionWarning("Could not read requirements.txt: " + e.getMessage());
            }
        }

        if (!djangoFound && Files.exists(pyproject)) {
            try {
                String content = Files.readString(pyproject);
                if (content.toLowerCase().contains("django")) {
                    djangoFound = true;
                }
            } catch (IOException e) {
                model.addDetectionWarning("Could not read pyproject.toml: " + e.getMessage());
            }
        }

        if (djangoFound) {
            model.setLanguage("Python");
            model.setFramework("Django");
            model.setTestFramework("pytest");
            model.setArtifactType("DIST");
            
            // Detect python build systems
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
