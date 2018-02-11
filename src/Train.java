import java.util.*;

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

    public Train(int trainNo, String name, boolean isUpDirection) {
        requireNonNull(name, "The Train name is null.");
        this.stoppageMap = new HashMap<>();
        this.trainNo = trainNo;
        this.name = name;
        this.upDirection = isUpDirection;
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
        return this.stoppageMap.getOrDefault(stId, new TrainAtStation("DefaultConstructorForNull")).getArr();
    }

    public TrainTime getDept(String stId) {
        requireNonNull(stId,"Station id is null.");
        return this.stoppageMap.getOrDefault(stId, new TrainAtStation("DefaultConstructorForNull")).getDept();
    }

    public boolean addStoppage(Station station, TrainTime arrival, TrainTime departure) {
        if(station==null){
            // System.err.println("Station is not in route or some error occurred");
            return true;
        }
        requireNonNull(arrival, "Arrival is null.");
        requireNonNull(departure, "Departure is null.");
        TrainAtStation trainAtStation = new TrainAtStation(station.getId(), this.trainNo, arrival, departure);
        this.stoppageMap.put(station.getId(), trainAtStation);
        return station.addTrain(trainAtStation);
    }

    public List<TrainAtStation> sortArr(List<TrainAtStation> trainAtStationsList) {
        trainAtStationsList.sort(( o1, o2) ->{
            if(o1.getArr().equals(o2.getArr())) {
                return o1.getDept().compareTo(o2.getDept());
            }
            return o1.getArr().compareTo(o2.getArr());
        });
        return trainAtStationsList;
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
        List<TrainAtStation> trainAtStationsList = new ArrayList<>(this.stoppageMap.values());
        trainAtStationsList = sortArr(trainAtStationsList);
        for (TrainAtStation trainAtStation: trainAtStationsList) {
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