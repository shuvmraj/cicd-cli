package cli.commands;

import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.parser.ParserFactory;
import cli.parser.PipelineParser;
import cli.utils.OutputFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.concurrent.Callable;

/**
 * Subcommand to explain a pipeline's lifecycle, stages, runtime, and parallelism.
 */
@Command(
        name = "explain",
        description = "Provides a structured explanation of a pipeline workflow, depicting stages flow and runtime estimations"
)
public class ExplainCommand implements Callable<Integer> {

    @Option(names = {"-f", "--file"}, description = "Path to the pipeline file")
    private String pipelineFile;

    @Option(names = {"-d", "--dir"}, description = "Project directory (used for auto-detection)")
    private String baseDir = ".";

    @Option(names = {"--platform"}, description = "Pipeline platform for files with non-standard names")
    private String platform;

    @Override
    public Integer call() {
        Path projectPath = Path.of(baseDir).toAbsolutePath().normalize();
        try {
            Path pipelinePath = pipelineFile != null
                    ? Path.of(pipelineFile).toAbsolutePath().normalize()
                    : autoDetectPipeline(projectPath);
            if (pipelinePath == null || !Files.isRegularFile(pipelinePath)) {
                System.err.println(OutputFormatter.red("Error: No pipeline configuration file found to explain."));
                return 1;
            }
            String content = Files.readString(pipelinePath);
            PipelineParser parser = platform == null
                    ? ParserFactory.getParser(pipelinePath)
                    : ParserFactory.getParser(platform);
            ParsedPipeline pipeline = parser.parse(content);
            if (!pipeline.getErrors().isEmpty()) {
                pipeline.getErrors().forEach(error ->
                        System.err.println(OutputFormatter.red("Parse error: " + error)));
                return 1;
            }

            List<PipelineStage> stages = pipeline.getStages();
            if (stages.isEmpty()) {
                System.out.println(OutputFormatter.yellow("The pipeline has no defined stages."));
                return 0;
            }

            // Print flow diagram
            System.out.println("\n" + OutputFormatter.bold("Pipeline") + "\n");
            for (int i = 0; i < stages.size(); i++) {
                PipelineStage stage = stages.get(i);
                System.out.println("↓");
                System.out.println(OutputFormatter.cyan(stage.getName()));
            }

            // Estimate Runtime
            int estimatedSeconds = estimateRuntime(stages);
            int minutes = (int) Math.ceil(estimatedSeconds / 60.0);

            System.out.println("\n" + OutputFormatter.bold("Rough Heuristic Runtime Estimate (not measured)"));
            System.out.println(minutes + " min");

            // Calculate parallel jobs
            Integer maxParallel = calculateParallelJobs(stages);
            System.out.println("\n" + OutputFormatter.bold("Maximum Jobs at the Same Dependency Depth"));
            if (maxParallel == null) {
                System.out.println("Unavailable because the dependency graph contains a cycle.");
            } else {
                System.out.println(maxParallel + " (uses parsed job/stage dependencies; ignores runner capacity and platform limits)");
            }
            System.out.println();

            for (String warning : pipeline.getWarnings()) {
                System.err.println(OutputFormatter.yellow("Warning: " + warning));
            }
            return 0;
        } catch (IllegalArgumentException e) {
            System.err.println(OutputFormatter.red("Pipeline error: " + e.getMessage()));
            return 2;
        } catch (IOException e) {
            System.err.println(OutputFormatter.red("IO Error: Failed to read pipeline file: " + e.getMessage()));
            return 1;
        }
    }

    private int estimateRuntime(List<PipelineStage> stages) {
        int total = 0;
        for (PipelineStage stage : stages) {
            String name = stage.getName().toLowerCase();
            boolean hasNpm = false;
            boolean hasMvn = false;
            boolean hasDocker = false;
            boolean hasDeploy = false;

            for (String cmd : stage.getCommands()) {
                String c = cmd.toLowerCase();
                if (c.contains("npm install") || c.contains("yarn install") || c.contains("pnpm install")) hasNpm = true;
                if (c.contains("mvn") || c.contains("gradle")) hasMvn = true;
                if (c.contains("docker build") || c.contains("build-push-action")) hasDocker = true;
                if (c.contains("kubectl") || c.contains("helm")) hasDeploy = true;
            }

            if (name.contains("install") || hasNpm) {
                total += 90;
            } else if (name.contains("compile") || name.contains("build") || hasMvn) {
                if (hasDocker || name.contains("docker")) {
                    total += 120;
                } else {
                    total += 90;
                }
            } else if (name.contains("test") || name.contains("spec")) {
                total += 60;
            } else if (name.contains("deploy") || hasDeploy) {
                total += 60;
            } else {
                total += 30; // default/unknown stage
            }
        }
        return total;
    }

