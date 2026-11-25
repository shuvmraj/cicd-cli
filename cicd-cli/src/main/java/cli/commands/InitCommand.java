package cli.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Subcommand to initialize a new .cicd-config.json file.
 */
@Command(
        name = "init",
        description = "Initializes the CLI in the current directory by creating a .cicd-config.json"
)
public class InitCommand implements Callable<Integer> {

    @Option(names = {"-d", "--dir"}, description = "Target directory to initialize")
    private String targetDir = ".";

    @Override
    public Integer call() {
        Path path = Path.of(targetDir).toAbsolutePath().normalize();
        File configFile = path.resolve(".cicd-config.json").toFile();

        if (configFile.exists()) {
            System.out.println("CLI is already initialized. Config file exists at: " + configFile.getAbsolutePath());
            return 0;
        }

        try {
            Files.createDirectories(path);

            Map<String, Object> config = new LinkedHashMap<>();
            config.put("projectName", path.getFileName().toString());
            config.put("version", "1.0.0");
            config.put("customBuildCommand", null);
            config.put("customTestCommand", null);
            config.put("overrideLanguage", null);
            config.put("overrideFramework", null);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(configFile, config);

            System.out.println("\nInitialized successfully!");
            System.out.println("Config created at: " + configFile.getAbsolutePath() + "\n");
            return 0;
        } catch (IOException e) {
            System.err.println("Failed to create configuration: " + e.getMessage());
            return 1;
        }
    }
}
