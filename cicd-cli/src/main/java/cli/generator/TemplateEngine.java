package cli.generator;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Renders pipeline configs using Mustache templates loaded from resources.
 */
public class TemplateEngine {
    private final MustacheFactory mustacheFactory;

    public TemplateEngine() {
        this.mustacheFactory = new DefaultMustacheFactory();
    }

    /**
     * Renders a resource template with the given context variables.
     * @param templateResourcePath resource path (e.g. "/templates/github/actions-template.yml")
     * @param context Map of properties
     * @return rendered content
     * @throws IOException if the template fails to load
     */
    public String render(String templateResourcePath, Map<String, Object> context) throws IOException {
        InputStream is = getClass().getResourceAsStream(templateResourcePath);
        if (is == null) {
            throw new FileNotFoundException("Resource template not found: " + templateResourcePath);
        }

        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Mustache mustache = mustacheFactory.compile(reader, templateResourcePath);
            StringWriter writer = new StringWriter();
            mustache.execute(writer, context).flush();
            return writer.toString();
        }
    }
}
