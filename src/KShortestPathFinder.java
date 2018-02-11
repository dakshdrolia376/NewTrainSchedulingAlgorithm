import java.util.*;

import static java.util.Objects.requireNonNull;

public class KShortestPathFinder {

    private boolean checkK(int k) {
        return (k>=1);
    }

    public List<Path> findShortestPaths(Node source, Node target, GraphKBestPath graph, int k, List<Double> maxCostList) {
        requireNonNull(source, "The source node is null.");
        requireNonNull(target, "The target node is null.");
        requireNonNull(graph, "The graph is null.");
        if (!checkK(k)){
            throw new IllegalArgumentException("Invalid number of paths required.");
        }
        System.out.println("Finding shortest path bw : " + source.toString() + " >> " + target.toString());

        Path bestPath = new Path(source);

        List<Path> paths = new ArrayList<>(k);
        Map<String, Integer> countMap = new HashMap<>();
        Queue<Path> HEAP = new PriorityQueue<>(Comparator.comparingDouble(Path::pathCost));

        HEAP.add(new Path(source));

        while (!HEAP.isEmpty() && countMap.getOrDefault(target.toString(), 0) < k) {
            Path currentPath = HEAP.remove();
            if(currentPath.getLength()>bestPath.getLength()){
                System.out.println("Best Path till now cost : " + currentPath.pathCost() + " >> " + currentPath.toString());
                bestPath = currentPath;
                Scheduler.getRuntimeMemory();
            }

            if(currentPath.pathCost() > maxCostList.get(currentPath.getLength()-1)){
                // System.out.println("Max cost : " + maxCostList.get(currentPath.getLength()-1) + " Rejected Path cost : "
                //         + currentPath.pathCost() + " >> " + currentPath.toString());
                continue;
            }

            // if((currentPath.getLength() + maxDifferenceAllowed) < bestPath.getLength()){
            //     // System.out.println("Rejected Path cost : " + currentPath.pathCost() + " >> " + currentPath.toString());
            //     // System.out.println("Best Path cost : " + p.pathCost() + " >> " + p.toString());
            //     continue;
            // }

            Node endNode = currentPath.getEndNode();
            countMap.put(endNode.toString(), countMap.getOrDefault(endNode.toString(), 0) + 1);
            if (endNode.equals(target)) {
                // System.out.print(" In path memory size: ");
                // Scheduler.getRuntimeMemory();
                TrainTime sourceTimeTemp = currentPath.getSourceTime();
                boolean diversePath = true;
                for(Path pathAlreadyFound: paths){
                    if(Math.abs((pathAlreadyFound.getSourceTime().compareTo(sourceTimeTemp)))<15){
                        diversePath = false;
                        break;
                    }
                }
                if(diversePath) {
                    paths.add(currentPath);
                    System.out.println("Accepted Path found :" + currentPath.toString());
                }
                else {
                    System.out.println("Rejected Path found :" + currentPath.toString());
                }
            }
            else {
                if (countMap.get(endNode.toString()) <= k) {
                    // System.out.println("adding to Path : " + currentPath.toString());
                    for (Edge edge : graph.get(endNode)) {
                        // System.out.println("\t\t Adding edge : " + edge.toString());
                        Path path = currentPath.append(edge);
                        HEAP.add(path);
                    }
                }
            }
        }
        System.out.println(paths.toString());
        return paths;
    }
}