package cli.parser;

import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.model.PipelineStep;
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

            YamlSourceLocations locations = new YamlSourceLocations(content);
            pipeline.setPlatform("azure");
            pipeline.setName(stringValue(root.get("name")));
            addTriggerValues(pipeline, root.get("trigger"));

            // 1. Global Variables
            Object varsObj = root.get("variables");
            if (varsObj instanceof Map) {
                toStringMap((Map<?, ?>) varsObj).forEach(pipeline.getEnvironment()::put);
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

            // 2. Global Pool and container image
            Object poolObj = root.get("pool");
            if (poolObj instanceof Map) {
                Map<String, Object> poolMap = (Map<String, Object>) poolObj;
                Object vmImage = poolMap.get("vmImage");
                if (vmImage != null) {
                    pipeline.setRunner(String.valueOf(vmImage));
                }
            } else if (poolObj instanceof String) {
                pipeline.setRunner(String.valueOf(poolObj));
            }
            Object containerObj = root.get("container");
            if (containerObj != null) {
                pipeline.setDockerImage(readImage(containerObj));
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
                    for (int stageIndex = 0; stageIndex < stagesList.size(); stageIndex++) {
                        Object stageObj = stagesList.get(stageIndex);
                        if (stageObj instanceof Map) {
                            Map<String, Object> stageMap = (Map<String, Object>) stageObj;
                            String stageName = stringValue(stageMap.getOrDefault("stage", "Stage"));
                            List<String> stageDependencies = toStringList(stageMap.get("dependsOn"));

                            // Extract commands from nested jobs
                            Object jobsObj = stageMap.get("jobs");
                            if (jobsObj instanceof List) {
                                List<?> jobsList = (List<?>) jobsObj;
                                for (int jobIndex = 0; jobIndex < jobsList.size(); jobIndex++) {
                                    Object jobObj = jobsList.get(jobIndex);
                                    if (jobObj instanceof Map) {
                                        Map<String, Object> jobMap = (Map<String, Object>) jobObj;
                                        String jobId = stringValue(jobMap.getOrDefault("job",
                                                jobMap.getOrDefault("deployment", "Job")));
                                        PipelineStage pipelineJob = new PipelineStage(
                                                stringValue(jobMap.getOrDefault("displayName", jobId)));
                                        pipelineJob.setId(jobId);
                                        pipelineJob.setStageName(stageName);
                                        pipelineJob.setSourceLine(locations.lineForPath(
                                                "stages", stageIndex, "jobs", jobIndex));
                                        pipelineJob.setStageDependencies(stageDependencies);
                                        addDependencies(pipelineJob, jobMap.get("dependsOn"));
                                        pipelineJob.setCondition(stringValue(jobMap.get("condition")));
                                        pipelineJob.setRunner(readPoolImage(jobMap.get("pool"), pipeline.getRunner()));
                                        pipelineJob.setContainerImage(readImage(jobMap.get("container")));
                                        Object jobVariables = jobMap.get("variables");
                                        if (jobVariables instanceof Map) {
                                            toStringMap((Map<?, ?>) jobVariables)
                                                    .forEach(pipelineJob.getEnv()::put);
                                        }
                                        extractSteps(pipelineJob, jobMap.get("steps"));
                                        if (jobMap.containsKey("deployment")) {
                                            pipeline.addUnsupportedFeature("Azure deployment jobs");
                                        }
                                        pipeline.addStage(pipelineJob);
                                    }
                                }
                            } else {
                                pipeline.addWarning("Azure stage '" + stageName + "' has no job list");
                            }
                        }
                    }
                }
            } else if (root.containsKey("jobs")) {
                Object jobsObj = root.get("jobs");
                if (jobsObj instanceof List) {
                    List<?> jobsList = (List<?>) jobsObj;
                    for (int jobIndex = 0; jobIndex < jobsList.size(); jobIndex++) {
                        Object jobObj = jobsList.get(jobIndex);
                        if (jobObj instanceof Map) {
                            Map<String, Object> jobMap = (Map<String, Object>) jobObj;
                            String jobName = stringValue(jobMap.getOrDefault("job", "Job"));
                            PipelineStage pipelineStage = new PipelineStage(
                                    stringValue(jobMap.getOrDefault("displayName", jobName)));
                            pipelineStage.setId(jobName);
                            pipelineStage.setSourceLine(locations.lineForPath("jobs", jobIndex));
                            pipelineStage.setRunner(readPoolImage(jobMap.get("pool"), pipeline.getRunner()));
                            pipelineStage.setContainerImage(readImage(jobMap.get("container")));
                            
                            Object dependsOn = jobMap.get("dependsOn");
                            addDependencies(pipelineStage, dependsOn);
                            pipelineStage.setCondition(stringValue(jobMap.get("condition")));
                            Object jobVariables = jobMap.get("variables");
                            if (jobVariables instanceof Map) {
                                toStringMap((Map<?, ?>) jobVariables).forEach(pipelineStage.getEnv()::put);
                            }
                            
                            extractSteps(pipelineStage, jobMap.get("steps"));
                            pipeline.addStage(pipelineStage);
                        }
                    }
                }
            } else if (root.containsKey("steps")) {
                PipelineStage pipelineStage = new PipelineStage("PipelineSteps");
                pipelineStage.setId("PipelineSteps");
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
                    PipelineStep step = new PipelineStep();
                    step.setName(stringValue(stepMap.get("displayName")));
                    Object stepEnv = stepMap.get("env");
                    if (stepEnv instanceof Map) {
                        step.setEnvironment(toStringMap((Map<?, ?>) stepEnv));
                    }

                    String script = stringValue(stepMap.get("script"));
                    if (script != null) {
                        step.setCommand(script);
                        stage.addStep(step);
                        continue;
                    }
                    
                    String bash = stringValue(stepMap.get("bash"));
                    if (bash != null) {
                        step.setCommand(bash);
                        stage.addStep(step);
                        continue;
                    }

                    String pwsh = stringValue(stepMap.get("pwsh"));
                    if (pwsh != null) {
                        step.setCommand(pwsh);
                        stage.addStep(step);
                        continue;
                    }

                    String powershell = stringValue(stepMap.get("powershell"));
                    if (powershell != null) {
                        step.setCommand(powershell);
                        stage.addStep(step);
                        continue;
                    }

                    String task = stringValue(stepMap.get("task"));
                    if (task != null) {
                        step.setUses("task: " + task);
                        Object inputs = stepMap.get("inputs");
                        if (inputs instanceof Map) {
                            step.setInputs(toStringMap((Map<?, ?>) inputs));
                        }
                        stage.addStep(step);
                    } else if (stepMap.containsKey("checkout")) {
                        step.setUses("checkout: " + stepMap.get("checkout"));
                        stage.addStep(step);
                    }
                }
            }
        }
    }

    private void addTriggerValues(ParsedPipeline pipeline, Object value) {
        pipeline.setTriggerConfiguration(value);
        if (value instanceof Map) {
            ((Map<?, ?>) value).keySet().forEach(key -> pipeline.addTrigger(String.valueOf(key)));
        } else if (value instanceof List) {
            ((List<?>) value).forEach(item -> pipeline.addTrigger(String.valueOf(item)));
        } else if (value instanceof String) {
            pipeline.addTrigger((String) value);
        }
    }

    private String readImage(Object value) {
        if (value instanceof Map) {
            Object image = ((Map<?, ?>) value).get("image");
            return image == null ? null : String.valueOf(image);
        }
        return value == null ? null : String.valueOf(value);
    }

    private String readPoolImage(Object pool, String fallback) {
        if (pool instanceof Map) {
            Object image = ((Map<?, ?>) pool).get("vmImage");
            return image == null ? fallback : String.valueOf(image);
        }
        return pool == null ? fallback : String.valueOf(pool);
    }

    private List<String> toStringList(Object value) {
        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) value) {
                result.add(String.valueOf(item));
            }
            return result;
        }
        if (value instanceof String) {
            return new ArrayList<>(List.of((String) value));
        }
        return new ArrayList<>();
    }

    private Map<String, String> toStringMap(Map<?, ?> source) {
        Map<String, String> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(String.valueOf(key), String.valueOf(value)));
        return result;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
