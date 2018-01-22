import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Station {

    private final String name;
    private final String id;
    private final double distance;
    private final List<TrainAtStation> arrDeptSchedule;

    public Station(String id, String name, double distance) {
        requireNonNull(id, "Station id is null.");
        requireNonNull(name, "Station name is null.");
        arrDeptSchedule = new ArrayList<>();
        this.id = id;
        this.name = name;
        this.distance = distance;
    }

    public List<TrainAtStation> getStationSchedule() {
        return this.arrDeptSchedule;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public double getDistance() {
        return this.distance;
    }

    public boolean addTrain(TrainAtStation TrainAtStation) {
        return this.arrDeptSchedule.add(TrainAtStation);
    }

    public void sortArr() {
        this.arrDeptSchedule.sort(( o1, o2) ->{
            if(o1.getArr().equals(o2.getArr())) {
                return o1.getDept().compareTo(o2.getDept());
            }
            return o1.getArr().compareTo(o2.getArr());
        });
    }

    public void sortDept() {
        this.arrDeptSchedule.sort(( o1, o2) ->{
                if(o1.getDept().equals(o2.getDept())) {
                    return o1.getArr().compareTo(o2.getArr());
                }
                return o1.getDept().compareTo(o2.getDept());
        });
    }

    public void printInfo() {
        System.out.println("**********************************************************");
        System.out.println("Station id: " + this.id + " Name: "+ this.name + " Distance: " + this.distance + " No of trains passing" + this.arrDeptSchedule.size());
        System.out.println("Train\tArrival\tDeparture");
        for (TrainAtStation trainAtStation: this.arrDeptSchedule) {
            System.out.println( trainAtStation.getTrainNo() + "\t" + trainAtStation.getArr() + "\t"  + trainAtStation.getDept());
        }
        System.out.println("**********************************************************");
    }
}
