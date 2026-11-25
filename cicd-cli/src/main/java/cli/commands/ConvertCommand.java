package cli.commands;

import cli.model.ParsedPipeline;
import cli.parser.ParserFactory;
import cli.parser.PipelineParser;
import cli.generator.PipelineConversionResult;
import cli.generator.PipelineConverter;
import cli.utils.OutputFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * Subcommand to convert pipelines between platforms.
 */
@Command(
        name = "convert",
        description = "Converts pipelines from a source platform to a target platform"
)
public class ConvertCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Source platform (e.g. github, gitlab, jenkins, azure)")
    private String sourcePlatform;

    @Parameters(index = "1", description = "Target platform (e.g. github, gitlab, jenkins, azure)")
    private String targetPlatform;

    @Option(names = {"-f", "--file"}, description = "Input pipeline file (auto-detected if omitted)")
    private String inputFile;

    @Option(names = {"-o", "--output"}, description = "Output target file path (prints to stdout if omitted)")
    private String outputFile;

    @Option(names = {"-d", "--dir"}, description = "Project directory for source pipeline auto-detection")
    private String baseDir = ".";

    @Override
    public Integer call() {
        try {
            Path projectPath = Path.of(baseDir).toAbsolutePath().normalize();
            Path inputPath = inputFile == null
                    ? autoDetectSourcePipeline(projectPath, sourcePlatform)
                    : Path.of(inputFile).toAbsolutePath().normalize();
            if (inputPath == null || !Files.isRegularFile(inputPath)) {
                System.err.println(OutputFormatter.red("Error: Source pipeline file not found."));
                return 1;
            }

            String sourceContent = Files.readString(inputPath);
            PipelineParser parser = ParserFactory.getParser(sourcePlatform);
            ParsedPipeline parsedPipeline = parser.parse(sourceContent);
            if (!parsedPipeline.getErrors().isEmpty()) {
                parsedPipeline.getErrors().forEach(error ->
                        System.err.println(OutputFormatter.red("Parse error: " + error)));
                return 1;
            }
            PipelineConversionResult result = new PipelineConverter().convert(parsedPipeline, targetPlatform);
            for (String warning : parsedPipeline.getWarnings()) {
                System.err.println(OutputFormatter.yellow("Warning: " + warning));
            }
            for (String warning : result.getWarnings()) {
                System.err.println(OutputFormatter.yellow("Conversion warning: " + warning));
            }

            System.out.println("\nConverting " + OutputFormatter.bold(sourcePlatform) +
                    " (" + inputPath.getFileName() + ") to " + OutputFormatter.bold(targetPlatform) + "...\n");
            if (outputFile != null) {
                Path outPath = Path.of(outputFile).toAbsolutePath().normalize();
                if (outPath.equals(inputPath)) {
                    System.err.println(OutputFormatter.red(
                            "Error: Output path matches the input file; choose a different output path."));
                    return 1;
                }
                Path parent = outPath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.writeString(outPath, result.getContent());
                System.out.println(OutputFormatter.green("Success: ") + "Pipeline translated and saved to: " + OutputFormatter.bold(outPath.toString()) + "\n");
            } else {
                System.out.println(OutputFormatter.bold("Converted Pipeline (" + targetPlatform + "):") + "\n");
                System.out.println(result.getContent());
                System.out.println();
            }
            return 0;
        } catch (IllegalArgumentException e) {
            System.err.println(OutputFormatter.red("Configuration Error: " + e.getMessage()));
            return 2;
        } catch (IOException e) {
            System.err.println(OutputFormatter.red("IO Error: Failed translation steps: " + e.getMessage()));
            return 1;
        }
    }

    private Path autoDetectSourcePipeline(Path root, String platform) throws IOException {
        switch (platform.trim().toLowerCase()) {
            case "github":
            case "github-actions":
                Path githubDir = root.resolve(".github").resolve("workflows");
                if (Files.exists(githubDir) && Files.isDirectory(githubDir)) {
                    try (Stream<Path> files = Files.list(githubDir)) {
                        return files.filter(Files::isRegularFile)
                                .filter(f -> f.toString().endsWith(".yml") || f.toString().endsWith(".yaml"))
                                .sorted()
                                .findFirst()
                                .orElse(null);
                    }
                }
                break;
            case "gitlab":
            case "gitlab-ci":
                Path gitlab = root.resolve(".gitlab-ci.yml");
                if (Files.exists(gitlab)) return gitlab;
                break;
            case "jenkins":
            case "jenkinsfile":
                Path jenkins = root.resolve("Jenkinsfile");
                if (Files.exists(jenkins)) return jenkins;
                break;
            case "azure":
            case "azure-devops":
            case "azure-pipelines":
                Path azure = root.resolve("azure-pipelines.yml");
                if (Files.exists(azure)) return azure;
                break;
            default:
                ParserFactory.getParser(platform);
        }
        return null;
    }
}
