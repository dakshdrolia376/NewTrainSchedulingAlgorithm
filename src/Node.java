import java.time.LocalTime;

import static java.util.Objects.requireNonNull;

public class Node {
    private final LocalTime time;
    private final String stationId;
    private final double distance;
    private final double waitTime;

    Node(LocalTime time, String stationId, double distance, double waitTime) {
        requireNonNull(stationId, "The station id is null.");
        this.time = time;
        this.stationId = stationId;
        this.distance = distance;
        this.waitTime = waitTime;
    }

    @SuppressWarnings("unused")
    public void printInfo() {
        if(this.time!=null) {
            System.out.println(this.stationId +"\t" + this.time+ "\t" + this.distance + "\t" + this.waitTime);
        }
        else {
            System.out.println(this.stationId +"\tNA\t" + this.distance + "\t" + this.waitTime);
        }
    }

    public String getStationId() {
        return this.stationId;
    }

    public LocalTime getTime() {
        return this.time;
    }

    public double getDistance() {
        return this.distance;
    }

    public double getWaitTime() {
        return this.waitTime;
    }
}