    private Integer calculateParallelJobs(List<PipelineStage> stages) {
        Map<String, PipelineStage> jobs = new LinkedHashMap<>();
        Map<String, List<String>> dependents = new LinkedHashMap<>();
        Map<String, Integer> remainingDependencies = new LinkedHashMap<>();
        Map<String, List<String>> jobsByStage = new LinkedHashMap<>();
        for (PipelineStage stage : stages) {
            String id = identifier(stage).toLowerCase(Locale.ROOT);
            jobs.put(id, stage);
            dependents.put(id, new ArrayList<>());
            remainingDependencies.put(id, 0);
            if (stage.getStageName() != null) {
                jobsByStage.computeIfAbsent(stage.getStageName().toLowerCase(Locale.ROOT),
                        ignored -> new ArrayList<>()).add(id);
            }
        }
        for (Map.Entry<String, PipelineStage> entry : jobs.entrySet()) {
            String id = entry.getKey();
            Set<String> dependencyIds = new LinkedHashSet<>();
            for (String dependency : entry.getValue().getDependencies()) {
                String dependencyId = dependency.toLowerCase(Locale.ROOT);
                dependencyIds.add(dependencyId);
            }
            for (String stageDependency : entry.getValue().getStageDependencies()) {
                dependencyIds.addAll(jobsByStage.getOrDefault(
                        stageDependency.toLowerCase(Locale.ROOT), Collections.emptyList()));
            }
            for (String dependencyId : dependencyIds) {
                if (jobs.containsKey(dependencyId) && !dependencyId.equals(id)) {
                    dependents.get(dependencyId).add(id);
                    remainingDependencies.put(id, remainingDependencies.get(id) + 1);
                }
            }
        }

        List<String> ready = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : remainingDependencies.entrySet()) {
            if (entry.getValue() == 0) {
                ready.add(entry.getKey());
            }
        }
        int visited = 0;
        int maximumAtDepth = 0;
        while (!ready.isEmpty()) {
            maximumAtDepth = Math.max(maximumAtDepth, ready.size());
            List<String> next = new ArrayList<>();
            for (String finished : ready) {
                visited++;
                for (String dependent : dependents.get(finished)) {
                    int remaining = remainingDependencies.get(dependent) - 1;
                    remainingDependencies.put(dependent, remaining);
                    if (remaining == 0) next.add(dependent);
                }
            }
            ready = next;
        }
        return visited == stages.size() ? maximumAtDepth : null;
    }

    private String identifier(PipelineStage stage) {
        return stage.getId() == null || stage.getId().isBlank() ? stage.getName() : stage.getId();
    }

    private Path autoDetectPipeline(Path root) throws IOException {
        Path githubDir = root.resolve(".github").resolve("workflows");
        if (Files.exists(githubDir) && Files.isDirectory(githubDir)) {
            try (Stream<Path> files = Files.list(githubDir)) {
                Path workflow = files.filter(Files::isRegularFile)
                        .filter(f -> f.toString().endsWith(".yml") || f.toString().endsWith(".yaml"))
                        .sorted()
                        .findFirst()
                        .orElse(null);
                if (workflow != null) return workflow;
            }
        }
        Path gitlab = root.resolve(".gitlab-ci.yml");
        if (Files.exists(gitlab)) return gitlab;
        Path jenkins = root.resolve("Jenkinsfile");
        if (Files.exists(jenkins)) return jenkins;
        Path azure = root.resolve("azure-pipelines.yml");
        if (Files.exists(azure)) return azure;
        return null;
    }
}
