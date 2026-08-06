package cli.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Normalized model representing a parsed CI/CD pipeline.
 */
public class ParsedPipeline {
    private List<PipelineStage> stages = new ArrayList<>();
    private Map<String, String> environment = new HashMap<>();
    private List<String> secrets = new ArrayList<>();
    private boolean hasCache;
    private String dockerImage;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public ParsedPipeline() {}

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
        this.environment = environment;
    }

    public List<String> getSecrets() {
        return secrets;
    }

    public void setSecrets(List<String> secrets) {
        this.secrets = secrets;
    }

    public void addSecret(String secret) {
        if (this.secrets == null) {
            this.secrets = new ArrayList<>();
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
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    @Override
    public String toString() {
        return "ParsedPipeline{" +
                "stages=" + stages +
                ", environment=" + environment +
                ", secrets=" + secrets +
                ", hasCache=" + hasCache +
                ", dockerImage='" + dockerImage + '\'' +
                ", errors=" + errors +
                ", warnings=" + warnings +
                '}';
    }
}
