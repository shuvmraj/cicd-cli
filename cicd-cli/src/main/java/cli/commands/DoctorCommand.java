package cli.commands;

import cli.utils.ShellUtils;
import cli.utils.OutputFormatter;
import picocli.CommandLine.Command;

/**
 * Subcommand to check system dependencies and environment health.
 */
@Command(
        name = "doctor",
        description = "Validates the local environment, verifying toolchain installations (Docker, Git, kubectl, helm, Node, Java)"
)
public class DoctorCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("\nChecking CLI Environment Health...\n");

        boolean overallPass = true;

        // 1. Git Check
        checkTool("Git", new String[]{"git", "--version"}, true);

        // 2. Docker Check
        checkTool("Docker", new String[]{"docker", "--version"}, false);

        // 3. Java Check
        checkTool("Java", new String[]{"java", "--version"}, false);

        // 4. Node Check
        checkTool("Node", new String[]{"node", "--version"}, false);

        // 5. kubectl Check
        checkTool("kubectl", new String[]{"kubectl", "version", "--client"}, false);

        // 6. helm Check
        checkTool("helm", new String[]{"helm", "version", "--template", "{{.Version}}"}, false);

        System.out.println("\nEnvironment scan complete.\n");
    }

    private void checkTool(String name, String[] cmd, boolean isCritical) {
        String output = ShellUtils.execCommand(cmd);
        String label = String.format("%-10s", name);

        if (output != null && !output.isEmpty()) {
            // Clean up outputs for compact display
            String version = cleanVersionOutput(name, output);
            System.out.println(OutputFormatter.cyan(label) + " " + 
                               String.format("%-45s", version) + " " + 
                               OutputFormatter.bgGreen(" PASS "));
        } else {
            if (isCritical) {
                System.out.println(OutputFormatter.cyan(label) + " " + 
                                   String.format("%-45s", "Not detected on PATH") + " " + 
                                   OutputFormatter.bgRed(" FAIL "));
            } else {
                System.out.println(OutputFormatter.cyan(label) + " " + 
                                   String.format("%-45s", "Not detected on PATH") + " " + 
                                   OutputFormatter.bgYellow(" WARNING "));
            }
        }
    }

    private String cleanVersionOutput(String name, String raw) {
        if (raw == null) return "Unknown Version";
        // Clean multi-line outputs
        raw = raw.split("\n")[0];

        if (name.equalsIgnoreCase("java")) {
            // java --version contains openjdk or java version, extract standard parts
            if (raw.contains("openjdk")) {
                return raw.substring(0, Math.min(raw.length(), 35));
            }
        }
        if (name.equalsIgnoreCase("kubectl")) {
            // kubectl output is long, clean it up
            if (raw.contains("Client Version")) {
                return "Client Version " + raw.replaceAll("(?i).*GitVersion:\"([^\"]+)\".*", "$1");
            }
            return raw.substring(0, Math.min(raw.length(), 35));
        }
        return raw;
    }
}
