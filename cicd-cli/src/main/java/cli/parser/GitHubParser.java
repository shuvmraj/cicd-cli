package cli.parser;

import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import org.yaml.snakeyaml.Yaml;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses GitHub Actions YAML workflows.
 */
public class GitHubParser implements PipelineParser {
    private static final Pattern SECRET_PATTERN = Pattern.compile("secrets\\.(\\w+)");

    @SuppressWarnings("unchecked")
    @Override
    public ParsedPipeline parse(String content) {
        ParsedPipeline pipeline = new ParsedPipeline();
        if (content == null || content.trim().isEmpty()) {
            pipeline.addError("Workflow content is empty");
            return pipeline;
        }

        try {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(content);
            if (root == null) {
                pipeline.addError("Invalid YAML structure or empty workflow");
                return pipeline;
            }

            // 1. Global Env
            Object envObj = root.get("env");
            if (envObj instanceof Map) {
                Map<String, Object> envMap = (Map<String, Object>) envObj;
                envMap.forEach((k, v) -> pipeline.getEnvironment().put(k, String.valueOf(v)));
            }

            // 2. Scan for secrets in the entire content
            Matcher matcher = SECRET_PATTERN.matcher(content);
            while (matcher.find()) {
                pipeline.addSecret(matcher.group(1));
            }

            // 3. Scan for cache usage
            if (content.contains("actions/cache")) {
                pipeline.setHasCache(true);
            }

            // 4. Parse Jobs
            Object jobsObj = root.get("jobs");
            if (jobsObj instanceof Map) {
                Map<String, Object> jobsMap = (Map<String, Object>) jobsObj;
                for (Map.Entry<String, Object> entry : jobsMap.entrySet()) {
                    String jobId = entry.getKey();
                    Object jobVal = entry.getValue();

                    if (jobVal instanceof Map) {
                        Map<String, Object> jobMap = (Map<String, Object>) jobVal;
                        String name = (String) jobMap.getOrDefault("name", jobId);
                        PipelineStage stage = new PipelineStage(name);

                        // Extract image (runs-on)
                        Object runsOn = jobMap.get("runs-on");
                        if (runsOn != null) {
                            pipeline.setDockerImage(String.valueOf(runsOn));
                        }

                        // Extract dependencies (needs)
                        Object needs = jobMap.get("needs");
                        if (needs instanceof String) {
                            stage.addDependency((String) needs);
                        } else if (needs instanceof List) {
                            List<?> needsList = (List<?>) needs;
                            for (Object need : needsList) {
                                stage.addDependency(String.valueOf(need));
                            }
                        }

                        // Extract job environment
                        Object jobEnvObj = jobMap.get("env");
                        if (jobEnvObj instanceof Map) {
                            Map<String, Object> jobEnvMap = (Map<String, Object>) jobEnvObj;
                            jobEnvMap.forEach((k, v) -> stage.getEnv().put(k, String.valueOf(v)));
                        }

                        // Extract commands from steps
                        Object stepsObj = jobMap.get("steps");
                        if (stepsObj instanceof List) {
                            List<?> stepsList = (List<?>) stepsObj;
                            for (Object stepObj : stepsList) {
                                if (stepObj instanceof Map) {
                                    Map<String, Object> stepMap = (Map<String, Object>) stepObj;
                                    String runCmd = (String) stepMap.get("run");
                                    if (runCmd != null) {
                                        // Split multiline commands
                                        for (String line : runCmd.split("\\r?\\n")) {
                                            if (!line.trim().isEmpty()) {
                                                stage.addCommand(line.trim());
                                            }
                                        }
                                    } else {
                                        String usesAction = (String) stepMap.get("uses");
                                        if (usesAction != null) {
                                            stage.addCommand("uses: " + usesAction);
                                        }
                                    }
                                }
                            }
                        }

                        pipeline.addStage(stage);
                    }
                }
            } else {
                pipeline.addWarning("No jobs found in the workflow file");
            }

        } catch (Exception e) {
            pipeline.addError("Failed to parse YAML syntax: " + e.getMessage());
        }

        return pipeline;
    }
}
