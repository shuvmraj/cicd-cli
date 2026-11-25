package cli;

import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.model.PipelineStep;
import cli.parser.GitHubParser;
import cli.parser.GitLabParser;
import cli.parser.JenkinsParser;
import cli.parser.AzureParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    @Test
    public void testGitHubParser() {
        String yaml = "name: CI\n" +
                "env:\n" +
                "  GLOBAL_VAR: hello\n" +
                "jobs:\n" +
                "  build:\n" +
                "    runs-on: ubuntu-latest\n" +
                "    steps:\n" +
                "      - name: Build\n" +
                "        run: mvn clean package\n" +
                "  test:\n" +
                "    needs: build\n" +
                "    runs-on: ubuntu-latest\n" +
                "    steps:\n" +
                "      - name: Run Tests\n" +
                "        run: mvn test\n" +
                "        env:\n" +
                "          PASSWORD: secrets.DB_PASS";

        GitHubParser parser = new GitHubParser();
        ParsedPipeline pipeline = parser.parse(yaml);

        assertNotNull(pipeline);
        assertEquals(2, pipeline.getStages().size());
        assertEquals("hello", pipeline.getEnvironment().get("GLOBAL_VAR"));
        assertNull(pipeline.getDockerImage());

        PipelineStage buildStage = pipeline.getStages().stream()
                .filter(s -> s.getName().equals("build")).findFirst().orElse(null);
        assertNotNull(buildStage);
        assertEquals("ubuntu-latest", buildStage.getRunner());
        assertEquals("mvn clean package", buildStage.getCommands().get(0));

        PipelineStage testStage = pipeline.getStages().stream()
                .filter(s -> s.getName().equals("test")).findFirst().orElse(null);
        assertNotNull(testStage);
        assertTrue(testStage.getDependencies().contains("build"));
    }

    @Test
    public void testGitLabParser() {
        String yaml = "image: node:20\n" +
                "variables:\n" +
                "  API_KEY: \"$MY_API_KEY\"\n" +
                "stages:\n" +
                "  - test\n" +
                "test-job:\n" +
                "  stage: test\n" +
                "  script:\n" +
                "    - npm install\n" +
                "    - npm test";

        GitLabParser parser = new GitLabParser();
        ParsedPipeline pipeline = parser.parse(yaml);

        assertNotNull(pipeline);
        assertEquals("node:20", pipeline.getDockerImage());
        assertEquals("$MY_API_KEY", pipeline.getEnvironment().get("API_KEY"));
        assertEquals(1, pipeline.getStages().size());
        
        PipelineStage stage = pipeline.getStages().get(0);
        assertEquals("test-job", stage.getName());
        assertEquals("test", stage.getStageName());
        assertEquals(2, stage.getCommands().size());
        assertEquals("npm install", stage.getCommands().get(0));
    }

    @Test
    public void testJenkinsParser() {
        String jenkinsfile = "pipeline {\n" +
                "    agent { docker { image 'node:20' } }\n" +
                "    environment {\n" +
                "        DB_USER = 'admin'\n" +
                "    }\n" +
                "    stages {\n" +
                "        stage('Build') {\n" +
                "            steps {\n" +
                "                sh 'npm run build'\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        JenkinsParser parser = new JenkinsParser();
        ParsedPipeline pipeline = parser.parse(jenkinsfile);

        assertNotNull(pipeline);
        assertEquals("node:20", pipeline.getDockerImage());
        assertEquals("admin", pipeline.getEnvironment().get("DB_USER"));
        assertEquals(1, pipeline.getStages().size());
        assertEquals("Build", pipeline.getStages().get(0).getName());
        assertEquals("npm run build", pipeline.getStages().get(0).getCommands().get(0));
    }

    @Test
    public void testAzureParser() {
        String yaml = "pool:\n" +
                "  vmImage: 'ubuntu-latest'\n" +
                "variables:\n" +
                "  target: 'prod'\n" +
                "steps:\n" +
                "  - script: echo hello\n" +
                "  - bash: echo world";

        AzureParser parser = new AzureParser();
        ParsedPipeline pipeline = parser.parse(yaml);

        assertNotNull(pipeline);
        assertEquals("ubuntu-latest", pipeline.getRunner());
        assertEquals("prod", pipeline.getEnvironment().get("target"));
        assertEquals(1, pipeline.getStages().size());
        assertEquals("echo hello", pipeline.getStages().get(0).getCommands().get(0));
        assertEquals("echo world", pipeline.getStages().get(0).getCommands().get(1));
    }

    @Test
    public void testGitHubParserPreservesActionInputsAndTriggers() {
        String yaml = "name: Build\n"
                + "on:\n"
                + "  push:\n"
                + "    branches: [main]\n"
                + "jobs:\n"
                + "  verify:\n"
                + "    runs-on: ubuntu-latest\n"
                + "    container:\n"
                + "      image: node:20\n"
                + "    steps:\n"
                + "      - name: Checkout\n"
                + "        uses: actions/checkout@v4\n"
                + "        with:\n"
                + "          fetch-depth: 0\n"
                + "      - name: Test\n"
                + "        run: npm test\n";

        ParsedPipeline pipeline = new GitHubParser().parse(yaml);

        assertEquals("github", pipeline.getPlatform());
        assertEquals("Build", pipeline.getName());
        assertTrue(pipeline.getTriggers().contains("push"));
        PipelineStage job = pipeline.getStages().get(0);
        assertEquals("ubuntu-latest", job.getRunner());
        assertEquals("node:20", job.getContainerImage());
        PipelineStep checkout = job.getSteps().get(0);
        assertEquals("actions/checkout@v4", checkout.getUses());
        assertEquals("0", checkout.getInputs().get("fetch-depth"));
        assertEquals("npm test", job.getCommands().get(0));
    }

    @Test
    public void testAzureParserKeepsJobsAndStageDependenciesSeparate() {
        String yaml = "stages:\n"
                + "  - stage: Build\n"
                + "    jobs:\n"
                + "      - job: compile\n"
                + "        steps:\n"
                + "          - script: mvn package\n"
                + "      - job: verify\n"
                + "        dependsOn: compile\n"
                + "        steps:\n"
                + "          - script: mvn test\n"
                + "  - stage: Publish\n"
                + "    dependsOn: Build\n"
                + "    jobs:\n"
                + "      - job: publish\n"
                + "        steps:\n"
                + "          - script: echo publish\n";

        ParsedPipeline pipeline = new AzureParser().parse(yaml);

        assertEquals(3, pipeline.getStages().size());
        assertEquals("Build", pipeline.getStages().get(0).getStageName());
        assertEquals("compile", pipeline.getStages().get(0).getId());
        assertTrue(pipeline.getStages().get(1).getDependencies().contains("compile"));
        assertTrue(pipeline.getStages().get(2).getStageDependencies().contains("Build"));
    }
}
