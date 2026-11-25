package cli;

import cli.generator.PipelineConversionResult;
import cli.generator.PipelineConverter;
import cli.model.ParsedPipeline;
import cli.parser.GitHubParser;
import cli.parser.GitLabParser;
import cli.parser.AzureParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PipelineConverterTest {
    private final PipelineConverter converter = new PipelineConverter();

    @Test
    void githubConversionPreservesWorkflowAndJobSemantics() {
        String source = "name: Build\n"
                + "on: [push, pull_request]\n"
                + "env:\n"
                + "  MODE: release\n"
                + "jobs:\n"
                + "  build:\n"
                + "    runs-on: ubuntu-latest\n"
                + "    container: node:20\n"
                + "    steps:\n"
                + "      - name: Checkout\n"
                + "        uses: actions/checkout@v4\n"
                + "        with:\n"
                + "          fetch-depth: 0\n"
                + "      - run: npm run build\n"
                + "  test:\n"
                + "    needs: build\n"
                + "    runs-on: ubuntu-latest\n"
                + "    steps:\n"
                + "      - run: npm test\n";

        ParsedPipeline sourcePipeline = new GitHubParser().parse(source);
        PipelineConversionResult result = converter.convert(sourcePipeline, "github-actions");
        ParsedPipeline converted = new GitHubParser().parse(result.getContent());

        assertTrue(converted.getErrors().isEmpty());
        assertEquals(sourcePipeline.getTriggers(), converted.getTriggers());
        assertEquals("release", converted.getEnvironment().get("MODE"));
        assertEquals("node:20", converted.getStages().get(0).getContainerImage());
        assertEquals("npm run build", converted.getStages().get(0).getCommands().get(0));
        assertEquals("build", converted.getStages().get(1).getDependencies().get(0));
        assertEquals("0", converted.getStages().get(0).getSteps().get(0).getInputs().get("fetch-depth"));
        assertTrue(result.getWarnings().isEmpty());
    }

    @Test
    void gitlabConversionPreservesJobStagesAndReportsUnsupportedActions() {
        String source = "stages: [build, test]\n"
                + "build:\n"
                + "  stage: build\n"
                + "  image: node:20\n"
                + "  script: npm run build\n"
                + "test:\n"
                + "  stage: test\n"
                + "  needs: [build]\n"
                + "  script: npm test\n";

        ParsedPipeline sourcePipeline = new GitLabParser().parse(source);
        String output = converter.convert(sourcePipeline, "gitlab").getContent();
        ParsedPipeline converted = new GitLabParser().parse(output);

        assertTrue(converted.getErrors().isEmpty());
        assertEquals("build", converted.getStages().get(0).getStageName());
        assertEquals("node:20", converted.getStages().get(0).getContainerImage());
        assertEquals("npm test", converted.getStages().get(1).getCommands().get(0));
        assertEquals("build", converted.getStages().get(1).getDependencies().get(0));

        sourcePipeline.getStages().get(0).getSteps().get(0).setCommand(null);
        sourcePipeline.getStages().get(0).getSteps().get(0).setUses("actions/cache@v4");
        PipelineConversionResult withUnsupportedAction = converter.convert(sourcePipeline, "gitlab");
        assertTrue(withUnsupportedAction.getWarnings().stream()
                .anyMatch(warning -> warning.contains("cannot directly run source action")));
    }

    @Test
    void azureConversionRetainsStageAndJobDependencyLevels() {
        ParsedPipeline pipeline = new ParsedPipeline();
        pipeline.setName("build");
        pipeline.setPlatform("azure");
        pipeline.setRunner("ubuntu-latest");
        pipeline.addTrigger("main");
        cli.model.PipelineStage compile = new cli.model.PipelineStage("compile");
        compile.setId("compile");
        compile.setStageName("Build");
        compile.addCommand("mvn package");
        pipeline.addStage(compile);
        cli.model.PipelineStage verify = new cli.model.PipelineStage("verify");
        verify.setId("verify");
        verify.setStageName("Test");
        verify.addStageDependency("Build");
        verify.addDependency("compile");
        verify.addCommand("mvn test");
        pipeline.addStage(verify);

        PipelineConversionResult result = converter.convert(pipeline, "azure");
        ParsedPipeline converted = new AzureParser().parse(result.getContent());
        assertTrue(converted.getErrors().isEmpty());
        assertTrue(converted.getStages().get(1).getStageDependencies().contains("Build"));
        assertTrue(converted.getStages().get(1).getDependencies().contains("compile"));
        assertEquals("mvn test", converted.getStages().get(1).getCommands().get(0));
        assertTrue(result.getWarnings().isEmpty());
    }

    @Test
    void gitlabArtifactDependenciesAreNotMistakenForSchedulingDependencies() {
        String source = "stages: [build, deploy]\n"
                + "compile:\n"
                + "  stage: build\n"
                + "  script: npm run build\n"
                + "  artifacts:\n"
                + "    paths: [dist]\n"
                + "deploy:\n"
                + "  stage: deploy\n"
                + "  dependencies: [compile]\n"
                + "  script: echo deploy\n";

        ParsedPipeline parsed = new GitLabParser().parse(source);

        assertTrue(parsed.getStages().get(1).getDependencies().isEmpty());
        assertEquals("compile", parsed.getStages().get(1).getArtifactDependencies().get(0));
        PipelineConversionResult result = converter.convert(parsed, "github");
        assertTrue(result.getWarnings().stream()
                .anyMatch(warning -> warning.contains("artifact-download dependencies")));
        assertTrue(result.getContent().contains("needs:"));
    }
}
