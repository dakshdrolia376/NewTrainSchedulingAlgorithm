import java.util.*;

import static java.util.Objects.requireNonNull;

public class KShortestPathFinder {

    private boolean checkK(int k) {
        return (k>=1);
    }

    public List<Path> findShortestPaths(Node source, Node target, GraphKBestPath graph, int k) {
        requireNonNull(source, "The source node is null.");
        requireNonNull(target, "The target node is null.");
        requireNonNull(graph, "The graph is null.");
        if (!checkK(k)){
            throw new IllegalArgumentException("Invalid number of paths required.");
        }

        List<Path> paths = new ArrayList<>(k);
        Map<String, Integer> countMap = new HashMap<>();
        Queue<Path> HEAP = new PriorityQueue<>(Comparator.comparingDouble(Path::pathCost));

        HEAP.add(new Path(source));

        while (!HEAP.isEmpty() && countMap.getOrDefault(target.toString(), 0) < k) {
            Path currentPath = HEAP.remove();
            System.out.println("****" +currentPath.toString());
            Node endNode = currentPath.getEndNode();
            if(!endNode.isValid()){
                continue;
            }
            countMap.put(endNode.toString(), countMap.getOrDefault(endNode.toString(), 0) + 1);
            if (endNode.equals(target)) {
                paths.add(currentPath);
            }
            if (countMap.get(endNode.toString()) <= k) {
                for (Edge edge : graph.get(endNode)) {
                    Path path = currentPath.append(edge);
                    HEAP.add(path);
                }
            }
        }
        return paths;
    }
}