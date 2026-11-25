package cli.utils;

import cli.model.ProjectModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectConfigService {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void apply(Path projectDir, ProjectModel model) throws IOException {
        Path configPath = projectDir.resolve(".cicd-config.json");
        if (!Files.exists(configPath)) {
            return;
        }
        JsonNode config = MAPPER.readTree(configPath.toFile());
        if (config == null || !config.isObject()) {
            throw new IOException("Expected a JSON object in " + configPath);
        }

        String language = readOptionalText(config, "overrideLanguage", configPath);
        String framework = readOptionalText(config, "overrideFramework", configPath);
        String buildCommand = readOptionalText(config, "customBuildCommand", configPath);
        String testCommand = readOptionalText(config, "customTestCommand", configPath);
        if (language != null) model.setLanguage(language);
        if (framework != null) model.setFramework(framework);
        if (buildCommand != null) model.setCustomBuildCommand(buildCommand);
        if (testCommand != null) model.setCustomTestCommand(testCommand);
    }

    private String readOptionalText(JsonNode config, String field, Path configPath) throws IOException {
        JsonNode value = config.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isTextual()) {
            throw new IOException("Field '" + field + "' must be a string or null in " + configPath);
        }
        String text = value.asText().trim();
        return text.isEmpty() ? null : text;
    }
}
