import java.util.*;

import static java.util.Objects.requireNonNull;

public class GraphMemory extends GraphParent{
    private Map<String,Map<String,Edge>> vertexEdgeMap;

    public GraphMemory(){
        super();
        vertexEdgeMap = new HashMap<>();
    }

    @Override
    public boolean disconnect(){
        return true;
    }

    @Override
    public boolean flushData(){
        return true;
    }

    @Override
    public boolean addNode(Node node){
        return true;
    }

    @Override
    public boolean addMultipleNode(List<Node> nodes){
        return true;
    }

    @Override
    public boolean addEdge(Edge edge) {
        requireNonNull(edge, "The edge is null.");
        vertexEdgeMap.putIfAbsent(edge.getFrom().toString(), new HashMap<>());
        vertexEdgeMap.get(edge.getFrom().toString()).put(edge.getTo().toString(), edge);
        return true;
    }

    @Override
    public boolean addMultipleEdge(List<Edge> edges) {
        requireNonNull(edges, "The edge list is null.");
        boolean ans = true;
        for(Edge edge: edges){
            ans = addEdge(edge);
        }
        return ans;
    }

    @Override
    public Edge get(Node from, Node to) {
        return vertexEdgeMap.getOrDefault(from.toString(),Collections.emptyMap()).getOrDefault(to.toString(),null);
    }

    @Override
    public Collection<Edge> get(Node from) {
        return vertexEdgeMap.getOrDefault(from.toString(), Collections.emptyMap()).values();
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder("");
        for(String nodeFrom:this.vertexEdgeMap.keySet()){
            stringBuilder.append(nodeFrom);
            stringBuilder.append("->>");
            stringBuilder.append(vertexEdgeMap.getOrDefault(nodeFrom, Collections.emptyMap()).values().toString());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}
