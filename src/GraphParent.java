import java.util.*;

public class GraphParent {
    public GraphParent(){
    }

    public boolean disconnect(){
        return true;
    }

    public boolean flushData(){
        return true;
    }

    public boolean addNode(Node node){
        return true;
    }

    public boolean addMultipleNode(List<Node> nodes){
        return true;
    }

    public boolean addEdge(Edge edge) {
        return true;
    }

    public boolean addMultipleEdge(List<Edge> edges) {
        return true;
    }

    public Edge get(Node from, Node to) {
        return null;
    }

    public Collection<Edge> get(Node from) {
        return Collections.emptyList();
    }

    public String toString(){
        return "";
    }
}
