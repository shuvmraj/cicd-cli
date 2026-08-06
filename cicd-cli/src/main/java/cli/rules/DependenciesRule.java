package cli.rules;

import cli.model.ProjectModel;
import cli.model.ParsedPipeline;
import cli.model.PipelineStage;
import cli.validation.Severity;
import cli.validation.ValidationIssue;
import java.util.*;

/**
 * Validates dependencies between pipeline stages to detect cycle dependencies.
 */
public class DependenciesRule implements Rule {
    @Override
    public String getName() {
        return "DependenciesRule";
    }

    @Override
    public String getDescription() {
        return "Detects dependency cycles and scheduling conflicts between pipeline stages.";
    }

    @Override
    public List<ValidationIssue> evaluate(ProjectModel project, ParsedPipeline pipeline) {
        List<ValidationIssue> issues = new ArrayList<>();
        List<PipelineStage> stages = pipeline.getStages();

        // Map of stage name to its dependencies
        Map<String, List<String>> adjList = new HashMap<>();
        for (PipelineStage stage : stages) {
            String name = stage.getName().trim();
            List<String> deps = new ArrayList<>();
            for (String dep : stage.getDependencies()) {
                deps.add(dep.trim());
            }
            adjList.put(name, deps);
        }

        // Check for cycles using DFS
        // States: 0 = UNVISITED, 1 = VISITING, 2 = VISITED
        Map<String, Integer> state = new HashMap<>();
        for (String node : adjList.keySet()) {
            state.put(node, 0);
        }

        List<String> cyclePath = new ArrayList<>();
        for (String node : adjList.keySet()) {
            if (state.get(node) == 0) {
                if (hasCycleDFS(node, adjList, state, cyclePath)) {
                    // format cycle description
                    Collections.reverse(cyclePath);
                    String cycleStr = String.join(" -> ", cyclePath);
                    issues.add(new ValidationIssue(
                            Severity.ERROR,
                            "Circular dependency detected in stages execution path: " + cycleStr + " -> " + node,
                            getName()
                    ));
                    // Stop after finding first cycle to prevent spamming
                    break;
                }
            }
        }

        // Check if any declared dependency references a stage that does not exist in the pipeline
        for (PipelineStage stage : stages) {
            for (String dep : stage.getDependencies()) {
                boolean found = false;
                for (PipelineStage s : stages) {
                    if (s.getName().trim().equalsIgnoreCase(dep.trim())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    issues.add(new ValidationIssue(
                            Severity.WARNING,
                            "Stage '" + stage.getName() + "' depends on non-existent stage '" + dep + "'.",
                            getName()
                    ));
                }
            }
        }

        return issues;
    }

    private boolean hasCycleDFS(String curr, Map<String, List<String>> adjList, Map<String, Integer> state, List<String> path) {
        state.put(curr, 1); // visiting
        path.add(curr);

        List<String> neighbors = adjList.get(curr);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                // If neighbor is not defined in the stages, skip it (will be caught by the non-existent check)
                if (!state.containsKey(neighbor)) {
                    continue;
                }
                
                int neighborState = state.get(neighbor);
                if (neighborState == 1) {
                    // Cycle detected!
                    return true;
                } else if (neighborState == 0) {
                    if (hasCycleDFS(neighbor, adjList, state, path)) {
                        return true;
                    }
                }
            }
        }

        state.put(curr, 2); // visited
        path.remove(path.size() - 1);
        return false;
    }
}
