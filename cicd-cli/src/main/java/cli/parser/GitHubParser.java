package cli.parser;

import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.model.PipelineStep;
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

            YamlSourceLocations locations = new YamlSourceLocations(content);
            pipeline.setPlatform("github");
            pipeline.setName(stringValue(root.get("name")));

            for (Map.Entry<String, Object> entry : root.entrySet()) {
                if ("on".equalsIgnoreCase(String.valueOf(entry.getKey()))
                        || Boolean.TRUE.equals(entry.getKey())) {
                    pipeline.setTriggerConfiguration(entry.getValue());
                    addTriggerValues(pipeline, entry.getValue());
                }
            }

            // 1. Global Env
            Object envObj = root.get("env");
            if (envObj instanceof Map) {
                Map<String, Object> envMap = (Map<String, Object>) envObj;
                envMap.forEach((k, v) -> pipeline.getEnvironment().put(k, String.valueOf(v)));
            }

            Object container = root.get("container");
            if (container != null) {
                pipeline.setDockerImage(readImage(container));
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
                        String name = stringValue(jobMap.getOrDefault("name", jobId));
                        PipelineStage stage = new PipelineStage(name);
                        stage.setId(jobId);
                        stage.setStageName("jobs");
                        stage.setSourceLine(locations.lineForPath("jobs", jobId));
                        stage.setCondition(stringValue(jobMap.get("if")));

                        // A runner label is not a container image.
                        Object runsOn = jobMap.get("runs-on");
                        if (runsOn != null) {
                            stage.setRunner(stringValue(runsOn));
                        }
                        Object jobContainer = jobMap.get("container");
                        if (jobContainer != null) {
                            stage.setContainerImage(readImage(jobContainer));
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
                            for (int stepIndex = 0; stepIndex < stepsList.size(); stepIndex++) {
                                Object stepObj = stepsList.get(stepIndex);
                                if (stepObj instanceof Map) {
                                    Map<String, Object> stepMap = (Map<String, Object>) stepObj;
                                    PipelineStep step = new PipelineStep();
                                    step.setName(stringValue(stepMap.get("name")));
                                    step.setSourceLine(locations.lineForPath("jobs", jobId, "steps", stepIndex));
                                    Object stepEnvObj = stepMap.get("env");
                                    if (stepEnvObj instanceof Map) {
                                        step.setEnvironment(toStringMap((Map<?, ?>) stepEnvObj));
                                    }
                                    Object withObj = stepMap.get("with");
                                    if (withObj instanceof Map) {
                                        step.setInputs(toStringMap((Map<?, ?>) withObj));
                                    }

                                    String runCmd = stringValue(stepMap.get("run"));
                                    if (runCmd != null) {
                                        step.setCommand(runCmd);
                                        stage.addStep(step);
                                    } else {
                                        String usesAction = stringValue(stepMap.get("uses"));
                                        if (usesAction != null) {
                                            step.setUses(usesAction);
                                            stage.addStep(step);
                                        } else {
                                            pipeline.addUnsupportedFeature("GitHub step without run/uses");
                                        }
                                    }
                                }
                            }
                        }
                        if (jobMap.containsKey("uses") && !(stepsObj instanceof List)) {
                            pipeline.addUnsupportedFeature("GitHub reusable workflow job '" + jobId + "'");
                        }
                        Object strategy = jobMap.get("strategy");
                        if (strategy instanceof Map && ((Map<?, ?>) strategy).containsKey("matrix")) {
                            pipeline.addUnsupportedFeature("GitHub job matrix strategy");
                        }

                        pipeline.addStage(stage);
                    }
                }
            } else {
                pipeline.addWarning("No jobs found in the workflow file");
            }

            if (root.containsKey("permissions")) {
                pipeline.addUnsupportedFeature("GitHub workflow permissions");
            }
            if (root.containsKey("concurrency")) {
                pipeline.addUnsupportedFeature("GitHub workflow concurrency");
            }

        } catch (Exception e) {
            pipeline.addError("Failed to parse YAML syntax: " + e.getMessage());
        }

        return pipeline;
    }

    private void addTriggerValues(ParsedPipeline pipeline, Object value) {
        if (value instanceof Map) {
            ((Map<?, ?>) value).forEach((key, triggerConfiguration) -> {
                pipeline.addTrigger(String.valueOf(key));
                if (triggerConfiguration instanceof Map && !((Map<?, ?>) triggerConfiguration).isEmpty()) {
                    pipeline.addUnsupportedFeature("GitHub trigger filters");
                }
            });
        } else if (value instanceof List) {
            ((List<?>) value).forEach(item -> pipeline.addTrigger(String.valueOf(item)));
        } else if (value != null) {
            pipeline.addTrigger(String.valueOf(value));
        }
    }

    private String readImage(Object container) {
        if (container instanceof Map) {
            return stringValue(((Map<?, ?>) container).get("image"));
        }
        return stringValue(container);
    }

    private Map<String, String> toStringMap(Map<?, ?> source) {
        Map<String, String> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(String.valueOf(key), stringValue(value)));
        return result;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
