package cli.detector;

import cli.model.ProjectModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Orchestrates tech stack detectors to build a normalized ProjectModel.
 */
public class DetectorService {
    private final List<ProjectDetector> detectors;

    public DetectorService() {
        detectors = new ArrayList<>();
        // Frontend
        detectors.add(new NextJsDetector());
        detectors.add(new ReactDetector());
        detectors.add(new AngularDetector());
        detectors.add(new VueDetector());
        
        // Backend
        detectors.add(new SpringBootDetector());
        detectors.add(new NodeExpressDetector());
        detectors.add(new DjangoDetector());
        detectors.add(new FlaskDetector());
        detectors.add(new LaravelDetector());
        detectors.add(new AspNetDetector());
        
        // Static HTML (run last as fallback)
        detectors.add(new HtmlDetector());
    }

    /**
     * Scans the project directory and returns a populated ProjectModel.
     */
    public ProjectModel detect(Path projectDir) {
        ProjectModel model = new ProjectModel();

        // 1. Detect Framework & Language
        boolean detected = false;
        for (ProjectDetector detector : detectors) {
            if (detector.detect(projectDir, model)) {
                detected = true;
                break;
            }
        }

        if (!detected) {
            // Fallback default
            model.setLanguage("Unknown");
            model.setFramework("Unknown");
            model.setBuildTool("None");
            model.setTestFramework("None");
            model.setPackageManager("None");
            model.setArtifactType("None");
        }

        // 2. Auxiliary Detection (Docker, Kubernetes)
        boolean dockerEnabled = Files.exists(projectDir.resolve("Dockerfile")) || 
                               Files.exists(projectDir.resolve("docker-compose.yml"));
        model.setDockerEnabled(dockerEnabled);

        boolean k8sEnabled = Files.exists(projectDir.resolve("deployment.yaml")) || 
                             Files.exists(projectDir.resolve("k8s"));
        if (!k8sEnabled) {
            // Scan for any yaml files in root containing kubernetes apiVersion
            try (Stream<Path> files = Files.list(projectDir)) {
                k8sEnabled = files.filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
                        .anyMatch(p -> {
                            try {
                                String content = Files.readString(p);
                                return content.contains("apiVersion:") && (content.contains("Deployment") || content.contains("Service"));
                            } catch (IOException e) {
                                return false;
                            }
                        });
            } catch (IOException ignored) {}
        }
        model.setKubernetesEnabled(k8sEnabled);

        // 3. Resolve Deployment Target
        if (k8sEnabled) {
            model.setDeploymentTarget("Kubernetes");
        } else if (dockerEnabled) {
            model.setDeploymentTarget("Docker");
        } else if ("React".equals(model.getFramework()) || 
                   "Angular".equals(model.getFramework()) || 
                   "Vue".equals(model.getFramework()) || 
                   "Next.js".equals(model.getFramework()) || 
                   "HTML/CSS/JavaScript".equals(model.getFramework())) {
            model.setDeploymentTarget("Static Hosting");
        } else {
            model.setDeploymentTarget("Virtual Machine / Cloud Run");
        }

        return model;
    }
}
