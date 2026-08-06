package cli.parser;

import java.nio.file.Path;

/**
 * Factory class to resolve the correct PipelineParser based on file name or path.
 */
public class ParserFactory {
    
    /**
     * Resolves parser based on file name and path.
     * @param filePath the path to the pipeline file
     * @return the appropriate PipelineParser
     */
    public static PipelineParser getParser(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        String absolutePath = filePath.toAbsolutePath().toString().toLowerCase();

        if (absolutePath.contains(".github") || fileName.contains("github-actions") || fileName.contains("github")) {
            return new GitHubParser();
        } else if (fileName.contains(".gitlab-ci") || fileName.contains("gitlab")) {
            return new GitLabParser();
        } else if (fileName.contains("jenkinsfile") || fileName.contains("jenkins")) {
            return new JenkinsParser();
        } else if (fileName.contains("azure-pipelines") || fileName.contains("azure")) {
            return new AzureParser();
        }

        // Default fallback: check file contents or guess from extensions
        if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
            // If it contains jobs and runs-on, guess github
            // Let's default to GitHubParser for generic yml, or return a parser that doesn't crash.
            return new GitHubParser();
        }

        return new JenkinsParser();
    }
}
