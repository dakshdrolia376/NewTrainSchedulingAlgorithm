import static java.util.Objects.requireNonNull;

public class Edge<V> {

    public final V from;
    public final V to;
    public final double weight;

    public Edge(V from, V to, double weight) {
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
    }
}