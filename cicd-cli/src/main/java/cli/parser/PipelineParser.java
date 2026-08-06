package cli.parser;

import cli.model.ParsedPipeline;

/**
 * Interface for parsing platform-specific pipeline files into a normalized ParsedPipeline.
 */
public interface PipelineParser {
    /**
     * Parses pipeline content into a normalized model.
     * @param content the raw string content of the pipeline file
     * @return the normalized ParsedPipeline
     */
    ParsedPipeline parse(String content);
}
