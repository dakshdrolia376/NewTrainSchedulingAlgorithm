import java.time.LocalTime;
import java.util.ArrayList;

public class Train {

    private int trainNo;
    private String name;
    private ArrayList<TrainAtStation> stoppage = new ArrayList<>();

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
        for (TrainAtStation trainAtStation: this.stoppage) {
            if(trainAtStation.getStationId().equals(stId)){
                return trainAtStation.getArr();
            }
        }
        return null;
    }

    public LocalTime getDept(String stId) {
        for (TrainAtStation trainAtStation: this.stoppage) {
            if(trainAtStation.getStationId().equals(stId)){
                return trainAtStation.getDept();
            }
        }
        return null;
    }

    public boolean addStoppage(Station station, LocalTime arrival, LocalTime departure) {
        TrainAtStation TrainAtStation = new TrainAtStation(station.getId(), this.trainNo, arrival, departure);
        if(!station.addTrain(TrainAtStation)){
            return false;
        }
        this.stoppage.add(TrainAtStation);
        return true;
    }

    @SuppressWarnings("unused")
    public ArrayList<TrainAtStation> getStoppage() {
        return this.stoppage;
    }

    @SuppressWarnings("unused")
    public void printInfo() {
        System.out.println("**********************************************************");
        System.out.println("Train No: " + this.trainNo + " name: " + this.name);
        System.out.println("Station\tArrival\tDeparture");
        for (TrainAtStation trainAtStation: this.stoppage) {
            System.out.println( trainAtStation.getStationId() + "\t" + trainAtStation.getArr() + "\t"  + trainAtStation.getDept());
        }
        System.out.println("**********************************************************");
    }
}

