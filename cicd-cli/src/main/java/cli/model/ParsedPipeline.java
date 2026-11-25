package cli.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Normalized model representing a parsed CI/CD pipeline.
 */
public class ParsedPipeline {
    private String platform;
    private String name;
    private List<PipelineStage> stages = new ArrayList<>();
    private Map<String, String> environment = new LinkedHashMap<>();
    private List<String> triggers = new ArrayList<>();
    private Object triggerConfiguration;
    private List<String> secrets = new ArrayList<>();
    private boolean hasCache;
    private String dockerImage;
    private String runner;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private List<String> unsupportedFeatures = new ArrayList<>();

    public ParsedPipeline() {}

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PipelineStage> getStages() {
        return stages;
    }

    public void setStages(List<PipelineStage> stages) {
        this.stages = stages;
    }

    public void addStage(PipelineStage stage) {
        if (this.stages == null) {
            this.stages = new ArrayList<>();
        }
        this.stages.add(stage);
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment != null ? new LinkedHashMap<>(environment) : new LinkedHashMap<>();
    }

    public List<String> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<String> triggers) {
        this.triggers = triggers != null ? new ArrayList<>(triggers) : new ArrayList<>();
    }

    public void addTrigger(String trigger) {
        triggers.add(trigger);
    }

    public Object getTriggerConfiguration() {
        return triggerConfiguration;
    }

    public void setTriggerConfiguration(Object triggerConfiguration) {
        this.triggerConfiguration = triggerConfiguration;
    }

    public List<String> getSecrets() {
        return secrets;
    }

    public void setSecrets(List<String> secrets) {
        this.secrets = secrets;
    }

    public void addSecret(String secret) {
        if (secret != null && !secrets.contains(secret)) {
            secrets.add(secret);
        }
        this.secrets.add(secret);
    }

    public boolean isHasCache() {
        return hasCache;
    }

    public void setHasCache(boolean hasCache) {
        this.hasCache = hasCache;
    }

    public String getDockerImage() {
        return dockerImage;
    }

    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }

    public String getRunner() {
        return runner;
    }

    public void setRunner(String runner) {
        this.runner = runner;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public List<String> getUnsupportedFeatures() {
        return unsupportedFeatures;
    }

    public void addUnsupportedFeature(String feature) {
        if (feature != null && !unsupportedFeatures.contains(feature)) {
            unsupportedFeatures.add(feature);
        }
    }

    @Override
    public String toString() {
        return "ParsedPipeline{" +
                "stages=" + stages +
                ", platform='" + platform + '\'' +
                ", environment=" + environment +
                ", triggers=" + triggers +
                ", secrets=" + secrets +
                ", hasCache=" + hasCache +
                ", dockerImage='" + dockerImage + '\'' +
                ", runner='" + runner + '\'' +
                ", errors=" + errors +
                ", warnings=" + warnings +
                ", unsupportedFeatures=" + unsupportedFeatures +
                '}';
    }
}
