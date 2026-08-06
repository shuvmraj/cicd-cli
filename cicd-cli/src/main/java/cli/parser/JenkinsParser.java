package cli.parser;

import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Jenkins declarative pipelines using structural regex and text scanning.
 */
public class JenkinsParser implements PipelineParser {
    private static final Pattern AGENT_IMAGE_PATTERN = Pattern.compile("image\\s+['\"]([^'\"]+)['\"]");
    private static final Pattern ENV_PATTERN = Pattern.compile("(\\w+)\\s*=\\s*['\"]([^'\"]+)['\"]");
    private static final Pattern SECRET_PATTERN = Pattern.compile("credentials\\(['\"]([^'\"]+)['\"]\\)");
    
    // Pattern to match: stage('Stage Name') {
    private static final Pattern STAGE_HEADER_PATTERN = Pattern.compile("stage\\s*\\(\\s*['\"]([^'\"]+)['\"]\\s*\\)\\s*\\{");
    
    // Command patterns
    private static final Pattern SH_SINGLE_LINE_PATTERN = Pattern.compile("sh\\s+['\"]([^'\"]+)['\"]");
    private static final Pattern SH_TRIPLE_QUOTE_PATTERN = Pattern.compile("sh\\s+'''(.*?)'''", Pattern.DOTALL);

    @Override
    public ParsedPipeline parse(String content) {
        ParsedPipeline pipeline = new ParsedPipeline();
        if (content == null || content.trim().isEmpty()) {
            pipeline.addError("Jenkinsfile content is empty");
            return pipeline;
        }

        // 1. Image detection
        Matcher imageMatcher = AGENT_IMAGE_PATTERN.matcher(content);
        if (imageMatcher.find()) {
            pipeline.setDockerImage(imageMatcher.group(1));
        }

        // 2. Environment variables detection
        // Find environment block
        int envIndex = content.indexOf("environment {");
        if (envIndex != -1) {
            String envBlock = extractBlock(content, envIndex + 12);
            Matcher envMatcher = ENV_PATTERN.matcher(envBlock);
            while (envMatcher.find()) {
                pipeline.getEnvironment().put(envMatcher.group(1), envMatcher.group(2));
            }
        }

        // 3. Secrets / Credentials detection
        Matcher secretMatcher = SECRET_PATTERN.matcher(content);
        while (secretMatcher.find()) {
            pipeline.addSecret(secretMatcher.group(1));
        }

        // 4. Cache detection
        if (content.contains("cache") || content.contains("restoreSheets") || content.contains("stash")) {
            pipeline.setHasCache(true);
        }

        // 5. Parse Stages
        Matcher stageHeaderMatcher = STAGE_HEADER_PATTERN.matcher(content);
        int searchStart = 0;
        while (stageHeaderMatcher.find(searchStart)) {
            String stageName = stageHeaderMatcher.group(1);
            int stageContentIndex = stageHeaderMatcher.end() - 1; // Position of '{'
            String stageBlock = extractBlock(content, stageContentIndex);

            PipelineStage stage = new PipelineStage(stageName);

            // Extract sh commands inside stage
            Matcher shSingle = SH_SINGLE_LINE_PATTERN.matcher(stageBlock);
            while (shSingle.find()) {
                stage.addCommand(shSingle.group(1).trim());
            }

            Matcher shTriple = SH_TRIPLE_QUOTE_PATTERN.matcher(stageBlock);
            while (shTriple.find()) {
                for (String line : shTriple.group(1).split("\\r?\\n")) {
                    if (!line.trim().isEmpty()) {
                        stage.addCommand(line.trim());
                    }
                }
            }

            pipeline.addStage(stage);
            searchStart = stageHeaderMatcher.end();
        }

        if (pipeline.getStages().isEmpty()) {
            pipeline.addWarning("No stage definitions found in Jenkinsfile");
        }

        return pipeline;
    }

    /**
     * Extracts a code block matching matching curly braces.
     * @param content whole document content
     * @param openBraceIndex position of the opening curly brace '{'
     * @return the content of the block (excluding outer braces)
     */
    private String extractBlock(String content, int openBraceIndex) {
        if (openBraceIndex < 0 || openBraceIndex >= content.length() || content.charAt(openBraceIndex) != '{') {
            return "";
        }

        int braceCount = 1;
        int i = openBraceIndex + 1;
        while (i < content.length() && braceCount > 0) {
            char c = content.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
            }
            i++;
        }

        if (braceCount == 0) {
            return content.substring(openBraceIndex + 1, i - 1);
        }
        return content.substring(openBraceIndex + 1);
    }
}
