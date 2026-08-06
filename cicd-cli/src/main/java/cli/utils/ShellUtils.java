package cli.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Utility to run system processes safely with timeout limits.
 */
public class ShellUtils {

    /**
     * Executes a system command and returns the trimmed first line of output,
     * or null if the command failed/timed out/is not installed.
     */
    public static String execCommand(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Setup a safety timeout of 3 seconds
            boolean finished = process.waitFor(3, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return null;
            }

            if (process.exitValue() != 0) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                return line != null ? line.trim() : "";
            }
        } catch (Exception e) {
            return null;
        }
    }
}
