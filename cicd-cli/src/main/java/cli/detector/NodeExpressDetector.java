package cli.detector;

import cli.model.ProjectModel;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Detects Node/Express applications.
 */
public class NodeExpressDetector implements ProjectDetector {
    @Override
    public boolean detect(Path projectDir, ProjectModel model) {
        try {
            if (DetectorUtils.hasDependency(projectDir, "express")) {
                DetectorUtils.configureNodeProject(projectDir, model);
                model.setFramework("Express");
                model.setTestFramework("Jest");
                model.setArtifactType("DIST");
                return true;
            }
        } catch (IOException e) {
            model.addDetectionWarning("Could not read package.json: " + e.getMessage());
        }
        return false;
    }
}
