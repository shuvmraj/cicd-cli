package cli.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single stage or job within a CI/CD pipeline.
 */
public class PipelineStage {
    private String id;
    private String name;
    private String stageName;
    private String runner;
    private String containerImage;
    private String condition;
    private int sourceLine = -1;
    private List<PipelineStep> steps = new ArrayList<>();
    private List<String> dependencies = new ArrayList<>();
    private List<String> stageDependencies = new ArrayList<>();
    private List<String> artifactDependencies = new ArrayList<>();
    private Map<String, String> env = new LinkedHashMap<>();

    public PipelineStage() {}

    public PipelineStage(String name) {
        this.id = name;
        this.name = name;
    }

    public PipelineStage(String name, List<String> commands) {
        this.id = name;
        this.name = name;
        setCommands(commands);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getRunner() {
        return runner;
    }

    public void setRunner(String runner) {
        this.runner = runner;
    }

    public String getContainerImage() {
        return containerImage;
    }

    public void setContainerImage(String containerImage) {
        this.containerImage = containerImage;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public int getSourceLine() {
        return sourceLine;
    }

    public void setSourceLine(int sourceLine) {
        this.sourceLine = sourceLine;
    }

    public List<PipelineStep> getSteps() {
        return steps;
    }

    public void setSteps(List<PipelineStep> steps) {
        this.steps = steps != null ? new ArrayList<>(steps) : new ArrayList<>();
    }

    public List<String> getCommands() {
        List<String> commands = new ArrayList<>();
        for (PipelineStep step : steps) {
            if (step.getCommand() != null) {
                commands.add(step.getCommand());
            }
        }
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.steps = new ArrayList<>();
        if (commands != null) {
            for (String command : commands) {
                addCommand(command);
            }
        }
    }

    public void addCommand(String command) {
        steps.add(PipelineStep.run(command));
    }

    public void addStep(PipelineStep step) {
        if (step != null) {
            steps.add(step);
        }
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
    }

    public void addDependency(String dependency) {
        dependencies.add(dependency);
    }

    public List<String> getStageDependencies() {
        return stageDependencies;
    }

    public void setStageDependencies(List<String> stageDependencies) {
        this.stageDependencies = stageDependencies != null ? new ArrayList<>(stageDependencies) : new ArrayList<>();
    }

    public void addStageDependency(String dependency) {
        stageDependencies.add(dependency);
    }

    public List<String> getArtifactDependencies() {
        return artifactDependencies;
    }

    public void setArtifactDependencies(List<String> artifactDependencies) {
        this.artifactDependencies = artifactDependencies != null
                ? new ArrayList<>(artifactDependencies) : new ArrayList<>();
    }

    public void addArtifactDependency(String dependency) {
        artifactDependencies.add(dependency);
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env != null ? new LinkedHashMap<>(env) : new LinkedHashMap<>();
    }

    @Override
    public String toString() {
        return "PipelineStage{" +
                "name='" + name + '\'' +
                ", steps=" + steps.size() +
                ", dependencies=" + dependencies +
                ", stageName='" + stageName + '\'' +
                ", env=" + env +
                '}';
    }
}
