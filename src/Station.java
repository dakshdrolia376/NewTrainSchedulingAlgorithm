import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;

public class Station {

    private final String name;
    private final String id;
    private final double distance;
    private final boolean isDirectLineAvailable;
    private final int noOfUpPlatform;
    private final int noOfDownPlatform;
    private final int noOfDualPlatform;
    private final List<TrainAtStation> arrDeptSchedule;

    public Station(String id, String name, double distance, boolean isDirectLineAvailable, int noOfUpPlatform,
                   int noOfDownPlatform, int noOfDualPlatform) {
        requireNonNull(id, "Station id is null.");
        requireNonNull(name, "Station name is null.");
        this.arrDeptSchedule = new ArrayList<>();
        this.id = id;
        this.name = name;
        this.distance = distance;
        this.isDirectLineAvailable = isDirectLineAvailable;
        this.noOfUpPlatform = noOfUpPlatform;
        this.noOfDownPlatform = noOfDownPlatform;
        this.noOfDualPlatform = noOfDualPlatform;
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

    public boolean isDirectLineAvailable() {
        return this.isDirectLineAvailable;
    }

    public int getNoOfUpPlatform() {
        return this.noOfUpPlatform + this.noOfDualPlatform;
    }

    public int getNoOfDownPlatform() {
        return this.noOfDownPlatform + this.noOfDualPlatform;
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

    // public void sortDept() {
    //     this.arrDeptSchedule.sort(( o1, o2) ->{
    //             if(o1.getDept().equals(o2.getDept())) {
    //                 return o1.getArr().compareTo(o2.getArr());
    //             }
    //             return o1.getDept().compareTo(o2.getDept());
    //     });
    // }

    public List<Node> getNodesFreeList(TrainTime startTime, TrainTime endTime){
        List<Node> stationNodes = new ArrayList<>();
        TrainTime slotDept = new TrainTime(startTime);
        while(slotDept.compareTo(endTime)<0) {
            stationNodes.add(new Node(slotDept, this.id));
            slotDept.addMinutes(1);
        }
        stationNodes.add(new Node(slotDept, this.id));
        return stationNodes;
    }

    @Override
    public String toString() {
        this.sortArr();
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("Station id: ");
        stringBuilder.append(this.id);
        stringBuilder.append(" Name: ");
        stringBuilder.append(this.name);
        stringBuilder.append(" Distance: ");
        stringBuilder.append(this.distance);
        stringBuilder.append(" No of trains passing: ");
        stringBuilder.append(this.arrDeptSchedule.size());
        stringBuilder.append(" DirectLine: ");
        stringBuilder.append(this.isDirectLineAvailable);
        stringBuilder.append(" Up No: ");
        stringBuilder.append(this.noOfUpPlatform);
        stringBuilder.append(" Down No: ");
        stringBuilder.append(this.noOfDownPlatform);
        stringBuilder.append(" Dual No: ");
        stringBuilder.append(this.noOfDownPlatform);
        stringBuilder.append('\n');
        stringBuilder.append("Train\tArrival\tDeparture");
        stringBuilder.append('\n');
        for (TrainAtStation trainAtStation: this.arrDeptSchedule) {
            stringBuilder.append( trainAtStation.getTrainNo());
            stringBuilder.append( '\t');
            stringBuilder.append( trainAtStation.getArr());
            stringBuilder.append( '\t');
            stringBuilder.append( trainAtStation.getDept());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}
