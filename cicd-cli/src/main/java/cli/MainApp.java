package cli;

import cli.commands.MainCommand;
import picocli.CommandLine;

/**
 * Entry point for the Universal CI/CD Pipeline CLI.
 */
public class MainApp {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new MainCommand()).execute(args);
        System.exit(exitCode);
    }
}
