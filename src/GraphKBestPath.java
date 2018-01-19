import java.util.*;

import static java.util.Objects.requireNonNull;

public class GraphKBestPath<V> {

    private Map<V,Map<V,Edge<V>>> vertexEdgeMap = new HashMap<>();

    public boolean addEdge(Edge<V> edge) {
        requireNonNull(edge, "The edge is null.");
        vertexEdgeMap.putIfAbsent(edge.from, new HashMap<>());
        Map<V, Edge<V>> fromMap = vertexEdgeMap.get(edge.from);

        // only single edge between two nodes.
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

    public void printInfo(){
        for(V nodeFrom:this.vertexEdgeMap.keySet()){
            System.out.print(nodeFrom + "->> ");
            Map<V,Edge<V>> map = vertexEdgeMap.get(nodeFrom);
            for(V nodeTo: map.keySet()){
                System.out.print(nodeTo + " " +map.get(nodeTo).weight + " ");
            }
            System.out.println();
        }
    }
}
