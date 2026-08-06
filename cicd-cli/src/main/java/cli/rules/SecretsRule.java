package cli.rules;

import cli.model.ProjectModel;
import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.validation.Severity;
import cli.validation.ValidationIssue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates that credentials, API keys, and sensitive tokens are not hardcoded.
 */
public class SecretsRule implements Rule {
    private static final String[] SENSITIVE_KEYWORDS = {
            "password", "secret", "token", "private_key", "api_key", "pwd", "credentials", "auth_key"
    };

    // Matches key=value style assignments in scripts: e.g. password=12345
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile("(?i)(password|secret|token|api_key|private_key|pwd)\\s*=\\s*([^\\s;&|]+)");
    // Matches CLI flags like -p, --password, etc.
    private static final Pattern CLI_PASSWORD_PATTERN = Pattern.compile("(?i)-(?:p|password|token|secret|api-key)\\s+([^\\s;&|]+)");

    @Override
    public String getName() {
        return "SecretsRule";
    }

    @Override
    public String getDescription() {
        return "Checks environment variables and scripts for hardcoded secrets, passwords, or access tokens.";
    }

    @Override
    public List<ValidationIssue> evaluate(ProjectModel project, ParsedPipeline pipeline) {
        List<ValidationIssue> issues = new ArrayList<>();

        // 1. Inspect global environment variables
        validateEnvMap(pipeline.getEnvironment(), "Global Env", issues);

        // 2. Inspect stage environment variables and commands
        for (PipelineStage stage : pipeline.getStages()) {
            validateEnvMap(stage.getEnv(), "Stage '" + stage.getName() + "' Env", issues);

            for (String cmd : stage.getCommands()) {
                Matcher matcher = ASSIGNMENT_PATTERN.matcher(cmd);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    String val = matcher.group(2).trim();

                    // Check if value is static (i.e. not referencing environment/secrets variable)
                    if (isHardcodedSecret(val)) {
                        issues.add(new ValidationIssue(
                                Severity.ERROR,
                                "Potential hardcoded secret detected in stage '" + stage.getName() + "' command: '" + key + "=" + maskSecret(val) + "'",
                                getName()
                        ));
                    }
                }

                Matcher cliMatcher = CLI_PASSWORD_PATTERN.matcher(cmd);
                while (cliMatcher.find()) {
                    String val = cliMatcher.group(1).trim();
                    if (isHardcodedSecret(val)) {
                        issues.add(new ValidationIssue(
                                Severity.ERROR,
                                "Potential hardcoded secret detected in stage '" + stage.getName() + "' CLI argument: '" + maskSecret(val) + "'",
                                getName()
                        ));
                    }
                }
            }
        }

        // 3. Info for total resolved secrets
        if (!pipeline.getSecrets().isEmpty()) {
            issues.add(new ValidationIssue(
                    Severity.INFO,
                    "Referenced " + pipeline.getSecrets().size() + " unique pipeline secret(s) securely.",
                    getName()
            ));
        }

        return issues;
    }

    private void validateEnvMap(Map<String, String> envMap, String context, List<ValidationIssue> issues) {
        if (envMap == null) return;
        for (Map.Entry<String, String> entry : envMap.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String val = entry.getValue();

            boolean isSensitiveKey = false;
            for (String kw : SENSITIVE_KEYWORDS) {
                if (key.contains(kw)) {
                    isSensitiveKey = true;
                    break;
                }
            }

            if (isSensitiveKey && isHardcodedSecret(val)) {
                issues.add(new ValidationIssue(
                        Severity.ERROR,
                        "Hardcoded secret in " + context + ": Key '" + entry.getKey() + "' has static value.",
                        getName()
                ));
            }
        }
    }

    private boolean isHardcodedSecret(String val) {
        if (val == null || val.trim().isEmpty()) {
            return false;
        }
        val = val.replace("\"", "").replace("'", "");
        
        // Non-hardcoded patterns:
        // ${{ secrets.XYZ }}
        // $VAR_NAME or ${VAR_NAME}
        // $(var) or $(Secrets.xyz)
        if (val.startsWith("$") || val.contains("${") || (val.startsWith("$(") && val.endsWith(")")) || val.startsWith("secret(") || val.matches("\\d+:\\d+")) {
            return false;
        }

        // If the value is a dummy/common placeholder like "dummy", "placeholder", "test", "my_secret", it's a warning, not error, but we treat it as hardcoded for security
        // Minimum length 4 to avoid matching simple variable defaults or short strings like 'yes'
        return val.length() >= 4;
    }

    private String maskSecret(String secret) {
        if (secret.length() <= 2) return "**";
        return secret.substring(0, 2) + "****";
    }
}
