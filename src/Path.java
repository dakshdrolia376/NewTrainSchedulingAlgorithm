import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class Path {

    private final Node node;
    private final double totalCost;

    public Path(Node source) {
        requireNonNull(source, "The input source node is null.");
        this.node = source;
        this.totalCost = 0.0;
    }

    private Path(Node node, double totalCost) {
        requireNonNull(node, "The input source node is null.");
        this.node = node;
        this.totalCost = totalCost;
    }


    public Path append(Edge edge) {
        requireNonNull(edge, "The input edge is null.");
        if (!node.equals(edge.getFrom())) {
            throw new IllegalArgumentException(format("The edge %s doesn't extend the path %s",
                    edge, this.getNodeList()));
        }
        return new NonEmptyPath(this, edge);
    }

    public Path appendNode(Node node, double weight) {
        requireNonNull(node, "The input node is null.");
        return new NonEmptyPath(this, node, weight);
    }

    @SuppressWarnings("unused")
    public Path removeNode() {
        return null;
    }

    public Path reverse() {
        return null;
    }

    public Node getEndNode() {
        return node;
    }

    public List<Node> getNodeList() {
        return new ArrayList<>();
    }

    @SuppressWarnings("unused")
    public List<Double> getWeightList() {
        return new ArrayList<>();
    }

    public double pathCost() {
        return totalCost;
    }

    private static class NonEmptyPath extends Path{
        private final Path predecessor;
        NonEmptyPath(Path path, Edge edge) {
            super(edge.getTo(), path.totalCost + edge.getWeight());
            predecessor = path;
        }

        NonEmptyPath(Path path, Node node, double weight) {
            super(node, weight);
            predecessor = path;
        }

        @Override
        public List<Node> getNodeList() {
            LinkedList<Node> result = new LinkedList<>();
            Path path = this;
            while(path instanceof NonEmptyPath) {
                result.addFirst(path.node);
                path = ((NonEmptyPath) path).predecessor;
            }
            result.addFirst(path.node);
            return result;
        }

        @Override
        public List<Double> getWeightList() {
            LinkedList<Double> result = new LinkedList<>();
            Path path = this;
            while(path instanceof NonEmptyPath) {
                result.addFirst(path.pathCost());
                path = ((NonEmptyPath) path).predecessor;
            }
            result.addFirst(path.pathCost());
            return result;
        }

        @Override
        public Path reverse() {
            List<Node> nodes = new ArrayList<>();
            List<Double> weights = new ArrayList<>();
            Path tempPath = this;
            while(tempPath instanceof NonEmptyPath) {
                nodes.add(tempPath.node);
                weights.add(tempPath.pathCost());
                tempPath = ((NonEmptyPath) tempPath).predecessor;
            }
            // Collections.reverse(nodes);
            Collections.reverse(weights);
            System.out.println(nodes.toString());
            System.out.println(weights.toString());
            if(nodes.size()>=1) {
                Path newPath = new Path(nodes.get(0));
                for (int i = 1; i < nodes.size(); i++) {
                    newPath = newPath.appendNode(nodes.get(i), weights.get(i));
                }
                return newPath;
            }
            else{
                System.out.println("Empty path");
                return null;
            }
        }

        @Override
        public Path removeNode() {
            return this.predecessor;
        }

        @Override
        public String toString() {
            return getNodeList().toString();
        }
    }
}