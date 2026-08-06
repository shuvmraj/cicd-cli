package cli.commands;

import cli.detector.DetectorService;
import cli.model.ParsedPipeline;
import cli.model.ProjectModel;
import cli.parser.ParserFactory;
import cli.parser.PipelineParser;
import cli.validation.Severity;
import cli.validation.ValidationEngine;
import cli.validation.ValidationIssue;
import cli.validation.ValidationReport;
import cli.utils.OutputFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * Subcommand to validate CI/CD pipelines.
 */
@Command(
        name = "validate",
        description = "Validates pipeline configuration files against standard rules and codebase structure"
)
public class ValidateCommand implements Callable<Integer> {

    @Option(names = {"-f", "--file"}, description = "Path to the pipeline file to validate")
    private String pipelineFile;

    @Option(names = {"-d", "--dir"}, description = "Base directory of the codebase")
    private String baseDir = ".";

    @Override
    public Integer call() {
        Path projectPath = Path.of(baseDir).toAbsolutePath().normalize();
        Path pipelinePath = null;

        if (pipelineFile != null) {
            pipelinePath = Path.of(pipelineFile).toAbsolutePath().normalize();
        } else {
            // Auto-detect pipeline file
            pipelinePath = autoDetectPipeline(projectPath);
        }

        if (pipelinePath == null || !Files.exists(pipelinePath)) {
            System.err.println(OutputFormatter.red("Error: No pipeline configuration file found to validate."));
            if (pipelineFile != null) {
                System.err.println("Specified file: " + pipelineFile);
            } else {
                System.err.println("Checked standard locations: .github/workflows/, .gitlab-ci.yml, Jenkinsfile, azure-pipelines.yml");
            }
            return 1;
        }

        System.out.println("\nValidating pipeline file: " + OutputFormatter.bold(pipelinePath.toString()) + "...\n");

        try {
            String content = Files.readString(pipelinePath);
            PipelineParser parser = ParserFactory.getParser(pipelinePath);
            ParsedPipeline parsedPipeline = parser.parse(content);

            DetectorService detectorService = new DetectorService();
            ProjectModel projectModel = detectorService.detect(projectPath);

            ValidationEngine validationEngine = new ValidationEngine();
            ValidationReport report = validationEngine.validate(projectModel, parsedPipeline);

            // Display Report
            System.out.println(OutputFormatter.bold("Validation Report") + "\n");

            // Print Errors
            List<ValidationIssue> errors = filterIssues(report.getIssues(), Severity.ERROR);
            if (!errors.isEmpty()) {
                System.out.println(OutputFormatter.red(OutputFormatter.bold("ERROR")));
                for (ValidationIssue issue : errors) {
                    System.out.println("  " + OutputFormatter.red("-") + " " + issue.getMessage());
                }
                System.out.println();
            }

            // Print Warnings
            List<ValidationIssue> warnings = filterIssues(report.getIssues(), Severity.WARNING);
            if (!warnings.isEmpty()) {
                System.out.println(OutputFormatter.yellow(OutputFormatter.bold("WARNING")));
                for (ValidationIssue issue : warnings) {
                    System.out.println("  " + OutputFormatter.yellow("-") + " " + issue.getMessage());
                }
                System.out.println();
            }

            // Print Infos
            List<ValidationIssue> infos = filterIssues(report.getIssues(), Severity.INFO);
            if (!infos.isEmpty()) {
                System.out.println(OutputFormatter.blue(OutputFormatter.bold("INFO")));
                for (ValidationIssue issue : infos) {
                    System.out.println("  " + OutputFormatter.blue("-") + " " + issue.getMessage());
                }
                System.out.println();
            }

            // Overall Status
            System.out.println(OutputFormatter.bold("Overall Status"));
            if ("FAILED".equals(report.getStatus())) {
                System.out.println(OutputFormatter.bgRed(" " + report.getStatus() + " ") + "\n");
                return 1;
            } else {
                System.out.println(OutputFormatter.bgGreen(" " + report.getStatus() + " ") + "\n");
                return 0;
            }

        } catch (IOException e) {
            System.err.println(OutputFormatter.red("Failed to read pipeline file: " + e.getMessage()));
            return 1;
        }
    }

    private Path autoDetectPipeline(Path root) {
        // Check GitHub Actions
        Path githubDir = root.resolve(".github").resolve("workflows");
        if (Files.exists(githubDir) && Files.isDirectory(githubDir)) {
            try (Stream<Path> files = Files.list(githubDir)) {
                Path workflow = files.filter(f -> f.toString().endsWith(".yml") || f.toString().endsWith(".yaml"))
                        .findFirst()
                        .orElse(null);
                if (workflow != null) return workflow;
            } catch (IOException ignored) {}
        }

        // Check GitLab
        Path gitlab = root.resolve(".gitlab-ci.yml");
        if (Files.exists(gitlab)) return gitlab;

        // Check Jenkins
        Path jenkins = root.resolve("Jenkinsfile");
        if (Files.exists(jenkins)) return jenkins;

        // Check Azure
        Path azure = root.resolve("azure-pipelines.yml");
        if (Files.exists(azure)) return azure;

        return null;
    }

    private List<ValidationIssue> filterIssues(List<ValidationIssue> list, Severity severity) {
        List<ValidationIssue> filtered = new ArrayList<>();
        for (ValidationIssue issue : list) {
            if (issue.getSeverity() == severity) {
                filtered.add(issue);
            }
        }
        return filtered;
    }
}
