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
            return getParser("github");
        } else if (fileName.contains(".gitlab-ci") || fileName.contains("gitlab")) {
            return getParser("gitlab");
        } else if (fileName.contains("jenkinsfile") || fileName.contains("jenkins")) {
            return getParser("jenkins");
        } else if (fileName.contains("azure-pipelines") || fileName.contains("azure")) {
            return getParser("azure");
        }

        if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
            throw new IllegalArgumentException("Cannot infer pipeline platform from '" + fileName
                    + "'. Rename it to a standard platform file or specify the platform.");
        }

        throw new IllegalArgumentException("Unsupported pipeline file: " + fileName);
    }

    public static PipelineParser getParser(String platform) {
        if (platform == null) {
            throw new IllegalArgumentException("Source platform cannot be null");
        }
        switch (platform.trim().toLowerCase()) {
            case "github":
            case "github-actions":
                return new GitHubParser();
            case "gitlab":
            case "gitlab-ci":
                return new GitLabParser();
            case "jenkins":
            case "jenkinsfile":
                return new JenkinsParser();
            case "azure":
            case "azure-devops":
            case "azure-pipelines":
                return new AzureParser();
            default:
                throw new IllegalArgumentException("Unsupported source platform: '" + platform
                        + "'. Supported: github, gitlab, jenkins, azure");
        }
    }
}
