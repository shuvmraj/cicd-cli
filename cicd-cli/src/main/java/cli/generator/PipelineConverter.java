package cli.generator;

import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.model.PipelineStep;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts the supported normalized pipeline semantics to a target platform.
 */
public class PipelineConverter {
    public PipelineConversionResult convert(ParsedPipeline pipeline, String targetPlatform) {
        String target = normalizePlatform(targetPlatform);
        List<String> warnings = new ArrayList<>();
        for (String feature : pipeline.getUnsupportedFeatures()) {
            if ("GitHub trigger filters".equals(feature) && "github".equals(target)) {
                continue;
            }
            warnings.add("Source feature may not be preserved: " + feature);
        }
        if (!"gitlab".equals(target) && pipeline.getStages().stream()
                .anyMatch(job -> !job.getArtifactDependencies().isEmpty())) {
            warnings.add("GitLab artifact-download dependencies need target-specific handling.");
        }
        if (pipeline.isHasCache() && !target.equals(pipeline.getPlatform())
                && !warnings.contains("Source cache configuration needs a target-platform implementation.")) {
            warnings.add("Source cache configuration needs a target-platform implementation.");
        }

        String output;
        switch (target) {
            case "github":
                output = dump(toGitHub(pipeline, warnings));
                break;
            case "gitlab":
                output = dump(toGitLab(pipeline, warnings));
                break;
            case "azure":
                output = dump(toAzure(pipeline, warnings));
                break;
            case "jenkins":
                output = toJenkins(pipeline, warnings);
                break;
            default:
                throw new IllegalArgumentException("Unsupported conversion target: " + targetPlatform);
        }
        return new PipelineConversionResult(output, warnings);
    }

    private Map<String, Object> toGitHub(ParsedPipeline pipeline, List<String> warnings) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("name", valueOr(pipeline.getName(), "Converted pipeline"));
        if ("github".equals(pipeline.getPlatform()) && pipeline.getTriggerConfiguration() != null) {
            root.put("on", pipeline.getTriggerConfiguration());
        } else {
            warnings.add(pipeline.getTriggers().isEmpty()
                    ? "No trigger was detected; configure the target workflow trigger."
                    : "Source trigger configuration was not translated to GitHub syntax; configure the target trigger.");
        }
        addMap(root, "env", pipeline.getEnvironment());

