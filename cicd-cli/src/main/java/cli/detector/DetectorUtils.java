package cli.detector;

import cli.model.ProjectModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class DetectorUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DetectorUtils() {}

    static boolean hasDependency(Path projectDir, String dependency) throws IOException {
        Path packageJson = projectDir.resolve("package.json");
        if (!Files.isRegularFile(packageJson)) {
            return false;
        }
        JsonNode root = MAPPER.readTree(packageJson.toFile());
        return hasDependencyIn(root, "dependencies", dependency)
                || hasDependencyIn(root, "devDependencies", dependency)
                || hasDependencyIn(root, "peerDependencies", dependency);
    }

    static void configureNodeProject(Path projectDir, ProjectModel model) {
        model.setLanguage(Files.exists(projectDir.resolve("tsconfig.json")) ? "TypeScript" : "JavaScript");
        List<String> lockfiles = new ArrayList<>();
        if (Files.exists(projectDir.resolve("pnpm-lock.yaml"))) lockfiles.add("pnpm");
        if (Files.exists(projectDir.resolve("yarn.lock"))) lockfiles.add("yarn");
        if (Files.exists(projectDir.resolve("package-lock.json"))
                || Files.exists(projectDir.resolve("npm-shrinkwrap.json"))) lockfiles.add("npm");

        String manager = lockfiles.isEmpty() ? "npm" : lockfiles.get(0);
        if (lockfiles.size() > 1) {
            model.addDetectionWarning("Multiple Node package-manager lockfiles found ("
                    + String.join(", ", lockfiles) + "); using " + manager + ".");
        }
        model.setBuildTool(manager);
        model.setPackageManager(manager);
    }

    private static boolean hasDependencyIn(JsonNode root, String section, String dependency) {
        JsonNode dependencies = root == null ? null : root.get(section);
        return dependencies != null && dependencies.has(dependency);
    }
}
