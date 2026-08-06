package cli.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Main command for the cicd CLI.
 */
@Command(
        name = "cicd",
        mixinStandardHelpOptions = true,
        version = "cicd CLI version 1.0.0",
        description = "Universal CI/CD Pipeline Validator, Generator, and Translator CLI",
        subcommands = {
                InitCommand.class,
                DetectCommand.class,
                ValidateCommand.class,
                GenerateCommand.class,
                ExplainCommand.class,
                ConvertCommand.class,
                DoctorCommand.class,
                VersionCommand.class
        }
)
public class MainCommand implements Runnable {
    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
