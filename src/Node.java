import java.time.LocalTime;

public class Node {
    private LocalTime time;
    private String stationId;
    private Double distance;
    private Double waitTime;

    Node(LocalTime time, String stationId, Double distance, Double waitTime) {
        super();
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

    @SuppressWarnings("unused")
    public String getStationId() {
        return this.stationId;
    }

    public LocalTime getTime() {
        return this.time;
    }

    public Double getDistance() {
        return this.distance;
    }

    @SuppressWarnings("unused")
    public Double getWaitTime() {
        return this.waitTime;
    }
}
