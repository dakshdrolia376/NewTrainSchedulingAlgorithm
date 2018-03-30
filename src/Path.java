import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

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

    public int getLength(){
        return this.length;
    }

    public Path append(Edge edge) {
        requireNonNull(edge, "The input edge is null.");
        if (!this.node.equals(edge.getFrom())) {
            throw new IllegalArgumentException(format("The edge %s doesn't extend the path %s",
                    edge, this.getNodeList()));
        }
        return new NonEmptyPath(this, edge);
    }

    public TrainTime getSourceTime(){
        if(this.sourceTime!=null){
            return new TrainTime(this.sourceTime);
        }
        return null;
    }

    public Path append(Node node, double weight) {
        requireNonNull(node, "The input node is null.");
        return new NonEmptyPath(this, node, weight);
    }

    public Path removeLastNode() {
        return null;
    }

    public Node getEndNode() {
        return this.node;
    }

    public List<Node> getNodeList() {
        List<Node> nodeList = new ArrayList<>(1);
        nodeList.add(this.node);
        return nodeList;
    }

    public List<Double> getWeightList() {
        List<Double> nodeList = new ArrayList<>(1);
        nodeList.add(this.totalCost);
        return nodeList;
    }

    public double pathCost() {
        return this.totalCost;
    }

    public int getUnScheduledStop(){
        return this.unScheduledStop;
    }

    private static class NonEmptyPath extends Path{
        private final Path predecessor;
        NonEmptyPath(Path path, Edge edge) {
            super(edge.getTo(), path.totalCost + edge.getWeight(), path.length+1, path.unScheduledStop+(edge.getDelay()?1:0));
            this.predecessor = path;
            if(this.predecessor.sourceTime==null) {
                super.sourceTime = edge.getFrom().getTime();
            }
            else{
                super.sourceTime = this.predecessor.sourceTime;
            }
        }

        NonEmptyPath(Path path, Node node, double weight) {
            super(node, weight, path.length+1, path.unScheduledStop);
            this.predecessor = path;
            if(this.predecessor.sourceTime==null) {
                super.sourceTime = node.getTime();
            }
            else{
                super.sourceTime = this.predecessor.sourceTime;
            }
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

        // @Override
        // public TrainTime getSourceTime(){
        //     if(super.sourceTime!=null){
        //         return new TrainTime(super.sourceTime);
        //     }
        //     return null;
        // }

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
        public Path removeLastNode() {
            return this.predecessor;
        }

        @Override
        public String toString() {
            return getNodeList().toString();
        }
    }
}