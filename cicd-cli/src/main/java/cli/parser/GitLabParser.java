package cli.parser;

import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
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
                Map<String, Object> varsMap = (Map<String, Object>) varsObj;
                varsMap.forEach((k, v) -> pipeline.getEnvironment().put(k, String.valueOf(v)));
            }

            // 3. Cache
            if (root.containsKey("cache") || content.contains("cache:")) {
                pipeline.setHasCache(true);
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

                        // Extract job-level image
                        Object jobImage = jobMap.get("image");
                        if (jobImage != null) {
                            if (jobImage instanceof String) {
                                pipeline.setDockerImage((String) jobImage);
                            } else if (jobImage instanceof Map) {
                                Map<String, Object> imageMap = (Map<String, Object>) jobImage;
                                Object name = imageMap.get("name");
                                if (name != null) {
                                    pipeline.setDockerImage(String.valueOf(name));
                                }
                            }
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
                                }
                            }
                        }

                        Object dependencies = jobMap.get("dependencies");
                        if (dependencies instanceof List) {
                            List<?> depList = (List<?>) dependencies;
                            for (Object dep : depList) {
                                stage.addDependency(String.valueOf(dep));
                            }
                        }

                        // Extract job-level env variables
                        Object jobVarsObj = jobMap.get("variables");
                        if (jobVarsObj instanceof Map) {
                            Map<String, Object> jobVarsMap = (Map<String, Object>) jobVarsObj;
                            jobVarsMap.forEach((k, v) -> stage.getEnv().put(k, String.valueOf(v)));
                        }

                        // Extract script commands
                        addScriptCommands(stage, jobMap.get("before_script"));
                        addScriptCommands(stage, jobMap.get("script"));
                        addScriptCommands(stage, jobMap.get("after_script"));

                        pipeline.addStage(stage);
                    }
                }
            }

            if (pipeline.getStages().isEmpty()) {
                pipeline.addWarning("No jobs with script block found in GitLab CI file");
            }

        } catch (Exception e) {
            pipeline.addError("Failed to parse GitLab CI YAML: " + e.getMessage());
        }

        return pipeline;
    }

    private void addScriptCommands(PipelineStage stage, Object scriptObj) {
        if (scriptObj instanceof String) {
            stage.addCommand(((String) scriptObj).trim());
        } else if (scriptObj instanceof List) {
            List<?> scriptList = (List<?>) scriptObj;
            for (Object line : scriptList) {
                stage.addCommand(String.valueOf(line).trim());
            }
        }
    }
}
