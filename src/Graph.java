import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

class adjListNode{
    int dest;
    long weight;
    adjListNode(int dest, long weight) {
        super();
        this.dest = dest;
        this.weight = weight;
    }
}

public class Graph {
    private ArrayList<Node> nodes;
    private HashMap< String, Integer> hashMap =  new HashMap<>();
    private ArrayList<LinkedList<adjListNode>> adjListArray;

    Graph(){
        super();
        this.adjListArray = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    public void addNode(Node node) {
        this.nodes.add(node);
        this.hashMap.put(node.getStationId() + "_"+node.getTime(), nodes.size()-1);
        this.adjListArray.add(new LinkedList<>());
    }

    private int getNodeIndex(Node node) {
        if(this.hashMap.containsKey(node.getStationId() + "_"+ node.getTime())) {
            return this.hashMap.get(node.getStationId() + "_"+ node.getTime());
        }
        return -1;
    }

    public boolean addEdge(Node src, Node dest, int edgeCost) {
        // Add an edge from src to dest.
        int indexSrc = getNodeIndex(src);
        int indexDest = getNodeIndex(dest);

        if(indexSrc>=0 && indexDest>=0 && indexSrc!=indexDest) {
            adjListNode aNode =  new adjListNode(indexDest, edgeCost);
            this.adjListArray.get(indexSrc).add(aNode);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
    public void printGraph() {
        String timeInfo;
        for(int i = 0; i < this.nodes.size(); i++) {
            System.out.println("Adjacency list of vertex ");
            this.nodes.get(i).printInfo();
            System.out.print("Head");

            for(int j=0;j<this.adjListArray.get(i).size();j++){
                timeInfo = "NA";
                Node temp = this.nodes.get(this.adjListArray.get(i).get(j).dest);
                if(temp.getTime() !=null) {
                    timeInfo = temp.getTime() + "";
                }
                System.out.print(" -> " + temp.getStationId() + " " + timeInfo );
            }
            System.out.println();
        }
    }

    public ArrayList<Node> dijkstra() {
        int size = this.nodes.size();
        long dist[] = new long[size];
        boolean added[] = new boolean[size];
        int parent[] = new int[size];

        int indexDest=-1;

        for(int i=0;i<size;i++) {
            dist[i] = Long.MAX_VALUE;
            added[i] = false;
            parent[i] = -1;
            if(this.nodes.get(i).getStationId().equalsIgnoreCase("SOURCE")) {
                dist[i] = 0;
            }
            else if(this.nodes.get(i).getStationId().equalsIgnoreCase("DEST")) {
                indexDest = i;
            }
        }

        boolean pathComplete = false;
        ArrayList<Node> bestPath = new ArrayList<>();

        while(!pathComplete) {
            int index = -1;
            long min= Long.MAX_VALUE;
            for(int i=0;i<size;i++) {
                if(!added[i] && dist[i] <= min) {
                    if(dist[i]<min) {
                        min = dist[i];
                        index = i;
                    }
                    else if(index>=0){
                        LocalTime localTime1 = this.nodes.get(index).getTime();
                        LocalTime localTime2 = this.nodes.get(i).getTime();
                        if(localTime1!=null && localTime2!=null && (localTime1.compareTo(localTime2) >0)){
                            index = i;
                        }
                    }
                }
            }

            if(index==-1) {
                //invalid path
                return bestPath;
            }

            added[index] = true;
            for(int i=0;i<adjListArray.get(index).size();i++) {
                adjListNode tempAdjNode = this.adjListArray.get(index).get(i);
                if(dist[tempAdjNode.dest] == Long.MAX_VALUE || (dist[index] + tempAdjNode.weight < dist[tempAdjNode.dest])) {
                    dist[tempAdjNode.dest] = dist[index] + tempAdjNode.weight;
                    parent[tempAdjNode.dest] = index;
                }
            }

            if(nodes.get(index).getStationId().equalsIgnoreCase("DEST")) {
                pathComplete=true;
            }
        }

        int tempIndex = indexDest;
        while(tempIndex!= -1) {
            bestPath.add(0,nodes.get(tempIndex));
            tempIndex = parent[tempIndex];
        }
        return bestPath;
    }
}
