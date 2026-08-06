package cli.commands;

import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.model.ProjectModel;
import cli.parser.ParserFactory;
import cli.parser.PipelineParser;
import cli.generator.PipelineGenerator;
import cli.generator.PipelineGeneratorFactory;
import cli.utils.OutputFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Subcommand to convert pipelines between platforms.
 */
@Command(
        name = "convert",
        description = "Converts pipelines from a source platform to a target platform"
)
public class ConvertCommand implements Runnable {

    @Parameters(index = "0", description = "Source platform (e.g. github, gitlab, jenkins, azure)")
    private String sourcePlatform;

    @Parameters(index = "1", description = "Target platform (e.g. github, gitlab, jenkins, azure)")
    private String targetPlatform;

    @Option(names = {"-f", "--file"}, description = "Input pipeline file (auto-detected if omitted)")
    private String inputFile;

    @Option(names = {"-o", "--output"}, description = "Output target file path (prints to stdout if omitted)")
    private String outputFile;

    @Override
    public void run() {
        Path inputPath = null;
        if (inputFile != null) {
            inputPath = Path.of(inputFile).toAbsolutePath().normalize();
        } else {
            inputPath = autoDetectSourcePipeline(sourcePlatform);
        }

        if (inputPath == null || !Files.exists(inputPath)) {
            System.err.println(OutputFormatter.red("Error: Source pipeline file not found."));
            return;
        }

        System.out.println("\nConverting " + OutputFormatter.bold(sourcePlatform) + 
                " (" + inputPath.getFileName() + ") to " + OutputFormatter.bold(targetPlatform) + "...\n");

        try {
            // 1. Parse source file into ParsedPipeline
            String sourceContent = Files.readString(inputPath);
            PipelineParser parser = ParserFactory.getParser(inputPath);
            ParsedPipeline parsedPipeline = parser.parse(sourceContent);

            // 2. Reconstruct ProjectModel from parsed contents
            ProjectModel model = reconstructModel(parsedPipeline);

            // 3. Generate target pipeline
            PipelineGenerator generator = PipelineGeneratorFactory.getGenerator(targetPlatform);
            String renderedTarget = generator.generate(model);

            // 4. Save or Print output
            if (outputFile != null) {
                Path outPath = Path.of(outputFile).toAbsolutePath().normalize();
                Path parent = outPath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.writeString(outPath, renderedTarget);
                System.out.println(OutputFormatter.green("Success: ") + "Pipeline translated and saved to: " + OutputFormatter.bold(outPath.toString()) + "\n");
            } else {
                System.out.println(OutputFormatter.bold("Converted Pipeline (" + targetPlatform + "):") + "\n");
                System.out.println(renderedTarget);
                System.out.println();
            }

        } catch (IllegalArgumentException e) {
            System.err.println(OutputFormatter.red("Configuration Error: " + e.getMessage()));
        } catch (IOException e) {
            System.err.println(OutputFormatter.red("IO Error: Failed translation steps: " + e.getMessage()));
        }
    }

    private Path autoDetectSourcePipeline(String platform) {
        Path currentDir = Path.of(".").toAbsolutePath().normalize();
        switch (platform.trim().toLowerCase()) {
            case "github":
            case "github-actions":
                Path githubDir = currentDir.resolve(".github").resolve("workflows");
                if (Files.exists(githubDir) && Files.isDirectory(githubDir)) {
                    try {
                        return Files.list(githubDir)
                                .filter(f -> f.toString().endsWith(".yml") || f.toString().endsWith(".yaml"))
                                .findFirst()
                                .orElse(null);
                    } catch (IOException ignored) {}
                }
                break;
            case "gitlab":
            case "gitlab-ci":
                Path gitlab = currentDir.resolve(".gitlab-ci.yml");
                if (Files.exists(gitlab)) return gitlab;
                break;
            case "jenkins":
            case "jenkinsfile":
                Path jenkins = currentDir.resolve("Jenkinsfile");
                if (Files.exists(jenkins)) return jenkins;
                break;
            case "azure":
            case "azure-devops":
            case "azure-pipelines":
                Path azure = currentDir.resolve("azure-pipelines.yml");
                if (Files.exists(azure)) return azure;
                break;
        }
        return null;
    }

    /**
     * Inspects a parsed pipeline to reverse-engineer a generic ProjectModel representation.
     */
    private ProjectModel reconstructModel(ParsedPipeline pipeline) {
        ProjectModel.Builder builder = new ProjectModel.Builder();

        // Default assumptions
        String language = "Unknown";
        String framework = "Unknown";
        String buildTool = "None";
        String testFramework = "None";
        boolean dockerEnabled = pipeline.getDockerImage() != null;
        boolean k8sEnabled = false;

        // Scan stages for command keywords to resolve stack details
        for (PipelineStage stage : pipeline.getStages()) {
            for (String cmd : stage.getCommands()) {
                String c = cmd.toLowerCase();

                if (c.contains("mvn")) {
                    language = "Java";
                    framework = "Spring Boot";
                    buildTool = "Maven";
                    testFramework = "JUnit";
                } else if (c.contains("gradle")) {
                    language = "Java";
                    framework = "Spring Boot";
                    buildTool = "Gradle";
                    testFramework = "JUnit";
                } else if (c.contains("npm") || c.contains("yarn") || c.contains("pnpm")) {
                    language = "JavaScript";
                    buildTool = c.contains("yarn") ? "yarn" : (c.contains("pnpm") ? "pnpm" : "npm");
                    testFramework = "Jest";
                    
                    if (c.contains("react") || framework.equals("Unknown")) {
                        framework = "React";
                    }
                } else if (c.contains("pytest") || c.contains("pip") || c.contains("poetry") || c.contains("python")) {
                    language = "Python";
                    buildTool = c.contains("poetry") ? "Poetry" : "pip";
                    testFramework = "pytest";
                    if (c.contains("django")) {
                        framework = "Django";
                    } else if (c.contains("flask")) {
                        framework = "Flask";
                    }
                } else if (c.contains("composer") || c.contains("phpunit") || c.contains("artisan")) {
                    language = "PHP";
                    framework = "Laravel";
                    buildTool = "composer";
                    testFramework = "PHPUnit";
                } else if (c.contains("dotnet")) {
                    language = "C#";
                    framework = "ASP.NET Core";
                    buildTool = "dotnet";
                    testFramework = "xUnit";
                }

                if (c.contains("docker build") || c.contains("docker push") || c.contains("build-push-action")) {
                    dockerEnabled = true;
                }

                if (c.contains("kubectl") || c.contains("helm")) {
                    k8sEnabled = true;
                }
            }
        }

        builder.language(language)
               .framework(framework)
               .buildTool(buildTool)
               .testFramework(testFramework)
               .dockerEnabled(dockerEnabled)
               .kubernetesEnabled(k8sEnabled);

        // Resolve deployment target
        if (k8sEnabled) {
            builder.deploymentTarget("Kubernetes");
        } else if (dockerEnabled) {
            builder.deploymentTarget("Docker");
        } else if (language.equals("JavaScript") && (framework.equals("React") || framework.equals("Angular") || framework.equals("Vue"))) {
            builder.deploymentTarget("Static Hosting");
        } else {
            builder.deploymentTarget("Virtual Machine / Cloud Run");
        }

        // Artifact resolution
        if (language.equals("Java")) {
            builder.artifactType("JAR");
        } else if (language.equals("JavaScript")) {
            builder.artifactType("DIST");
        } else {
            builder.artifactType("DIST");
        }

        return builder.build();
    }
}
