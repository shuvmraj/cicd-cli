package cli.detector;

import cli.model.ProjectModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Detects React applications.
 */
public class ReactDetector implements ProjectDetector {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean detect(Path projectDir, ProjectModel model) {
        Path packageJson = projectDir.resolve("package.json");
        if (Files.exists(packageJson)) {
            try {
                JsonNode root = mapper.readTree(packageJson.toFile());
                JsonNode dependencies = root.get("dependencies");
                // Check if react is a dependency and NOT next (which would be handled by NextJsDetector)
                if (dependencies != null && dependencies.has("react") && !dependencies.has("next")) {
                    model.setLanguage("JavaScript");
                    model.setFramework("React");
                    model.setTestFramework("Jest");
                    model.setArtifactType("DIST");
                    
                    if (Files.exists(projectDir.resolve("pnpm-lock.yaml"))) {
                        model.setBuildTool("pnpm");
                        model.setPackageManager("pnpm");
                    } else if (Files.exists(projectDir.resolve("yarn.lock"))) {
                        model.setBuildTool("yarn");
                        model.setPackageManager("yarn");
                    } else {
                        model.setBuildTool("npm");
                        model.setPackageManager("npm");
                    }
                    return true;
                }
            } catch (IOException ignored) {}
        }
        return false;
    }
}
