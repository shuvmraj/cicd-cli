package cli.detector;

import cli.model.ProjectModel;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Detects Vue applications.
 */
public class VueDetector implements ProjectDetector {
    @Override
    public boolean detect(Path projectDir, ProjectModel model) {
        try {
            if (DetectorUtils.hasDependency(projectDir, "vue")) {
                DetectorUtils.configureNodeProject(projectDir, model);
                model.setFramework("Vue");
                model.setTestFramework("Vitest");
                model.setArtifactType("DIST");
                return true;
            }
        } catch (IOException e) {
            model.addDetectionWarning("Could not read package.json: " + e.getMessage());
        }
        return false;
    }
}
