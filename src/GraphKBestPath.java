import java.util.*;
public class GraphKBestPath {
    private GraphParent graphParent;

    @SuppressWarnings("unused")
    public GraphKBestPath(boolean usePreviousComputation, String pathTemp){
        // graphParent = new GraphFile(usePreviousComputation, pathTemp);
        graphParent = new GraphMemory();
    }

    public boolean disconnect(){
        return graphParent.disconnect();
    }

    public boolean flushData(){
        return graphParent.flushData();
    }

    @SuppressWarnings("unused")
    public boolean addNode(Node node){
        return graphParent.addNode(node);
    }

    public boolean addMultipleNode(List<Node> nodes){
        return graphParent.addMultipleNode(nodes);
    }

    public boolean addEdge(Edge edge) {
        return graphParent.addEdge(edge);
    }

    @SuppressWarnings("unused")
    public boolean addMultipleEdge(List<Edge> edges) {
        return graphParent.addMultipleEdge(edges);
    }

    public Edge get(Node from, Node to) {
        return graphParent.get(from,to);
    }

    public Collection<Edge> get(Node from) {
        return graphParent.get(from);
    }

    public String toString(){
        return graphParent.toString();
    }
}
