package cli.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single stage or job within a CI/CD pipeline.
 */
public class PipelineStage {
    private String name;
    private List<String> commands = new ArrayList<>();
    private List<String> dependencies = new ArrayList<>();
    private Map<String, String> env = new HashMap<>();

    public PipelineStage() {}

    public PipelineStage(String name) {
        this.name = name;
    }

    public PipelineStage(String name, List<String> commands) {
        this.name = name;
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public void addCommand(String command) {
        if (this.commands == null) {
            this.commands = new ArrayList<>();
        }
        this.commands.add(command);
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(String dependency) {
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<>();
        }
        this.dependencies.add(dependency);
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    @Override
    public String toString() {
        return "PipelineStage{" +
                "name='" + name + '\'' +
                ", commands=" + commands +
                ", dependencies=" + dependencies +
                ", env=" + env +
                '}';
    }
}
