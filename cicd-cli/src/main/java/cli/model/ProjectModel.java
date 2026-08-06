package cli.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Normalized model representing a software project's technology stack.
 * Using Strings for technology fields (framework, language, etc.) instead of strict enums
 * allows adding new detector/template modules without changing the core engine.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectModel {
    private String language;
    private String framework;
    private String buildTool;
    private String testFramework;
    private boolean dockerEnabled;
    private boolean kubernetesEnabled;
    private String artifactType;
    private String packageManager;
    private String deploymentTarget;

    public ProjectModel() {}

    public ProjectModel(Builder builder) {
        this.language = builder.language;
        this.framework = builder.framework;
        this.buildTool = builder.buildTool;
        this.testFramework = builder.testFramework;
        this.dockerEnabled = builder.dockerEnabled;
        this.kubernetesEnabled = builder.kubernetesEnabled;
        this.artifactType = builder.artifactType;
        this.packageManager = builder.packageManager;
        this.deploymentTarget = builder.deploymentTarget;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public String getBuildTool() {
        return buildTool;
    }

    public void setBuildTool(String buildTool) {
        this.buildTool = buildTool;
    }

    public String getTestFramework() {
        return testFramework;
    }

    public void setTestFramework(String testFramework) {
        this.testFramework = testFramework;
    }

    public boolean isDockerEnabled() {
        return dockerEnabled;
    }

    public void setDockerEnabled(boolean dockerEnabled) {
        this.dockerEnabled = dockerEnabled;
    }

    public boolean isKubernetesEnabled() {
        return kubernetesEnabled;
    }

    public void setKubernetesEnabled(boolean kubernetesEnabled) {
        this.kubernetesEnabled = kubernetesEnabled;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }

    public String getPackageManager() {
        return packageManager;
    }

    public void setPackageManager(String packageManager) {
        this.packageManager = packageManager;
    }

    public String getDeploymentTarget() {
        return deploymentTarget;
    }

    public void setDeploymentTarget(String deploymentTarget) {
        this.deploymentTarget = deploymentTarget;
    }

    @Override
    public String toString() {
        return "ProjectModel{" +
                "language='" + language + '\'' +
                ", framework='" + framework + '\'' +
                ", buildTool='" + buildTool + '\'' +
                ", testFramework='" + testFramework + '\'' +
                ", dockerEnabled=" + dockerEnabled +
                ", kubernetesEnabled=" + kubernetesEnabled +
                ", artifactType='" + artifactType + '\'' +
                ", packageManager='" + packageManager + '\'' +
                ", deploymentTarget='" + deploymentTarget + '\'' +
                '}';
    }

    public static class Builder {
        private String language;
        private String framework;
        private String buildTool;
        private String testFramework;
        private boolean dockerEnabled;
        private boolean kubernetesEnabled;
        private String artifactType;
        private String packageManager;
        private String deploymentTarget;

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder framework(String framework) {
            this.framework = framework;
            return this;
        }

        public Builder buildTool(String buildTool) {
            this.buildTool = buildTool;
            return this;
        }

        public Builder testFramework(String testFramework) {
            this.testFramework = testFramework;
            return this;
        }

        public Builder dockerEnabled(boolean dockerEnabled) {
            this.dockerEnabled = dockerEnabled;
            return this;
        }

        public Builder kubernetesEnabled(boolean kubernetesEnabled) {
            this.kubernetesEnabled = kubernetesEnabled;
            return this;
        }

        public Builder artifactType(String artifactType) {
            this.artifactType = artifactType;
            return this;
        }

        public Builder packageManager(String packageManager) {
            this.packageManager = packageManager;
            return this;
        }

        public Builder deploymentTarget(String deploymentTarget) {
            this.deploymentTarget = deploymentTarget;
            return this;
        }

        public ProjectModel build() {
            return new ProjectModel(this);
        }
    }
}
