package cli.detector;

import cli.model.ProjectModel;
import java.nio.file.Path;

/**
 * Interface for tech stack detectors.
 */
public interface ProjectDetector {
    /**
     * Inspects the project directory and updates the ProjectModel if the stack is detected.
     * @param projectDir the root path of the project
     * @param model the model to update
     * @return true if the detector successfully identified its corresponding technology stack, false otherwise.
     */
    boolean detect(Path projectDir, ProjectModel model);
}
