package cli.parser;

import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import org.yaml.snakeyaml.Yaml;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Azure DevOps Pipelines YAML configurations.
 */
public class AzureParser implements PipelineParser {
    private static final Pattern SECRET_PATTERN = Pattern.compile("\\$\\(([^)]*(?:secret|pwd|pass|token|key)[^)]*)\\)", Pattern.CASE_INSENSITIVE);

    @SuppressWarnings("unchecked")
    @Override
    public ParsedPipeline parse(String content) {
        ParsedPipeline pipeline = new ParsedPipeline();
        if (content == null || content.trim().isEmpty()) {
            pipeline.addError("Azure Pipelines content is empty");
            return pipeline;
        }

        try {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(content);
            if (root == null) {
                pipeline.addError("Invalid Azure Pipelines YAML structure");
                return pipeline;
            }

            // 1. Global Variables
            Object varsObj = root.get("variables");
            if (varsObj instanceof Map) {
                Map<String, Object> varsMap = (Map<String, Object>) varsObj;
                varsMap.forEach((k, v) -> pipeline.getEnvironment().put(k, String.valueOf(v)));
            } else if (varsObj instanceof List) {
                List<?> varsList = (List<?>) varsObj;
                for (Object item : varsList) {
                    if (item instanceof Map) {
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        Object name = itemMap.get("name");
                        Object val = itemMap.get("value");
                        if (name != null && val != null) {
                            pipeline.getEnvironment().put(String.valueOf(name), String.valueOf(val));
                        }
                    }
                }
            }

            // 2. Global Pool/Image
            Object poolObj = root.get("pool");
            if (poolObj instanceof Map) {
                Map<String, Object> poolMap = (Map<String, Object>) poolObj;
                Object vmImage = poolMap.get("vmImage");
                if (vmImage != null) {
                    pipeline.setDockerImage(String.valueOf(vmImage));
                }
            }

            // 3. Scan Secrets
            Matcher matcher = SECRET_PATTERN.matcher(content);
            while (matcher.find()) {
                pipeline.addSecret(matcher.group(1));
            }

            // 4. Cache detection
            if (content.contains("Cache@2") || content.contains("CacheBeta@2")) {
                pipeline.setHasCache(true);
            }

            // 5. Parse Stages / Jobs / Steps
            if (root.containsKey("stages")) {
                Object stagesObj = root.get("stages");
                if (stagesObj instanceof List) {
                    List<?> stagesList = (List<?>) stagesObj;
                    for (Object stageObj : stagesList) {
                        if (stageObj instanceof Map) {
                            Map<String, Object> stageMap = (Map<String, Object>) stageObj;
                            String stageName = (String) stageMap.getOrDefault("stage", "Stage");
                            PipelineStage pipelineStage = new PipelineStage(stageName);

                            // Extract dependencies
                            Object dependsOn = stageMap.get("dependsOn");
                            addDependencies(pipelineStage, dependsOn);

                            // Extract commands from nested jobs
                            Object jobsObj = stageMap.get("jobs");
                            if (jobsObj instanceof List) {
                                List<?> jobsList = (List<?>) jobsObj;
                                for (Object jobObj : jobsList) {
                                    if (jobObj instanceof Map) {
                                        Map<String, Object> jobMap = (Map<String, Object>) jobObj;
                                        extractSteps(pipelineStage, jobMap.get("steps"));
                                    }
                                }
                            }
                            pipeline.addStage(pipelineStage);
                        }
                    }
                }
            } else if (root.containsKey("jobs")) {
                Object jobsObj = root.get("jobs");
                if (jobsObj instanceof List) {
                    List<?> jobsList = (List<?>) jobsObj;
                    for (Object jobObj : jobsList) {
                        if (jobObj instanceof Map) {
                            Map<String, Object> jobMap = (Map<String, Object>) jobObj;
                            String jobName = (String) jobMap.getOrDefault("job", "Job");
                            PipelineStage pipelineStage = new PipelineStage(jobName);
                            
                            Object dependsOn = jobMap.get("dependsOn");
                            addDependencies(pipelineStage, dependsOn);
                            
                            extractSteps(pipelineStage, jobMap.get("steps"));
                            pipeline.addStage(pipelineStage);
                        }
                    }
                }
            } else if (root.containsKey("steps")) {
                PipelineStage pipelineStage = new PipelineStage("PipelineSteps");
                extractSteps(pipelineStage, root.get("steps"));
                pipeline.addStage(pipelineStage);
            } else {
                pipeline.addWarning("No stages, jobs, or steps found in Azure Pipelines file");
            }

        } catch (Exception e) {
            pipeline.addError("Failed to parse Azure Pipelines YAML: " + e.getMessage());
        }

        return pipeline;
    }

    private void addDependencies(PipelineStage stage, Object dependsOn) {
        if (dependsOn instanceof String) {
            stage.addDependency((String) dependsOn);
        } else if (dependsOn instanceof List) {
            List<?> depList = (List<?>) dependsOn;
            for (Object dep : depList) {
                stage.addDependency(String.valueOf(dep));
            }
        }
    }

    private void extractSteps(PipelineStage stage, Object stepsObj) {
        if (stepsObj instanceof List) {
            List<?> stepsList = (List<?>) stepsObj;
            for (Object stepObj : stepsList) {
                if (stepObj instanceof Map) {
                    Map<?, ?> stepMap = (Map<?, ?>) stepObj;
                    
                    // Azure steps can use script, bash, pwsh, powershell, checkout, task
                    String script = (String) stepMap.get("script");
                    if (script != null) {
                        stage.addCommand(script.trim());
                        continue;
                    }
                    
                    String bash = (String) stepMap.get("bash");
                    if (bash != null) {
                        stage.addCommand(bash.trim());
                        continue;
                    }

                    String pwsh = (String) stepMap.get("pwsh");
                    if (pwsh != null) {
                        stage.addCommand(pwsh.trim());
                        continue;
                    }

                    String task = (String) stepMap.get("task");
                    if (task != null) {
                        stage.addCommand("task: " + task);
                    }
                }
            }
        }
    }
}
