import java.util.*;

import static java.util.Objects.requireNonNull;

public class GraphDatabase {
    private List<Edge> edgeList;
    private final DatabaseConnector databaseConnector;

    public GraphDatabase(boolean usePreviousComputation){
        edgeList = new ArrayList<>();
        databaseConnector = new DatabaseConnector();
        if(!usePreviousComputation && !databaseConnector.deleteAllNodes()){
            throw new RuntimeException("Unable to initialize database");
        }
    }

    public boolean disconnect(){
        return databaseConnector.closeConnection();
    }

    @SuppressWarnings("unused")
    public boolean addNode(Node node){
        requireNonNull(node, "The node is null.");
        return databaseConnector.insertIntoNode(node);
    }

    public boolean addMultipleNode(List<Node> nodes){
        requireNonNull(nodes, "The node list is null.");
        return databaseConnector.insertIntoNodeBatch(nodes);
    }

    public boolean addEdge(Edge edge) {
        requireNonNull(edge, "The edge is null.");
        if(!edge.getFrom().isValid() || !edge.getTo().isValid()){
            System.out.println("Invalid edge.");
            return false;
        }

        edgeList.add(edge);
        if(edgeList.size()>=10000){
            // System.out.println(edgeList.toString());
            boolean result = addMultipleEdge(edgeList);
            if(result){
                edgeList = new ArrayList<>();
                return true;
            }
            System.out.println("Some error occurred in adding edge");
            return false ;
        }
        return true;
    }

    public boolean flushEdgeList(){
        if(edgeList.size()==0){
            return true;
        }
        boolean result = addMultipleEdge(edgeList);
        if(result){
            edgeList = new ArrayList<>();
            return true;
        }
        return false;
    }

    public boolean addMultipleEdge(List<Edge> edges) {
        requireNonNull(edges, "The edge list is null.");
        return databaseConnector.insertIntoEdgeBatch(edges);
    }

    public Edge get(Node from, Node to) {
        double edgeWeight = databaseConnector.getEdgeWeight(from, to);
        if(edgeWeight>=0){
            return new Edge(from,to, edgeWeight);
        }
        else{
            System.out.println("Edge not found.");
            return null;
        }
    }

    public List<Edge> get(Node from) {
        return databaseConnector.getEdges(from);
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder("");
        List<Node> nodes = databaseConnector.getNodes();

        for(Node node:nodes){
            stringBuilder.append(node.toString());
            stringBuilder.append(" ->> ");
            List<Edge> edges = get(node);
            stringBuilder.append(edges.toString());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}
