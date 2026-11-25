package cli.detector;

import cli.model.ProjectModel;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Detects Angular applications.
 */
public class AngularDetector implements ProjectDetector {
    @Override
    public boolean detect(Path projectDir, ProjectModel model) {
        try {
            if (DetectorUtils.hasDependency(projectDir, "@angular/core")) {
                DetectorUtils.configureNodeProject(projectDir, model);
                model.setFramework("Angular");
                model.setTestFramework("Karma/Protractor");
                model.setArtifactType("DIST");
                return true;
            }
        } catch (IOException e) {
            model.addDetectionWarning("Could not read package.json: " + e.getMessage());
        }
        return false;
    }
}
