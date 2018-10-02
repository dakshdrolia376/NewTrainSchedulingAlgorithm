package iitp.naman.newtrainschedulingalgorithm;

import iitp.naman.newtrainschedulingalgorithm.util.TrainTime;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Class to store path info.
 */
public class Path {
    private final Node node;
    private final double totalCost;
    private final int length;
    private TrainTime sourceTime;
    private int unScheduledStop = 0;

    public Path(Node source) {
        requireNonNull(source, "The input source node is null.");
        this.node = source;
        this.totalCost = 0.0;
        this.length = 1;
        this.sourceTime = source.getTime();
    }

    private Path(Node node, double totalCost, int length, int unScheduledStop) {
        requireNonNull(node, "The input source node is null.");
        this.node = node;
        this.totalCost = totalCost;
        this.length = length;
        this.sourceTime = node.getTime();
        this.unScheduledStop = unScheduledStop;
    }

    /**
     * @return length of path.
     */
    public int getLength() {
        return this.length;
    }

    /**
     * @return source time for the path.
     */
    public TrainTime getSourceTime() {
        if (this.sourceTime != null) {
            return new TrainTime(this.sourceTime);
        }
        return null;
    }

    /**
     * @param edge edge to append in the path.
     * @return new path with appended edge.
     */
    public Path append(Edge edge) {
        requireNonNull(edge, "The input edge is null.");
        if (!this.node.equals(edge.getFrom())) {
            throw new IllegalArgumentException(format("The edge %s doesn't extend the path %s",
                    edge, this.getNodeList()));
        }
        return new NonEmptyPath(this, edge);
    }

    /**
     * @param node   node to append in the path.
     * @param weight weight of the edge.
     * @return new path with appended edge.
     */
    public Path append(Node node, double weight) {
        requireNonNull(node, "The input node is null.");
        return new NonEmptyPath(this, node, weight);
    }

    /**
     * Needs override by child class.
     *
     * @return new path after removing last node.
     */
    public Path removeLastNode() {
        return null;
    }

    /**
     * @return the last node in the path.
     */
    public Node getEndNode() {
        return this.node;
    }

    /**
     * Needs override by child class.
     *
     * @return list of nodes in the path.
     */
    public List<Node> getNodeList() {
        List<Node> nodeList = new ArrayList<>(1);
        nodeList.add(this.node);
        return nodeList;
    }

    /**
     * Needs override by child class.
     *
     * @return list of weight of edges in the path.
     */
    public List<Double> getWeightList() {
        List<Double> nodeList = new ArrayList<>(1);
        nodeList.add(this.totalCost);
        return nodeList;
    }

    /**
     * @return total path cost.
     */
    public double pathCost() {
        return this.totalCost;
    }

    /**
     * @return number of unscheduled stop in the path.
     */
    public int getUnScheduledStop() {
        return this.unScheduledStop;
    }

    /**
     * Child class
     */
    private static class NonEmptyPath extends Path {
        private final Path predecessor;

        NonEmptyPath(Path path, Edge edge) {
            super(edge.getTo(), path.totalCost + edge.getWeight(), path.length + 1, path.unScheduledStop + (edge.getDelay() ? 1 : 0));
            this.predecessor = path;
            if (this.predecessor.sourceTime == null) {
                super.sourceTime = edge.getFrom().getTime();
            } else {
                super.sourceTime = this.predecessor.sourceTime;
            }
        }

        NonEmptyPath(Path path, Node node, double weight) {
            super(node, weight, path.length + 1, path.unScheduledStop);
            this.predecessor = path;
            if (this.predecessor.sourceTime == null) {
                super.sourceTime = node.getTime();
            } else {
                super.sourceTime = this.predecessor.sourceTime;
            }
        }

        /**
         * @return list of nodes in the path.
         */
        @Override
        public List<Node> getNodeList() {
            LinkedList<Node> result = new LinkedList<>();
            Path path = this;
            while (path instanceof NonEmptyPath) {
                result.addFirst(path.node);
                path = ((NonEmptyPath) path).predecessor;
            }
            result.addFirst(path.node);
            return result;
        }

        /**
         * @return list of weight of edges in the path.
         */
        @Override
        public List<Double> getWeightList() {
            LinkedList<Double> result = new LinkedList<>();
            Path path = this;
            while (path instanceof NonEmptyPath) {
                result.addFirst(path.pathCost());
                path = ((NonEmptyPath) path).predecessor;
            }
            result.addFirst(path.pathCost());
            return result;
        }

        /**
         * @return new path after removing last node.
         */
        @Override
        public Path removeLastNode() {
            return this.predecessor;
        }

        @Override
        public String toString() {
            return getNodeList().toString();
        }
    }
}
