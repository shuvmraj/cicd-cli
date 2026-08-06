package cli.generator;

import cli.model.ProjectModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Base generator holding shared logic for template context resolution.
 */
public abstract class BaseGenerator implements PipelineGenerator {
    protected final TemplateEngine templateEngine;

    protected BaseGenerator() {
        this.templateEngine = new TemplateEngine();
    }

    /**
     * Resolves generic variables from the ProjectModel.
     */
    protected Map<String, Object> buildContext(ProjectModel project) {
        Map<String, Object> context = new HashMap<>();

        String buildTool = project.getBuildTool() != null ? project.getBuildTool() : "None";
        String testFramework = project.getTestFramework() != null ? project.getTestFramework() : "None";
        String framework = project.getFramework() != null ? project.getFramework() : "Unknown";
        String deploymentTarget = project.getDeploymentTarget() != null ? project.getDeploymentTarget() : "None";

        context.put("LANGUAGE", project.getLanguage());
        context.put("FRAMEWORK", framework);
        context.put("BUILD_TOOL", buildTool);
        context.put("DOCKER_ENABLED", project.isDockerEnabled());
        context.put("KUBERNETES_ENABLED", project.isKubernetesEnabled());
        context.put("DEPLOYMENT_TARGET", deploymentTarget);
        context.put("leftBrace", "{{");
        context.put("rightBrace", "}}");

        // 1. Resolve DOCKER_IMAGE
        context.put("DOCKER_IMAGE", getDockerImage(framework, buildTool));

        // 2. Resolve BUILD_COMMAND
        context.put("BUILD_COMMAND", getBuildCommand(buildTool));

        // 3. Resolve TEST_COMMAND
        context.put("TEST_COMMAND", getTestCommand(testFramework, buildTool));

        // 4. Resolve DEPLOY_COMMAND
        context.put("DEPLOY_COMMAND", getDeployCommand(deploymentTarget));

        // 5. Resolve CACHE_KEY & CACHE_PATH
        context.put("CACHE_KEY", getCacheKey(buildTool));
        context.put("CACHE_PATH", getCachePath(buildTool));

        // 6. Resolve ARTIFACT_PATH
        context.put("ARTIFACT_PATH", getArtifactPath(project.getArtifactType(), buildTool));

        return context;
    }

    private String getDockerImage(String framework, String buildTool) {
        if ("Spring Boot".equals(framework)) {
            return "Gradle".equalsIgnoreCase(buildTool) ? "gradle:8.6-jdk21" : "maven:3.9-eclipse-temurin-21";
        }
        if ("React".equals(framework) || "Angular".equals(framework) || "Vue".equals(framework) || "Next.js".equals(framework) || "Express".equals(framework)) {
            return "node:20";
        }
        if ("Django".equals(framework) || "Flask".equals(framework)) {
            return "python:3.11";
        }
        if ("Laravel".equals(framework)) {
            return "php:8.2-fpm";
        }
        if ("ASP.NET Core".equals(framework)) {
            return "mcr.microsoft.com/dotnet/sdk:8.0";
        }
        if ("HTML/CSS/JavaScript".equals(framework)) {
            return "nginx:alpine";
        }
        return "ubuntu:latest";
    }

    private String getBuildCommand(String buildTool) {
        if (buildTool == null) return "echo 'No build tool'";
        switch (buildTool.toLowerCase()) {
            case "maven": return "mvn clean package -DskipTests";
            case "gradle": return "./gradlew build -x test";
            case "npm": return "npm install && npm run build";
            case "yarn": return "yarn install && yarn build";
            case "pnpm": return "pnpm install && pnpm build";
            case "composer": return "composer install --no-ansi --no-interaction --no-progress";
            case "dotnet": return "dotnet restore && dotnet build --configuration Release";
            case "poetry": return "poetry install && poetry build";
            case "pip": return "pip install -r requirements.txt";
            default: return "echo 'No compile/build step required.'";
        }
    }

    private String getTestCommand(String testFramework, String buildTool) {
        if (testFramework == null || "none".equalsIgnoreCase(testFramework)) {
            return "echo 'No tests configured'";
        }
        switch (testFramework.toLowerCase()) {
            case "junit":
                return "Gradle".equalsIgnoreCase(buildTool) ? "./gradlew test" : "mvn test";
            case "jest":
                if ("yarn".equalsIgnoreCase(buildTool)) return "yarn test";
                if ("pnpm".equalsIgnoreCase(buildTool)) return "pnpm test";
                return "npm test";
            case "vitest":
                if ("yarn".equalsIgnoreCase(buildTool)) return "yarn run test";
                if ("pnpm".equalsIgnoreCase(buildTool)) return "pnpm run test";
                return "npm run test";
            case "karma/protractor":
                return "npm test";
            case "pytest":
                if ("poetry".equalsIgnoreCase(buildTool)) return "poetry run pytest";
                return "pytest";
            case "phpunit":
                return "vendor/bin/phpunit";
            case "xunit":
                return "dotnet test";
            default:
                return "echo 'Running unit tests...'";
        }
    }

    private String getDeployCommand(String deploymentTarget) {
        if ("Kubernetes".equalsIgnoreCase(deploymentTarget)) {
            return "kubectl apply -f deployment.yaml";
        }
        if ("Docker".equalsIgnoreCase(deploymentTarget)) {
            return "docker build -t my-app:latest . && docker run -d -p 8080:8080 my-app:latest";
        }
        if ("Static Hosting".equalsIgnoreCase(deploymentTarget)) {
            return "echo 'Uploading build/dist artifacts to static hosting provider (e.g. AWS S3, Vercel, Netlify)...'";
        }
        return "echo 'Deploying package application...'";
    }

    private String getCacheKey(String buildTool) {
        if (buildTool == null) return "general-cache";
        return buildTool.toLowerCase() + "-cache";
    }

    private String getCachePath(String buildTool) {
        if (buildTool == null) return ".cache";
        switch (buildTool.toLowerCase()) {
            case "maven": return "~/.m2/repository";
            case "gradle": return "~/.gradle/caches";
            case "npm":
            case "yarn":
            case "pnpm":
                return "node_modules";
            case "composer": return "vendor";
            case "dotnet": return "~/.nuget/packages";
            case "poetry": return "~/.cache/pypoetry";
            case "pip": return "~/.cache/pip";
            default: return ".cache";
        }
    }

    private String getArtifactPath(String artifactType, String buildTool) {
        if (artifactType == null) return ".";
        switch (artifactType.toUpperCase()) {
            case "JAR":
            case "WAR":
                return "Gradle".equalsIgnoreCase(buildTool) ? "build/libs/*.jar" : "target/*.jar";
            case "DIST":
            case "STATIC":
                return "dist";
            default:
                return ".";
        }
    }
}
