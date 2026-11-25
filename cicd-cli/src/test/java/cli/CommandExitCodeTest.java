package cli;

import cli.commands.MainCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandExitCodeTest {
    @TempDir
    Path tempDir;

    @Test
    void missingProjectDirectoryReturnsFailure() {
        int exitCode = new CommandLine(new MainCommand())
                .execute("detect", "--dir", tempDir.resolve("missing").toString());

        assertEquals(1, exitCode);
    }

    @Test
    void invalidGenerationTargetReturnsUsageFailure() {
        int exitCode = new CommandLine(new MainCommand())
                .execute("generate", "unknown", "--dir", tempDir.toString());

        assertEquals(2, exitCode);
    }

    @Test
    void unsupportedConversionTargetReturnsUsageFailure() throws IOException {
        Path input = tempDir.resolve("main.yml");
        Files.writeString(input, "name: test\njobs: {}\n");
        int exitCode = new CommandLine(new MainCommand())
                .execute("convert", "github", "unknown", "--file", input.toString());

        assertEquals(2, exitCode);
    }

    @Test
    void missingPipelineReturnsFailure() {
        int exitCode = new CommandLine(new MainCommand())
                .execute("validate", "--file", tempDir.resolve("missing.yml").toString(),
                        "--dir", tempDir.toString());

        assertEquals(1, exitCode);
    }
}
