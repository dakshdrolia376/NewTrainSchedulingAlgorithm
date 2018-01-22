import java.time.LocalTime;

import static java.util.Objects.requireNonNull;

public class TrainAtStation {

    private final String stationId;
    private final int trainNo;
    private final LocalTime arrival;
    private final LocalTime departure;

    public TrainAtStation(String stationId, int trainNo, LocalTime arrival, LocalTime departure) {
        requireNonNull(stationId, "Station id is null.");
        requireNonNull(arrival, "Arrival is null.");
        requireNonNull(departure, "Departure is null.");
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
