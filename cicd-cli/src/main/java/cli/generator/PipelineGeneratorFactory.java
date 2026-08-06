package cli.generator;

/**
 * Factory class resolving the correct PipelineGenerator strategy.
 */
public class PipelineGeneratorFactory {
    
    /**
     * Resolves the appropriate strategy based on platform name.
     * @param platform target platform (github, gitlab, jenkins, azure)
     * @return the PipelineGenerator implementation
     */
    public static PipelineGenerator getGenerator(String platform) {
        if (platform == null) {
            throw new IllegalArgumentException("Platform name cannot be null");
        }
        
        switch (platform.trim().toLowerCase()) {
            case "github":
            case "github-actions":
                return new GitHubGenerator();
            case "gitlab":
            case "gitlab-ci":
                return new GitLabGenerator();
            case "jenkins":
            case "jenkinsfile":
                return new JenkinsGenerator();
            case "azure":
            case "azure-devops":
            case "azure-pipelines":
                return new AzureGenerator();
            case "docker":
            case "dockerfile":
                return new DockerGenerator();
            default:
                throw new IllegalArgumentException("Unsupported CI/CD platform: '" + platform + 
                        "'. Supported: github, gitlab, jenkins, azure, docker");
        }
    }
}
