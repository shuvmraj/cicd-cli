package cli.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A pipeline action or shell command.
 */
public class PipelineStep {
    private String name;
    private String command;
    private String uses;
    private int sourceLine = -1;
    private Map<String, String> inputs = new LinkedHashMap<>();
    private Map<String, String> environment = new LinkedHashMap<>();

    public PipelineStep() {}

    public static PipelineStep run(String command) {
        PipelineStep step = new PipelineStep();
        step.setCommand(command);
        return step;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getUses() {
        return uses;
    }

    public void setUses(String uses) {
        this.uses = uses;
    }

    public int getSourceLine() {
        return sourceLine;
    }

    public void setSourceLine(int sourceLine) {
        this.sourceLine = sourceLine;
    }

    public Map<String, String> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, String> inputs) {
        this.inputs = inputs != null ? new LinkedHashMap<>(inputs) : new LinkedHashMap<>();
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment != null ? new LinkedHashMap<>(environment) : new LinkedHashMap<>();
    }
}
