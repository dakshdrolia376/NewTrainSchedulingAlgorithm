import java.util.*;

import static java.util.Objects.requireNonNull;

public class KShortestPathFinder {

    private boolean checkK(int k) {
        return (k>=1);
    }

    public List<Path> findShortestPaths(Node source, Node target, GraphKBestPath graph, int k, List<Double> maxCostList,
                                        List<String> stationList) {
        requireNonNull(source, "The source node is null.");
        requireNonNull(target, "The target node is null.");
        requireNonNull(graph, "The graph is null.");
        for(int i=0;i<stationList.size();i++){
            stationList.set(i,stationList.get(i).split(":")[0]);
        }
        if (!checkK(k)){
            throw new IllegalArgumentException("Invalid number of paths required.");
        }
        System.out.println("Finding shortest path bw : " + source.toString() + " >> " + target.toString());

        Path bestPath = new Path(source);

        List<Path> paths = new ArrayList<>(k);
        Map<String, Integer> countMap = new HashMap<>();
        Comparator<Path> comparatorPath = new Comparator<Path>() {
            @Override
            public int compare(Path o1, Path o2) {
                if(o1.pathCost()==o2.pathCost()){
                    return (o1.getUnScheduledStop() - o2.getUnScheduledStop());
                }
                else{
                    return (int)(o1.pathCost()- o2.pathCost());
                }
            }
        };

        Queue<Path> HEAP = new PriorityQueue<>(comparatorPath);

        HEAP.add(new Path(source));

        int countRejected=0;

        while (!HEAP.isEmpty() && paths.size() < k) {
            Path currentPath = HEAP.remove();
            // System.out.println(currentPath.pathCost()+" "+ currentPath.getUnScheduledStop());
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

            if(currentPath.getLength() > maxCostList.size()){
               //some loop has occurred...
                continue;
            }

            // if((currentPath.getLength() + maxDifferenceAllowed) < bestPath.getLength()){
            //     // System.out.println("Rejected Path cost : " + currentPath.pathCost() + " >> " + currentPath.toString());
            //     // System.out.println("Best Path cost : " + p.pathCost() + " >> " + p.toString());
            //     continue;else
            // }

            Node endNode = currentPath.getEndNode();
            countMap.put(endNode.toString(), countMap.getOrDefault(endNode.toString(), 0) + 1);
            if (endNode.equals(target)) {
                if(currentPath.getLength()==maxCostList.size()) {

                    // System.out.print(" In path memory size: ");
                    // Scheduler.getRuntimeMemory();
                    TrainTime sourceTimeTemp = currentPath.getSourceTime();
                    boolean diversePath = true;
                    for (Path pathAlreadyFound : paths) {
                        int temp = Math.abs((pathAlreadyFound.getSourceTime().compareTo(sourceTimeTemp)));
                        if (temp < 15 || (Math.abs(temp - 10080) < 15)) {
                            diversePath = false;
                            break;
                        }
                    }
                    if ((source.getTime() != null) || diversePath) {
                        paths.add(currentPath);
                        System.out.println("Accepted Path found :" + currentPath.toString() + " cost: " + currentPath.pathCost());
                    } else {
                        countRejected++;
                        System.out.println("Rejected Path found :" + currentPath.toString() + " cost: " + currentPath.pathCost());
                    }
                }
                else{
                    System.out.println("Some stations are repeating");
                }
            }
            else {
                if (countMap.get(endNode.toString()) <= k) {
                    int countStationNo = currentPath.getLength();
                    // System.out.println("adding to Path : " + currentPath.toString());
                    for (Edge edge : graph.get(endNode)) {
                        // System.out.println("\t\t Adding edge : " + edge.toString());
                        if(countStationNo>= stationList.size() || edge.getTo().getStationId().equalsIgnoreCase(stationList.get(countStationNo))){
                            Path path = currentPath.append(edge);
                            // System.out.println("No of stops "+ path.getUnScheduledStop());
                            HEAP.add(path);
                        }
                    }
                }
            }
        }
        if(HEAP.isEmpty()) {
            System.out.print("Heap Empty ");
        }
        else{
            System.out.print("Heap Required Number of paths found ");
        }
        System.out.println(paths.toString());
        System.out.println("Rejected paths:" + countRejected);
        return paths;
    }
}