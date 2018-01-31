import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class Train {

    private final int trainNo;
    private final String name;
    private final Map<String, TrainAtStation> stoppageMap;
    private final boolean upDirection;

    public Train(int trainNo, String name) {
        requireNonNull(name, "The Train name is null.");
        this.stoppageMap = new HashMap<>();
        this.trainNo = trainNo;
        this.name = name;
        this.upDirection = true;
    }

    @SuppressWarnings("unused")
    public int getTrainNo() {
        return this.trainNo;
    }

    public String getName() {
        return this.name;
    }

    public boolean isUpDirection() {
        return this.upDirection;
    }

    public TrainTime getArr(String stId) {
        requireNonNull(stId,"Station id is null.");
        TrainAtStation trainAtStation = this.stoppageMap.get(stId);
        if(trainAtStation!=null){
            return trainAtStation.getArr();
        }
        return null;
    }

    public TrainTime getDept(String stId) {
        requireNonNull(stId,"Station id is null.");
        TrainAtStation trainAtStation = this.stoppageMap.get(stId);
        if(trainAtStation!=null){
            return trainAtStation.getDept();
        }
        return null;
    }

    public boolean addStoppage(Station station, TrainTime arrival, TrainTime departure) {
        if(station==null){
            System.err.println("Station is not in route or some error occurred");
            return true;
        }
        requireNonNull(arrival, "Arrival is null.");
        requireNonNull(departure, "Departure is null.");
        TrainAtStation trainAtStation = new TrainAtStation(station.getId(), this.trainNo, arrival, departure);
        this.stoppageMap.put(station.getId(), trainAtStation);
        return station.addTrain(trainAtStation);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("Train No: ");
        stringBuilder.append(this.trainNo);
        stringBuilder.append(" name: ");
        stringBuilder.append(this.name);
        stringBuilder.append('\n');
        stringBuilder.append("Station\tArrival\tDeparture");
        stringBuilder.append('\n');
        for (TrainAtStation trainAtStation: this.stoppageMap.values()) {
            stringBuilder.append( trainAtStation.getStationId());
            stringBuilder.append('\t');
            stringBuilder.append(trainAtStation.getArr());
            stringBuilder.append( '\t');
            stringBuilder.append( trainAtStation.getDept());
            stringBuilder.append( '\n');
        }
        return stringBuilder.toString();
    }
}