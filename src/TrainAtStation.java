import static java.util.Objects.requireNonNull;

public class TrainAtStation {

    private final String stationId;
    private final int trainNo;
    private final TrainTime arrival;
    private final TrainTime departure;

    public TrainAtStation(String stationId, int trainNo, TrainTime arrival, TrainTime departure) {
        requireNonNull(stationId, "Station id is null.");
        requireNonNull(arrival, "Arrival is null.");
        requireNonNull(departure, "Departure is null.");
        this.stationId = stationId;
        this.trainNo =trainNo;
        this.arrival =arrival;
        this.departure = departure;
    }

    public TrainAtStation(String key){
        requireNonNull(key, "Invalid key");
        if(key.equalsIgnoreCase("DefaultConstructorForNull")){
            this.stationId =null;
            this.trainNo = 0;
            this.arrival =null;
            this.departure = null;
        }
        else{
            throw new IllegalArgumentException("Invalid key");
        }
    }

    public String getStationId() {
        return this.stationId;
    }

    public int getTrainNo() {
        return this.trainNo;
    }

    public TrainTime getArr() {
        return this.arrival;
    }

    public TrainTime getDept() {
        return this.departure;
    }
}