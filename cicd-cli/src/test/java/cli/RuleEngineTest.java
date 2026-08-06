package cli;

import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.model.ProjectModel;
import cli.rules.*;
import cli.validation.Severity;
import cli.validation.ValidationIssue;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RuleEngineTest {

    @Test
    public void testBuildStageRule() {
        ProjectModel project = new ProjectModel.Builder()
                .buildTool("Maven")
                .framework("Spring Boot")
                .build();

        // 1. Missing Build stage
        ParsedPipeline p1 = new ParsedPipeline();
        p1.addStage(new PipelineStage("test", List.of("mvn test")));

        BuildStageRule rule = new BuildStageRule();
        List<ValidationIssue> issues = rule.evaluate(project, p1);
        assertEquals(1, issues.size());
        assertEquals(Severity.ERROR, issues.get(0).getSeverity());
        assertTrue(issues.get(0).getMessage().contains("Build stage is missing"));

        // 2. Misconfigured Build stage (no maven commands)
        ParsedPipeline p2 = new ParsedPipeline();
        p2.addStage(new PipelineStage("build", List.of("echo 'building'")));
        issues = rule.evaluate(project, p2);
        assertEquals(1, issues.size());
        assertEquals(Severity.WARNING, issues.get(0).getSeverity());
        assertTrue(issues.get(0).getMessage().contains("none contain expected keyword: 'mvn'"));
    }

    @Test
    public void testDependenciesRule() {
        DependenciesRule rule = new DependenciesRule();

        // Cyclic dependency: A -> B -> C -> A
        ParsedPipeline pipeline = new ParsedPipeline();
        
        PipelineStage a = new PipelineStage("A");
        a.addDependency("C");
        pipeline.addStage(a);

        PipelineStage b = new PipelineStage("B");
        b.addDependency("A");
        pipeline.addStage(b);

        PipelineStage c = new PipelineStage("C");
        c.addDependency("B");
        pipeline.addStage(c);

        List<ValidationIssue> issues = rule.evaluate(new ProjectModel(), pipeline);
        assertEquals(1, issues.size());
        assertEquals(Severity.ERROR, issues.get(0).getSeverity());
        assertTrue(issues.get(0).getMessage().contains("Circular dependency detected"));
    }

    @Test
    public void testSecretsRule() {
        SecretsRule rule = new SecretsRule();

        ParsedPipeline pipeline = new ParsedPipeline();
        PipelineStage stage = new PipelineStage("build");
        stage.addCommand("docker login -u user -p mySuperSecretPassword123");
        pipeline.addStage(stage);

        List<ValidationIssue> issues = rule.evaluate(new ProjectModel(), pipeline);
        assertEquals(1, issues.size());
        assertEquals(Severity.ERROR, issues.get(0).getSeverity());
        assertTrue(issues.get(0).getMessage().contains("Potential hardcoded secret detected"));
    }

    @Test
    public void testDockerRule() {
        DockerRule rule = new DockerRule();
        ProjectModel project = new ProjectModel.Builder().dockerEnabled(true).build();

        // Invalid docker tag with uppercase repository
        ParsedPipeline pipeline = new ParsedPipeline();
        PipelineStage stage = new PipelineStage("docker-build");
        stage.addCommand("docker build -t MyRepo/app:latest .");
        pipeline.addStage(stage);

        List<ValidationIssue> issues = rule.evaluate(project, pipeline);
        
        // Should find invalid naming error
        boolean hasError = issues.stream()
                .anyMatch(issue -> issue.getSeverity() == Severity.ERROR && issue.getMessage().contains("Invalid Docker image naming format"));
        assertTrue(hasError);
    }
}
