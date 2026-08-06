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

/**
 * Subcommand to explain a pipeline's lifecycle, stages, runtime, and parallelism.
 */
@Command(
        name = "explain",
        description = "Provides a structured explanation of a pipeline workflow, depicting stages flow and runtime estimations"
)
public class ExplainCommand implements Runnable {

    @Option(names = {"-f", "--file"}, description = "Path to the pipeline file")
    private String pipelineFile;

    @Option(names = {"-d", "--dir"}, description = "Project directory (used for auto-detection)")
    private String baseDir = ".";

    @Override
    public void run() {
        Path projectPath = Path.of(baseDir).toAbsolutePath().normalize();
        Path pipelinePath = null;

        if (pipelineFile != null) {
            pipelinePath = Path.of(pipelineFile).toAbsolutePath().normalize();
        } else {
            pipelinePath = autoDetectPipeline(projectPath);
        }

        if (pipelinePath == null || !Files.exists(pipelinePath)) {
            System.err.println(OutputFormatter.red("Error: No pipeline configuration file found to explain."));
            return;
        }

        try {
            String content = Files.readString(pipelinePath);
            PipelineParser parser = ParserFactory.getParser(pipelinePath);
            ParsedPipeline pipeline = parser.parse(content);

            List<PipelineStage> stages = pipeline.getStages();
            if (stages.isEmpty()) {
                System.out.println(OutputFormatter.yellow("The pipeline has no defined stages."));
                return;
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

            System.out.println("\n" + OutputFormatter.bold("Estimated Runtime"));
            System.out.println(minutes + " min");

            // Calculate parallel jobs
            int maxParallel = calculateParallelJobs(stages);
            System.out.println("\n" + OutputFormatter.bold("Parallel Jobs"));
            System.out.println(maxParallel);
            System.out.println();

        } catch (IOException e) {
            System.err.println(OutputFormatter.red("IO Error: Failed to read pipeline file: " + e.getMessage()));
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

    private int calculateParallelJobs(List<PipelineStage> stages) {
        // Build adjacency map of stage name -> dependencies
        Map<String, List<String>> dependencies = new HashMap<>();
        Map<String, Integer> levels = new HashMap<>();

        for (PipelineStage stage : stages) {
            dependencies.put(stage.getName().toLowerCase(), stage.getDependencies());
        }

        // Compute levels (longest path from a stage with 0 dependencies)
        boolean changed = true;
        int iterations = 0;
        // Limit iterations to prevent infinite loop on cycles (cycles are checked in validation)
        while (changed && iterations < stages.size()) {
            changed = false;
            iterations++;
            for (PipelineStage stage : stages) {
                String name = stage.getName().toLowerCase();
                List<String> deps = dependencies.get(name);
                int currentLevel = levels.getOrDefault(name, 0);
                int maxDepLevel = -1;

                for (String dep : deps) {
                    Integer depLevel = levels.get(dep.toLowerCase());
                    if (depLevel != null) {
                        maxDepLevel = Math.max(maxDepLevel, depLevel);
                    } else {
                        // Dependency is not processed yet or doesn't exist
                        maxDepLevel = Math.max(maxDepLevel, 0);
                    }
                }

                int newLevel = maxDepLevel + 1;
                if (newLevel != currentLevel) {
                    levels.put(name, newLevel);
                    changed = true;
                }
            }
        }

        // Count how many stages are in each level
        Map<Integer, Integer> levelCounts = new HashMap<>();
        for (int lv : levels.values()) {
            levelCounts.put(lv, levelCounts.getOrDefault(lv, 0) + 1);
        }

        // Maximum count at any level represents max parallel jobs
        int maxParallel = 1;
        for (int count : levelCounts.values()) {
            maxParallel = Math.max(maxParallel, count);
        }

        return maxParallel;
    }

    private Path autoDetectPipeline(Path root) {
        Path githubDir = root.resolve(".github").resolve("workflows");
        if (Files.exists(githubDir) && Files.isDirectory(githubDir)) {
            try (Stream<Path> files = Files.list(githubDir)) {
                Path workflow = files.filter(f -> f.toString().endsWith(".yml") || f.toString().endsWith(".yaml"))
                        .findFirst()
                        .orElse(null);
                if (workflow != null) return workflow;
            } catch (IOException ignored) {}
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
