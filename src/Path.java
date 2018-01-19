import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class Path<V> {

    private final V node;
    private final double totalCost;

    public Path(V source) {
        requireNonNull(source, "The input source node is null.");
        node = source;
        totalCost = 0.0;
    }

    private Path(V node, double totalCost) {
        requireNonNull(node, "The input source node is null.");
        this.node = node;
        this.totalCost = totalCost;
    }


    public Path<V> append(Edge<V> edge) {
        requireNonNull(edge, "The input edge is null.");
        if (!node.equals(edge.from)) {
            throw new IllegalArgumentException(format("The edge %s doesn't extend the path %s", edge, this.getNodeList()));
        }
        return new NonEmptyPath<>(this, edge);
    }

    public V getEndNode() {
        return node;
    }

    public List<V> getNodeList() {
        return new ArrayList<>();
    }

    public double pathCost() {
        return totalCost;
    }

    private static class NonEmptyPath<V> extends Path<V> {
        private final Path<V> predecessor;

        NonEmptyPath(Path<V> path, Edge<V> edge) {
            super(edge.to, path.totalCost + edge.weight);
            predecessor = path;
        }

        @Override
        public List<V> getNodeList() {
            LinkedList<V> result = new LinkedList<>();
            Path<V> path = this;
            while(path instanceof NonEmptyPath) {
                result.addFirst(path.node);
                path = ((NonEmptyPath<V>) path).predecessor;
            }
            result.addFirst(path.node);
            return result;
        }

        @Override
        public String toString() {
            return getNodeList().toString();
        }
    }
}