package cli.generator;

import java.util.ArrayList;
import java.util.List;

public class PipelineConversionResult {
    private final String content;
    private final List<String> warnings;

    public PipelineConversionResult(String content, List<String> warnings) {
        this.content = content;
        this.warnings = warnings == null ? new ArrayList<>() : new ArrayList<>(warnings);
    }

    public String getContent() {
        return content;
    }

    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
}
