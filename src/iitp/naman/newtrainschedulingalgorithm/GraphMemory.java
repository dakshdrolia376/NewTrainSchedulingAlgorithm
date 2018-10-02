package iitp.naman.newtrainschedulingalgorithm;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class GraphMemory {
    private Map<String, Map<String, Edge>> vertexEdgeMap;


    public GraphMemory() {
        super();
        vertexEdgeMap = new HashMap<>();
    }

    /**
     * Deletes the data from memeory.
     */
    public boolean disconnect() {
        this.vertexEdgeMap = new HashMap<>();
        return true;
    }

    public boolean flushData() {
        return true;
    }

    /**
     * Helps if node need to be added.
     */
    public boolean addNode(Node node) {
        return true;
    }

    /**
     * Helps if multiple nodes need to be added.
     */
    public boolean addMultipleNode(List<Node> nodes) {
        return true;
    }

    /**
     * Add edge in graph.
     *
     * @param edge edge details
     * @return true if successful.
     */
    public boolean addEdge(Edge edge) {
        requireNonNull(edge, "The edge is null.");
        vertexEdgeMap.putIfAbsent(edge.getFrom().toString(), new HashMap<>());
        vertexEdgeMap.get(edge.getFrom().toString()).put(edge.getTo().toString(), edge);
        return true;
    }

    /**
     * Add edges in batch.
     *
     * @param edges list of edges.
     * @return true if successful.
     */
    public boolean addMultipleEdge(List<Edge> edges) {
        requireNonNull(edges, "The edge list is null.");
        boolean ans = true;
        for (Edge edge : edges) {
            ans = addEdge(edge);
        }
        return ans;
    }

    /**
     * @param from from node of edge.
     * @param to   to node of edge.
     * @return edge matching with details.
     */
    public Edge get(Node from, Node to) {
        return vertexEdgeMap.getOrDefault(from.toString(), Collections.emptyMap()).getOrDefault(to.toString(), null);
    }

    /**
     * @param from from node of edge.
     * @return list of all edges going out from given node.
     */
    public Collection<Edge> get(Node from) {
        return vertexEdgeMap.getOrDefault(from.toString(), Collections.emptyMap()).values();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        for (String nodeFrom : this.vertexEdgeMap.keySet()) {
            stringBuilder.append(nodeFrom);
            stringBuilder.append("->>");
            stringBuilder.append(vertexEdgeMap.getOrDefault(nodeFrom, Collections.emptyMap()).values().toString());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}
