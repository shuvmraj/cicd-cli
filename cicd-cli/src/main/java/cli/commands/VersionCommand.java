package cli.commands;

import picocli.CommandLine.Command;

/**
 * Subcommand to output current CLI version.
 */
@Command(
        name = "version",
        description = "Prints the version details of this CLI tool"
)
public class VersionCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("cicd CLI version 1.0.0");
        System.out.println("Java Runtime: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
    }
}
