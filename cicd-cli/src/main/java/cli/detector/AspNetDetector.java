package cli.detector;

import cli.model.ProjectModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Detects ASP.NET Core applications.
 */
public class AspNetDetector implements ProjectDetector {
    @Override
    public boolean detect(Path projectDir, ProjectModel model) {
        try (Stream<Path> list = Files.list(projectDir)) {
            boolean hasCsProj = list.anyMatch(path -> path.toString().endsWith(".csproj") || path.toString().endsWith(".sln"));
            if (hasCsProj) {
                model.setLanguage("C#");
                model.setFramework("ASP.NET Core");
                model.setBuildTool("dotnet");
                model.setTestFramework("xUnit");
                model.setPackageManager("nuget");
                model.setArtifactType("DIST");
                return true;
            }
        } catch (IOException ignored) {}
        return false;
    }
}
