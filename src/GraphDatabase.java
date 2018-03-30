import java.util.*;

import static java.util.Objects.requireNonNull;

public class GraphDatabase extends GraphParent{
    private List<Edge> edgeList;
    private final DatabaseConnector databaseConnector;

    public GraphDatabase(boolean usePreviousComputation){
        super();
        edgeList = new ArrayList<>();
        databaseConnector = new DatabaseConnector();
        if(!usePreviousComputation && !databaseConnector.deleteAllNodes()){
            throw new RuntimeException("Unable to initialize database");
        }
    }

    @Override
    public boolean disconnect(){
        return databaseConnector.closeConnection();
    }

    @Override
    public boolean flushData(){
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

    @Override
    public boolean addNode(Node node){
        requireNonNull(node, "The node is null.");
        return databaseConnector.insertIntoNode(node);
    }

    @Override
    public boolean addMultipleNode(List<Node> nodes){
        requireNonNull(nodes, "The node list is null.");
        return databaseConnector.insertIntoNodeBatch(nodes);
    }

    @Override
    public boolean addEdge(Edge edge) {
        requireNonNull(edge, "The edge is null.");
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

    @Override
    public boolean addMultipleEdge(List<Edge> edges) {
        requireNonNull(edges, "The edge list is null.");
        return databaseConnector.insertIntoEdgeBatch(edges);
    }

    @Override
    public Edge get(Node from, Node to) {
        double edgeWeight = databaseConnector.getEdgeWeight(from, to);
        if(edgeWeight>=0){
            return new Edge(from,to, edgeWeight, false);
        }
        else{
            System.out.println("Edge not found.");
            return null;
        }
    }

    @Override
    public Collection<Edge> get(Node from) {
        return databaseConnector.getEdges(from);
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder("");
        List<Node> nodes = databaseConnector.getNodes();

        for(Node node:nodes){
            stringBuilder.append(node.toString());
            stringBuilder.append(" ->> ");
            stringBuilder.append(get(node).toString());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}