        Map<String, Object> jobs = new LinkedHashMap<>();
        Map<String, List<String>> jobsByStage = new LinkedHashMap<>();
        for (PipelineStage job : pipeline.getStages()) {
            jobsByStage.computeIfAbsent(valueOr(job.getStageName(), "jobs"), ignored -> new ArrayList<>())
                    .add(valueOr(job.getId(), job.getName()));
        }
        for (PipelineStage job : pipeline.getStages()) {
            Map<String, Object> jobMap = new LinkedHashMap<>();
            jobMap.put("name", job.getName());
            if (job.getRunner() != null) {
                jobMap.put("runs-on", job.getRunner());
            } else if (pipeline.getRunner() != null) {
                jobMap.put("runs-on", pipeline.getRunner());
            } else {
                warnings.add("No runner specified for job '" + job.getName() + "'.");
            }
            if (job.getContainerImage() != null) {
                jobMap.put("container", Map.of("image", job.getContainerImage()));
            } else if (pipeline.getDockerImage() != null) {
                jobMap.put("container", Map.of("image", pipeline.getDockerImage()));
            }
            Set<String> needs = new LinkedHashSet<>(job.getDependencies());
            for (String stageDependency : job.getStageDependencies()) {
                List<String> previousJobs = jobsByStage.get(stageDependency);
                if (previousJobs == null || previousJobs.isEmpty()) {
                    warnings.add("Could not map stage dependency '" + stageDependency
                            + "' for job '" + job.getName() + "'.");
                } else {
                    needs.addAll(previousJobs);
                }
            }
            if (!needs.isEmpty()) {
                jobMap.put("needs", new ArrayList<>(needs));
            }
            if (job.getCondition() != null && "github".equals(pipeline.getPlatform())) {
                jobMap.put("if", job.getCondition());
            } else if (job.getCondition() != null) {
                warnings.add("Condition on job '" + job.getName()
                        + "' uses source-platform syntax and needs target-specific review.");
            }
            addMap(jobMap, "env", job.getEnv());

            List<Map<String, Object>> steps = new ArrayList<>();
            for (PipelineStep step : job.getSteps()) {
                Map<String, Object> stepMap = new LinkedHashMap<>();
                if (step.getName() != null) {
                    stepMap.put("name", step.getName());
                }
                if (step.getUses() != null) {
                    stepMap.put("uses", step.getUses());
                    addMap(stepMap, "with", step.getInputs());
                } else if (step.getCommand() != null) {
                    stepMap.put("run", step.getCommand());
                }
                addMap(stepMap, "env", step.getEnvironment());
                if (!stepMap.isEmpty()) {
                    steps.add(stepMap);
                }
            }
            jobMap.put("steps", steps);
            jobs.put(valueOr(job.getId(), job.getName()), jobMap);
        }
        root.put("jobs", jobs);
        return root;
    }

    private Map<String, Object> toGitLab(ParsedPipeline pipeline, List<String> warnings) {
        Map<String, Object> root = new LinkedHashMap<>();
        List<String> stages = uniqueStageNames(pipeline.getStages());
        root.put("stages", stages);
        addMap(root, "variables", pipeline.getEnvironment());
        if (pipeline.getDockerImage() != null) {
            root.put("image", pipeline.getDockerImage());
        }
        if (!pipeline.getTriggers().isEmpty()) {
            warnings.add("GitLab trigger rules differ from the source platform and need review.");
        }
        if (!pipeline.getStages().stream().allMatch(job -> job.getStageDependencies().isEmpty())) {
            warnings.add("Explicit source stage dependencies are represented by GitLab stage ordering; verify the resulting order.");
        }

        for (PipelineStage job : pipeline.getStages()) {
            Map<String, Object> jobMap = new LinkedHashMap<>();
            jobMap.put("stage", valueOr(job.getStageName(), stages.isEmpty() ? "test" : stages.get(0)));
            if (job.getContainerImage() != null) {
                jobMap.put("image", job.getContainerImage());
            } else if (pipeline.getDockerImage() != null) {
                jobMap.put("image", pipeline.getDockerImage());
            }
            if (job.getRunner() != null) {
                jobMap.put("tags", List.of(job.getRunner()));
            } else if (pipeline.getRunner() != null) {
                jobMap.put("tags", List.of(pipeline.getRunner()));
            }
            if (!job.getDependencies().isEmpty()) {
                jobMap.put("needs", job.getDependencies());
            }
            if (!job.getArtifactDependencies().isEmpty()) {
                jobMap.put("dependencies", job.getArtifactDependencies());
            }
            addMap(jobMap, "variables", job.getEnv());
            List<String> scripts = new ArrayList<>();
            for (PipelineStep step : job.getSteps()) {
                if (step.getCommand() != null) {
                    scripts.add(step.getCommand());
                } else if (step.getUses() != null) {
                    warnings.add("GitLab cannot directly run source action '" + step.getUses()
                            + "' in job '" + job.getName() + "'.");
                }
            }
            jobMap.put("script", scripts);
            if (job.getCondition() != null) {
                warnings.add("Condition on job '" + job.getName()
                        + "' was not mapped to GitLab rules; review it manually.");
            }
            root.put(valueOr(job.getId(), job.getName()), jobMap);
        }
        return root;
    }

    private Map<String, Object> toAzure(ParsedPipeline pipeline, List<String> warnings) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("name", valueOr(pipeline.getName(), "Converted pipeline"));
        if ("azure".equals(pipeline.getPlatform()) && pipeline.getTriggerConfiguration() != null) {
            root.put("trigger", pipeline.getTriggerConfiguration());
        } else if ("azure".equals(pipeline.getPlatform()) && !pipeline.getTriggers().isEmpty()) {
            root.put("trigger", pipeline.getTriggers());
        } else {
            warnings.add(pipeline.getTriggers().isEmpty()
                    ? "No trigger was detected; configure the target pipeline trigger."
                    : "Source trigger configuration was not translated to Azure syntax; configure the target trigger.");
        }
        addMap(root, "variables", pipeline.getEnvironment());
        if (pipeline.getRunner() != null) {
            root.put("pool", Map.of("vmImage", pipeline.getRunner()));
        }
        if (pipeline.getDockerImage() != null) {
            root.put("container", pipeline.getDockerImage());
        }

        List<Map<String, Object>> stages = new ArrayList<>();
        Map<String, List<PipelineStage>> groupedJobs = new LinkedHashMap<>();
        for (PipelineStage job : pipeline.getStages()) {
            groupedJobs.computeIfAbsent(valueOr(job.getStageName(), "Build"), ignored -> new ArrayList<>()).add(job);
        }
        for (Map.Entry<String, List<PipelineStage>> entry : groupedJobs.entrySet()) {
            Map<String, Object> stageMap = new LinkedHashMap<>();
            stageMap.put("stage", entry.getKey());
            Set<String> stageDependencies = new LinkedHashSet<>();
            for (PipelineStage job : entry.getValue()) {
                stageDependencies.addAll(job.getStageDependencies());
            }
            if (!stageDependencies.isEmpty()) {
                stageMap.put("dependsOn", new ArrayList<>(stageDependencies));
            }
            List<Map<String, Object>> jobs = new ArrayList<>();
            for (PipelineStage job : entry.getValue()) {
                Map<String, Object> jobMap = new LinkedHashMap<>();
                jobMap.put("job", valueOr(job.getId(), job.getName()));
                if (!job.getDependencies().isEmpty()) {
                    jobMap.put("dependsOn", job.getDependencies());
                }
                String runner = valueOr(job.getRunner(), pipeline.getRunner());
                if (runner != null) {
                    jobMap.put("pool", Map.of("vmImage", runner));
                }
                if (job.getContainerImage() != null) {
                    jobMap.put("container", job.getContainerImage());
                }
                if (job.getCondition() != null && "azure".equals(pipeline.getPlatform())) {
                    jobMap.put("condition", job.getCondition());
                } else if (job.getCondition() != null) {
                    warnings.add("Condition on job '" + job.getName()
                            + "' uses source-platform syntax and needs Azure-specific review.");
                }
                addMap(jobMap, "variables", job.getEnv());

                List<Map<String, Object>> steps = new ArrayList<>();
                for (PipelineStep step : job.getSteps()) {
                    Map<String, Object> stepMap = new LinkedHashMap<>();
                    if (step.getName() != null) {
                        stepMap.put("displayName", step.getName());
                    }
                    if (step.getCommand() != null) {
                        stepMap.put("script", step.getCommand());
                    } else if (step.getUses() != null) {
                        if (step.getUses().startsWith("task: ")) {
                            stepMap.put("task", step.getUses().substring("task: ".length()));
                            addMap(stepMap, "inputs", step.getInputs());
                        } else if (step.getUses().startsWith("checkout: ")) {
                            stepMap.put("checkout", step.getUses().substring("checkout: ".length()));
                        } else {
                            warnings.add("Azure cannot directly run source action '" + step.getUses()
                                    + "' in job '" + job.getName() + "'.");
                        }
                    }
                    addMap(stepMap, "env", step.getEnvironment());
                    if (!stepMap.isEmpty()) {
                        steps.add(stepMap);
                    }
                }
                jobMap.put("steps", steps);
                jobs.add(jobMap);
            }
            stageMap.put("jobs", jobs);
            stages.add(stageMap);
        }
        root.put("stages", stages);
        return root;
    }

    private String toJenkins(ParsedPipeline pipeline, List<String> warnings) {
        if (!pipeline.getTriggers().isEmpty()) {
            warnings.add("Jenkins conversion does not preserve source trigger configuration.");
        }
        StringBuilder output = new StringBuilder("pipeline {\n    agent ");
        if (pipeline.getDockerImage() != null) {
            output.append("{ docker { image '").append(escapeGroovy(pipeline.getDockerImage())).append("' } }\n");
        } else if (pipeline.getRunner() != null) {
            output.append("{ label '").append(escapeGroovy(pipeline.getRunner())).append("' }\n");
        } else {
            output.append("any\n");
            warnings.add("No runner or container was detected; Jenkins will use any available agent.");
        }
        appendGroovyEnvironment(output, "    ", pipeline.getEnvironment());
        output.append("    stages {\n");
        for (PipelineStage job : pipeline.getStages()) {
            if (!job.getDependencies().isEmpty() || !job.getStageDependencies().isEmpty()) {
                warnings.add("Jenkins conversion does not preserve dependency scheduling for job '"
                        + job.getName() + "'.");
            }
            if (job.getCondition() != null) {
                warnings.add("Condition on job '" + job.getName() + "' needs a Jenkins-specific when rule.");
            }
            output.append("        stage('").append(escapeGroovy(job.getName())).append("') {\n");
            appendGroovyEnvironment(output, "            ", job.getEnv());
            output.append("            steps {\n");
            for (PipelineStep step : job.getSteps()) {
                if (step.getCommand() != null) {
                    output.append("                sh '''\n")
                            .append(step.getCommand()).append("\n                '''\n");
                } else if (step.getUses() != null) {
                    warnings.add("Jenkins cannot directly run source action '" + step.getUses()
                            + "' in job '" + job.getName() + "'.");
                }
            }
            output.append("            }\n        }\n");
        }
        output.append("    }\n}\n");
        return output.toString();
    }

    private void appendGroovyEnvironment(StringBuilder output, String indent, Map<String, String> values) {
        if (values.isEmpty()) {
            return;
        }
        output.append(indent).append("environment {\n");
        values.forEach((key, value) -> output.append(indent).append("    ")
                .append(key).append(" = '").append(escapeGroovy(value)).append("'\n"));
        output.append(indent).append("}\n");
    }

    private List<String> uniqueStageNames(List<PipelineStage> jobs) {
        Set<String> names = new LinkedHashSet<>();
        for (PipelineStage job : jobs) {
            names.add(valueOr(job.getStageName(), "test"));
        }
        return new ArrayList<>(names);
    }

    private void addMap(Map<String, Object> target, String key, Map<String, String> values) {
        if (values != null && !values.isEmpty()) {
            target.put(key, new LinkedHashMap<>(values));
        }
    }

    private String dump(Map<String, Object> document) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setSplitLines(false);
        options.setLineBreak(DumperOptions.LineBreak.UNIX);
        return new Yaml(options).dump(document);
    }

    private String normalizePlatform(String platform) {
        if (platform == null) {
            throw new IllegalArgumentException("Target platform cannot be null");
        }
        switch (platform.trim().toLowerCase()) {
            case "github":
            case "github-actions":
                return "github";
            case "gitlab":
            case "gitlab-ci":
                return "gitlab";
            case "azure":
            case "azure-devops":
            case "azure-pipelines":
                return "azure";
            case "jenkins":
            case "jenkinsfile":
                return "jenkins";
            default:
                throw new IllegalArgumentException("Unsupported conversion target: '" + platform
                        + "'. Supported: github, gitlab, jenkins, azure");
        }
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String escapeGroovy(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
