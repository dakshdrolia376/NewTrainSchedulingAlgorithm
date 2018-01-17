import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class Train {

    private int trainNo;
    private String name;
    private Map<String, TrainAtStation> stoppageMap = new HashMap<>();

    public Train(int trainNo, String name) {
        super();
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
        TrainAtStation trainAtStation = stoppageMap.get(stId);
        if(trainAtStation!=null){
            return trainAtStation.getArr();
        }
        return null;
    }

    public LocalTime getDept(String stId) {
        TrainAtStation trainAtStation = stoppageMap.get(stId);
        if(trainAtStation!=null){
            return trainAtStation.getDept();
        }
        return null;
    }

    public boolean addStoppage(Station station, LocalTime arrival, LocalTime departure) {
        TrainAtStation trainAtStation = new TrainAtStation(station.getId(), this.trainNo, arrival, departure);
        if(!station.addTrain(trainAtStation)){
            return false;
        }
        stoppageMap.put(station.getId(), trainAtStation);
        return true;
    }

    @SuppressWarnings("unused")
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

