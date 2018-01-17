public class Edge<V> {

    public final V from;
    public final V to;
    public final double weight;

    Edge(V from, V to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        if (Double.isNaN(weight)) {
            throw new IllegalArgumentException("The weight is NaN.");
        }
        if (weight < 0.0) {
            throw new IllegalArgumentException("The weight is negative.");
        }
    }
}