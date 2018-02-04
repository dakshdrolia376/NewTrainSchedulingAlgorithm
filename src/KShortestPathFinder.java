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

        int maxLengthPathFound=0;
        int maxDifferenceAllowed = 6;

        List<Path> paths = new ArrayList<>(k);
        Map<String, Integer> countMap = new HashMap<>();
        Queue<Path> HEAP = new PriorityQueue<>(Comparator.comparingDouble(Path::pathCost));

        HEAP.add(new Path(source));

        while (!HEAP.isEmpty() && countMap.getOrDefault(target.toString(), 0) < k) {
            Path currentPath = HEAP.remove();
            if(currentPath.getLength()>maxLengthPathFound){
                maxLengthPathFound = currentPath.getLength();
                System.out.println("Best Path till now cost : " + currentPath.pathCost() + " >> " + currentPath.toString());
            }

            if(currentPath.getLength()+maxDifferenceAllowed <maxLengthPathFound){
                // System.out.println("Rejected Path cost : " + currentPath.pathCost() + " >> " + currentPath.toString());
                // System.out.println("Best Path cost : " + p.pathCost() + " >> " + p.toString());
                continue;
            }

            Node endNode = currentPath.getEndNode();
            countMap.put(endNode.toString(), countMap.getOrDefault(endNode.toString(), 0) + 1);
            if (endNode.equals(target)) {
                // System.out.print(" In path memory size: ");
                // Scheduler.getRuntimeMemory();
                paths.add(currentPath);
                // System.out.println("Path found :" + currentPath.toString());
            }
            else {
                if (countMap.get(endNode.toString()) <= k) {
                    for (Edge edge : graph.get(endNode)) {
                        Path path = currentPath.append(edge);
                        HEAP.add(path);
                    }
                }
            }
        }
        return paths;
    }
}