package iitp.naman.newtrainschedulingalgorithm;

import java.util.*;

/**
 * Creates graph required for storing the schedule info.
 */
public class GraphKBestPath {
    private GraphMemory graphMemory;

    public GraphKBestPath(boolean usePreviousComputation, String pathTemp) {
        graphMemory = new GraphMemory();
    }

    public boolean disconnect() {
        return graphMemory.disconnect();
    }

    public boolean flushData() {
        return graphMemory.flushData();
    }

    @SuppressWarnings("unused")
    public boolean addNode(Node node) {
        return graphMemory.addNode(node);
    }

    public boolean addMultipleNode(List<Node> nodes) {
        return graphMemory.addMultipleNode(nodes);
    }

    public boolean addEdge(Edge edge) {
        return graphMemory.addEdge(edge);
    }

    @SuppressWarnings("unused")
    public boolean addMultipleEdge(List<Edge> edges) {
        return graphMemory.addMultipleEdge(edges);
    }

    public Edge get(Node from, Node to) {
        return graphMemory.get(from, to);
    }

    public Collection<Edge> get(Node from) {
        return graphMemory.get(from);
    }

    public String toString() {
        return graphMemory.toString();
    }
}
