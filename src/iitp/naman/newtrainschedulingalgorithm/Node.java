package iitp.naman.newtrainschedulingalgorithm;

import iitp.naman.newtrainschedulingalgorithm.util.TrainTime;

import static java.util.Objects.requireNonNull;

/**
 * Class for storing node info of graph.
 */
public class Node {
    private final TrainTime time;
    private final String stationId;
    private int inEdgeCount;
    private int outEdgeCount;

    public Node(TrainTime time, String stationId) {
        requireNonNull(stationId, "The station id is null.");
        this.time = (time != null) ? (new TrainTime(time)) : null;
        this.stationId = stationId;
        this.inEdgeCount = 0;
        this.outEdgeCount = 0;
    }

    public Node(String label) {
        requireNonNull(label, "The label is null.");
        String data[] = label.split(":");
        this.stationId = data[0];
        TrainTime trainTime = null;
        if (data.length == 4) {
            try {
                trainTime = new TrainTime(Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
            } catch (Exception e) {
                System.out.println("Invalid label: " + label);
            }

        }
        this.time = trainTime;
        this.inEdgeCount = 0;
        this.outEdgeCount = 0;
    }

    /**
     * Increment by 1 the total number of IN edges for the node.
     */
    public void incrementInEdgeCount() {
        this.inEdgeCount++;
    }

    /**
     * Increment by 1 the total number of OUT edges for the node.
     */
    public void incrementOutEdgeCount() {
        this.outEdgeCount++;
    }

    /**
     * @return total number of IN edges for the node.
     */
    public int getInEdgeCount() {
        return this.inEdgeCount;
    }

    /**
     * @return total number of out edges for the node.
     */
    public int getOutEdgeCount() {
        return this.outEdgeCount;
    }

    /**
     * @param node node to be compared.
     * @return true if two nodes are identical.
     */
    public boolean equals(Node node) {
        return this.toString().equalsIgnoreCase(node.toString());
    }


    public String toString() {
        if (this.time != null) {
            return this.stationId + ":" + this.time.toString();
        } else {
            return this.stationId;
        }
    }

    /**
     * @return station id of the node.
     */
    public String getStationId() {
        return this.stationId;
    }

    /**
     * @return time details of the node.
     */
    public TrainTime getTime() {
        return this.time;
    }
}
