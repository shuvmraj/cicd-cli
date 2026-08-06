package cli.commands;

import cli.detector.DetectorService;
import cli.generator.PipelineGenerator;
import cli.generator.PipelineGeneratorFactory;
import cli.model.ProjectModel;
import cli.utils.OutputFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Subcommand to generate CI/CD pipelines.
 */
@Command(
        name = "generate",
        description = "Generates a fully optimized pipeline configuration file based on codebase technology stack"
)
public class GenerateCommand implements Runnable {

    @Parameters(index = "0", description = "Target platform: github, gitlab, jenkins, azure")
    private String platform;

    @Option(names = {"-d", "--dir"}, description = "Codebase directory to inspect")
    private String baseDir = ".";

    @Option(names = {"-o", "--output"}, description = "Output file path (prints to stdout if omitted)")
    private String outputPath;

    @Option(names = {"-w", "--write"}, description = "Write directly to standard platform location")
    private boolean writeToStandardLocation = false;

    @Override
    public void run() {
        Path projectPath = Path.of(baseDir).toAbsolutePath().normalize();
        
        if (!Files.exists(projectPath)) {
            System.err.println(OutputFormatter.red("Error: Project directory does not exist: " + baseDir));
            return;
        }

        try {
            // 1. Detect Stack
            DetectorService detectorService = new DetectorService();
            ProjectModel model = detectorService.detect(projectPath);

            // 2. Generate Pipeline
            PipelineGenerator generator = PipelineGeneratorFactory.getGenerator(platform);
            String renderedPipeline = generator.generate(model);

            // 3. Resolve destination path
            Path destPath = null;
            if (outputPath != null) {
                destPath = Path.of(outputPath).toAbsolutePath().normalize();
            } else if (writeToStandardLocation) {
                destPath = resolveStandardLocation(projectPath, platform);
            }

            if (destPath != null) {
                // Ensure parent directories exist
                Path parent = destPath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.writeString(destPath, renderedPipeline);
                System.out.println("\n" + OutputFormatter.green("Success: ") + "Pipeline generated and saved to: " + OutputFormatter.bold(destPath.toString()) + "\n");
            } else {
                // Output to stdout
                System.out.println("\n" + OutputFormatter.bold("Generated " + platform + " Pipeline:") + "\n");
                System.out.println(renderedPipeline);
                System.out.println();
            }

        } catch (IllegalArgumentException e) {
            System.err.println(OutputFormatter.red("Error: " + e.getMessage()));
        } catch (IOException e) {
            System.err.println(OutputFormatter.red("IO Error: Failed to write output: " + e.getMessage()));
        }
    }

    private Path resolveStandardLocation(Path root, String platformName) {
        switch (platformName.trim().toLowerCase()) {
            case "github":
            case "github-actions":
                return root.resolve(".github").resolve("workflows").resolve("main.yml");
            case "gitlab":
            case "gitlab-ci":
                return root.resolve(".gitlab-ci.yml");
            case "jenkins":
            case "jenkinsfile":
                return root.resolve("Jenkinsfile");
            case "azure":
            case "azure-devops":
            case "azure-pipelines":
                return root.resolve("azure-pipelines.yml");
            case "docker":
            case "dockerfile":
                return root.resolve("Dockerfile");
            default:
                return null;
        }
    }
}
