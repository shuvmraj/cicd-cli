package cli.parser;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

import java.io.StringReader;

final class YamlSourceLocations {
    private final Node root;

    YamlSourceLocations(String content) {
        this.root = new Yaml().compose(new StringReader(content));
    }

    int lineForPath(Object... path) {
        Node node = root;
        if (node == null) {
            return -1;
        }
        for (Object segment : path) {
            if (segment instanceof Integer && node instanceof SequenceNode) {
                SequenceNode sequence = (SequenceNode) node;
                int index = (Integer) segment;
                if (index < 0 || index >= sequence.getValue().size()) {
                    return -1;
                }
                node = sequence.getValue().get(index);
            } else if (segment instanceof String && node instanceof MappingNode) {
                node = mappingValue((MappingNode) node, (String) segment);
                if (node == null) {
                    return -1;
                }
            } else {
                return -1;
            }
        }
        return node.getStartMark() == null ? -1 : node.getStartMark().getLine() + 1;
    }

    private static Node mappingValue(MappingNode mapping, String key) {
        for (NodeTuple tuple : mapping.getValue()) {
            Node keyNode = tuple.getKeyNode();
            if (keyNode instanceof ScalarNode && key.equals(((ScalarNode) keyNode).getValue())) {
                return tuple.getValueNode();
            }
        }
        return null;
    }
}
