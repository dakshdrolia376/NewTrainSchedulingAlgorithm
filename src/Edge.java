import static java.util.Objects.requireNonNull;

public class Edge {

    private final Node from;
    private final Node to;
    private final double weight;
    private final boolean delay;

    public Edge(Node from, Node to, double weight, boolean delay) {
        requireNonNull(from, "The from node is null.");
        requireNonNull(to, "The to node is null.");
        if (Double.isNaN(weight)) {
            throw new IllegalArgumentException("The weight is NaN.");
        }
        if (weight < 0.0) {
            throw new IllegalArgumentException("The weight is negative.");
        }
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.delay = delay;
    }

    public Node getFrom(){
        return this.from;
    }

    public Node getTo(){
        return this.to;
    }

    public double getWeight(){
        return this.weight;
    }

    public boolean getDelay(){
        return this.delay;
    }

    @Override
    public String toString(){
        return this.from.toString() + "-(" + this.weight+")->" + this.to.toString();
    }
}