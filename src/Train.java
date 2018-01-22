import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class Train {

    private final int trainNo;
    private final String name;
    private final Map<String, TrainAtStation> stoppageMap;

    public Train(int trainNo, String name) {
        requireNonNull(name, "The Train name is null.");
        stoppageMap = new HashMap<>();
        this.trainNo = trainNo;
        this.name = name;
    }

    public int getTrainNo() {
        return this.trainNo;
    }

    public String getName() {
        return this.name;
    }

    public LocalTime getArr(String stId) {
        requireNonNull(stId,"Station id is null.");
        TrainAtStation trainAtStation = stoppageMap.get(stId);
        if(trainAtStation!=null){
            return trainAtStation.getArr();
        }
        return null;
    }

    public LocalTime getDept(String stId) {
        requireNonNull(stId,"Station id is null.");
        TrainAtStation trainAtStation = stoppageMap.get(stId);
        if(trainAtStation!=null){
            return trainAtStation.getDept();
        }
        return null;
    }

    public boolean addStoppage(Station station, LocalTime arrival, LocalTime departure) {
        requireNonNull(station, "Station is null.");
        requireNonNull(arrival, "Arrival is null.");
        requireNonNull(departure, "Departure is null.");
        TrainAtStation trainAtStation = new TrainAtStation(station.getId(), this.trainNo, arrival, departure);
        stoppageMap.put(station.getId(), trainAtStation);
        return station.addTrain(trainAtStation);
    }

    public void printInfo() {
        System.out.println("**********************************************************");
        System.out.println("Train No: " + this.trainNo + " name: " + this.name);
        System.out.println("Station\tArrival\tDeparture");
        for (TrainAtStation trainAtStation: this.stoppageMap.values()) {
            System.out.println( trainAtStation.getStationId() + "\t" + trainAtStation.getArr() + "\t"  + trainAtStation.getDept());
        }
        System.out.println("**********************************************************");
    }
}