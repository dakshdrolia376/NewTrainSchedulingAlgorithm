import java.util.*;

import static java.util.Objects.requireNonNull;

public class KShortestPathFinder {

    private boolean checkK(int k) {
        return (k>=1);
    }

    public List<Path> findShortestPaths(Node source, Node target, GraphKBestPath graph, int k, List<Double> maxCostList,
                                        List<String> stationList, boolean findNonDiversePath) {
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

        Queue<Path> HEAP = new PriorityQueue<>(( o1, o2) ->{
            if(o1.pathCost()==o2.pathCost()){
                return (o1.getUnScheduledStop() - o2.getUnScheduledStop());
            }
            else{
                return (int)(o1.pathCost()- o2.pathCost());
            }
        });

        HEAP.add(new Path(source));

        int countRejected=0;

        while (!HEAP.isEmpty() && paths.size() < k) {
            Path currentPath = HEAP.remove();
            if(currentPath.getLength()>bestPath.getLength()){
                System.out.println("Best Path till now cost : " + currentPath.pathCost() + " >> " + currentPath.toString());
                bestPath = currentPath;
                Scheduler.getRuntimeMemory();
            }

            if(currentPath.pathCost() > maxCostList.get(currentPath.getLength()-1)){
                continue;
            }

            if(currentPath.getLength() > maxCostList.size()){
                continue;
            }

            Node endNode = currentPath.getEndNode();
            countMap.put(endNode.toString(), countMap.getOrDefault(endNode.toString(), 0) + 1);
            if (endNode.equals(target)) {
                if(currentPath.getLength()==maxCostList.size()) {

                    TrainTime sourceTimeTemp = currentPath.getSourceTime();
                    boolean diversePath = true;
                    for (Path pathAlreadyFound : paths) {
                        int temp = Math.abs((pathAlreadyFound.getSourceTime().compareTo(sourceTimeTemp)));
                        if (temp < 15 || (Math.abs(temp - 10080) < 15)) {
                            diversePath = false;
                            break;
                        }
                    }
                    if (findNonDiversePath || diversePath) {
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
                    for (Edge edge : graph.get(endNode)) {
                        if(countStationNo>= stationList.size() || edge.getTo().getStationId().equalsIgnoreCase(stationList.get(countStationNo))){
                            Path path = currentPath.append(edge);
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