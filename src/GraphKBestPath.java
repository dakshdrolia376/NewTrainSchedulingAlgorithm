import java.util.*;

import static java.lang.String.*;

public class GraphKBestPath<V> {

    //could be replaced by http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/collect/Table.html
    private Map<V,Map<V,Edge<V>>> vertexEdgeMap = new HashMap<>();

    GraphKBestPath() {

    }

    public boolean addEdge(Edge<V> edge) {
        vertexEdgeMap.putIfAbsent(edge.from, new HashMap<>());
        Map<V, Edge<V>> fromMap = vertexEdgeMap.get(edge.from);
        if(fromMap.containsKey(edge.to)) {
            return false;
        }
        fromMap.put(edge.to, edge);
        return true;
    }

    public Edge<V> get(V from, V to) {
        return vertexEdgeMap.get(from).get(to);
    }

    public Collection<Edge<V>> get(V from) {
        return vertexEdgeMap.getOrDefault(from, Collections.emptyMap()).values();
    }
}
