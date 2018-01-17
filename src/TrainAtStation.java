import java.time.LocalTime;

public class TrainAtStation {

    private String stationId;
    private int trainNo;
    private LocalTime arrival;
    private LocalTime departure;

    TrainAtStation(String stationId, int trainNo, LocalTime arrival, LocalTime departure) {
        this.stationId = stationId;
        this.trainNo =trainNo;
        this.arrival =arrival;
        this.departure = departure;
    }

    public String getStationId() {
        return this.stationId;
    }

    public int getTrainNo() {
        return this.trainNo;
    }

    public LocalTime getArr() {
        return this.arrival;
    }

    public LocalTime getDept() {
        return this.departure;
    }
}
