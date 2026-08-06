package cli.detector;

import cli.model.ProjectModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Detects Laravel applications.
 */
public class LaravelDetector implements ProjectDetector {
    @Override
    public boolean detect(Path projectDir, ProjectModel model) {
        Path artisan = projectDir.resolve("artisan");
        Path composerJson = projectDir.resolve("composer.json");

        boolean laravelFound = false;

        if (Files.exists(artisan)) {
            laravelFound = true;
        }

        if (!laravelFound && Files.exists(composerJson)) {
            try {
                String content = Files.readString(composerJson);
                if (content.toLowerCase().contains("laravel/framework")) {
                    laravelFound = true;
                }
            } catch (IOException ignored) {}
        }

        if (laravelFound) {
            model.setLanguage("PHP");
            model.setFramework("Laravel");
            model.setBuildTool("composer");
            model.setTestFramework("PHPUnit");
            model.setPackageManager("composer");
            model.setArtifactType("DIST");
            return true;
        }

        return false;
    }
}
