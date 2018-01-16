import java.util.ArrayList;

public class Station {

    private String name;
    private String id;
    private double distance;
    private ArrayList<TrainAtStation> arrDeptSchedule = new ArrayList<>();

    public Station(String id, String name, Double distance) {
        super();
        this.id = id;
        this.name = name;
        this.distance = distance;
    }

    public ArrayList<TrainAtStation> getStationSchedule() {
        return this.arrDeptSchedule;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Double getDistance() {
        return this.distance;
    }

    public boolean addTrain(TrainAtStation TrainAtStation) {
        this.arrDeptSchedule.add(TrainAtStation);
        return true;
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

    @SuppressWarnings("unused")
    public void printInfo() {
        System.out.println("**********************************************************");
        System.out.println("Station id: " + this.id + " Name: "+ this.name);
        System.out.println("Train\tArrival\tDeparture");
        for (TrainAtStation trainAtStation: this.arrDeptSchedule) {
            System.out.println( trainAtStation.getTrainNo() + "\t" + trainAtStation.getArr() + "\t"  + trainAtStation.getDept());
        }
        System.out.println("**********************************************************");
    }
}
