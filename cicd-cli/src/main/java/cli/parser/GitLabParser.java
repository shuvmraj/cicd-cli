package cli.parser;

import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.model.PipelineStep;
import org.yaml.snakeyaml.Yaml;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses GitLab CI YAML files.
 */
public class GitLabParser implements PipelineParser {
    private static final Set<String> RESERVED_KEYWORDS = new HashSet<>(Arrays.asList(
            "stages", "cache", "default", "variables", "before_script", 
            "after_script", "image", "services", "include", "workflow", "types"
    ));
    
    private static final Pattern SECRET_PATTERN = Pattern.compile("\\$(\\w*(?:SECRET|PASSWORD|TOKEN|KEY)\\w*)", Pattern.CASE_INSENSITIVE);

    @SuppressWarnings("unchecked")
    @Override
    public ParsedPipeline parse(String content) {
        ParsedPipeline pipeline = new ParsedPipeline();
        if (content == null || content.trim().isEmpty()) {
            pipeline.addError("GitLab CI content is empty");
            return pipeline;
        }

        try {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(content);
            if (root == null) {
                pipeline.addError("Invalid GitLab YAML structure");
                return pipeline;
            }

            YamlSourceLocations locations = new YamlSourceLocations(content);
            pipeline.setPlatform("gitlab");
            pipeline.setName("GitLab CI");

            Object stagesObj = root.get("stages");
            List<String> declaredStages = new ArrayList<>();
            if (stagesObj instanceof List) {
                for (Object stage : (List<?>) stagesObj) {
                    declaredStages.add(String.valueOf(stage));
                }
            }

            // 1. Global Image
            Object imageObj = root.get("image");
            if (imageObj instanceof String) {
                pipeline.setDockerImage((String) imageObj);
            } else if (imageObj instanceof Map) {
                Map<String, Object> imageMap = (Map<String, Object>) imageObj;
                Object name = imageMap.get("name");
                if (name != null) {
                    pipeline.setDockerImage(String.valueOf(name));
                }
            }

            // 2. Global variables
            Object varsObj = root.get("variables");
            if (varsObj instanceof Map) {
                toStringMap((Map<?, ?>) varsObj).forEach(pipeline.getEnvironment()::put);
            }

            // 3. Cache
            if (root.containsKey("cache") || content.contains("cache:")) {
                pipeline.setHasCache(true);
                pipeline.addUnsupportedFeature("GitLab cache configuration");
            }

            // 4. Scan secrets
            Matcher matcher = SECRET_PATTERN.matcher(content);
            while (matcher.find()) {
                pipeline.addSecret(matcher.group(1));
            }

            // 5. Parse Jobs
            for (Map.Entry<String, Object> entry : root.entrySet()) {
                String key = entry.getKey();
                if (RESERVED_KEYWORDS.contains(key.toLowerCase())) {
                    continue;
                }

                Object val = entry.getValue();
                if (val instanceof Map) {
                    Map<String, Object> jobMap = (Map<String, Object>) val;
                    if (jobMap.containsKey("script")) {
                        PipelineStage stage = new PipelineStage(key);
                        stage.setId(key);
                        stage.setSourceLine(locations.lineForPath(key));
                        Object stageValue = jobMap.get("stage");
                        stage.setStageName(stageValue == null
                                ? (declaredStages.isEmpty() ? "test" : declaredStages.get(0))
                                : String.valueOf(stageValue));
                        int currentStageIndex = declaredStages.indexOf(stage.getStageName());
                        if (!jobMap.containsKey("needs") && currentStageIndex > 0) {
                            stage.setStageDependencies(new ArrayList<>(
                                    declaredStages.subList(0, currentStageIndex)));
                        }

                        // Extract job-level image
                        Object jobImage = jobMap.get("image");
                        if (jobImage != null) {
                                stage.setContainerImage(readImage(jobImage));
                        }

                        // Extract dependencies (needs / dependencies)
                        Object needs = jobMap.get("needs");
                        if (needs instanceof List) {
                            List<?> needsList = (List<?>) needs;
                            for (Object need : needsList) {
                                if (need instanceof String) {
                                    stage.addDependency((String) need);
                                } else if (need instanceof Map) {
                                    Object jobName = ((Map<?, ?>) need).get("job");
                                    if (jobName != null) {
                                        stage.addDependency(String.valueOf(jobName));
                                    }
                                    if (((Map<?, ?>) need).size() > 1) {
                                        pipeline.addUnsupportedFeature("GitLab needs artifact/optional flags");
                                    }
                                }
                            }
                        }

                        Object dependencies = jobMap.get("dependencies");
                        if (dependencies instanceof List) {
                            List<?> depList = (List<?>) dependencies;
                            for (Object dep : depList) {
                                stage.addArtifactDependency(String.valueOf(dep));
                            }
                        }

                        // Extract job-level env variables
                        Object jobVarsObj = jobMap.get("variables");
                        if (jobVarsObj instanceof Map) {
                            toStringMap((Map<?, ?>) jobVarsObj).forEach(stage.getEnv()::put);
                        }

                        // Extract script commands
                        Object defaultObj = root.get("default");
                        Object defaultBefore = defaultObj instanceof Map
                                ? ((Map<?, ?>) defaultObj).get("before_script") : null;
                        Object beforeScript = jobMap.containsKey("before_script")
                                ? jobMap.get("before_script") : (root.get("before_script") != null
                                ? root.get("before_script") : defaultBefore);
                        addScriptCommands(stage, beforeScript);
                        addScriptCommands(stage, jobMap.get("script"));
                        Object afterScript = jobMap.containsKey("after_script")
                                ? jobMap.get("after_script") : root.get("after_script");
                        addScriptCommands(stage, afterScript);

                        Object condition = jobMap.get("rules");
                        if (condition != null) {
                            pipeline.addUnsupportedFeature("GitLab job rules");
                        } else if (jobMap.containsKey("only") || jobMap.containsKey("except")) {
                            pipeline.addUnsupportedFeature("GitLab only/except conditions");
                        }
                        if (jobMap.containsKey("artifacts")) {
                            pipeline.addUnsupportedFeature("GitLab job artifacts");
                        }
                        if (jobMap.containsKey("cache")) {
                            pipeline.setHasCache(true);
                            pipeline.addUnsupportedFeature("GitLab job cache configuration");
                        }

                        pipeline.addStage(stage);
                    }
                }
            }

            if (pipeline.getStages().isEmpty()) {
                pipeline.addWarning("No jobs with script block found in GitLab CI file");
            }
            if (root.containsKey("workflow")) {
                pipeline.addUnsupportedFeature("GitLab workflow rules");
            }

        } catch (Exception e) {
            pipeline.addError("Failed to parse GitLab CI YAML: " + e.getMessage());
        }

        return pipeline;
    }

    private void addScriptCommands(PipelineStage stage, Object scriptObj) {
        if (scriptObj instanceof String) {
            stage.addCommand((String) scriptObj);
        } else if (scriptObj instanceof List) {
            List<?> scriptList = (List<?>) scriptObj;
            for (Object line : scriptList) {
                stage.addCommand(String.valueOf(line));
            }
        }
    }

    private String readImage(Object image) {
        if (image instanceof Map) {
            Object name = ((Map<?, ?>) image).get("name");
            return name == null ? null : String.valueOf(name);
        }
        return String.valueOf(image);
    }

    private Map<String, String> toStringMap(Map<?, ?> source) {
        Map<String, String> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(String.valueOf(key), String.valueOf(value)));
        return result;
    }
}
