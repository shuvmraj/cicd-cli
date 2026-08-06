package cli.commands;

import cli.detector.DetectorService;
import cli.model.ProjectModel;
import cli.utils.OutputFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Subcommand to scan a codebase and output its ProjectModel details.
 */
@Command(
        name = "detect",
        description = "Scans project directories to identify language, frameworks, and deployment setups"
)
public class DetectCommand implements Runnable {

    @Option(names = {"-d", "--dir"}, description = "Target directory to scan")
    private String targetDir = ".";

    @Override
    public void run() {
        Path path = Path.of(targetDir).toAbsolutePath().normalize();
        
        System.out.println("\nScanning project at " + OutputFormatter.bold(path.toString()) + "...\n");

        if (!Files.exists(path)) {
            System.err.println(OutputFormatter.red("Error: Target directory does not exist."));
            return;
        }

        DetectorService detectorService = new DetectorService();
        ProjectModel model = detectorService.detect(path);

        // Recommend platform based on existing files or fallback
        String recommendedPlatform = "GitHub Actions (recommended)";
        if (Files.exists(path.resolve(".gitlab-ci.yml"))) {
            recommendedPlatform = "GitLab CI (detected)";
        } else if (Files.exists(path.resolve("Jenkinsfile"))) {
            recommendedPlatform = "Jenkins (detected)";
        } else if (Files.exists(path.resolve("azure-pipelines.yml"))) {
            recommendedPlatform = "Azure DevOps (detected)";
        } else if (Files.exists(path.resolve(".github"))) {
            recommendedPlatform = "GitHub Actions (detected)";
        }

        // Print details in premium style
        System.out.println(OutputFormatter.cyan("Framework: ") + model.getFramework());
        System.out.println(OutputFormatter.cyan("Language: ") + model.getLanguage());
        System.out.println(OutputFormatter.cyan("Build Tool: ") + model.getBuildTool());
        System.out.println(OutputFormatter.cyan("Testing Framework: ") + model.getTestFramework());
        System.out.println(OutputFormatter.cyan("Docker: ") + (model.isDockerEnabled() ? OutputFormatter.green("Found") : OutputFormatter.yellow("Not Found")));
        System.out.println(OutputFormatter.cyan("Kubernetes: ") + (model.isKubernetesEnabled() ? OutputFormatter.green("Found") : OutputFormatter.yellow("Not Found")));
        System.out.println(OutputFormatter.cyan("Deployment Target: ") + model.getDeploymentTarget());
        System.out.println(OutputFormatter.cyan("CI Platform: ") + recommendedPlatform);

        System.out.println("\n" + OutputFormatter.green("Done.") + "\n");
    }
}
