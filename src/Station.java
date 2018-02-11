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
    private final int noOfUpTrack;
    private final int noOfDownTrack;
    private final int noOfDualTrack;
    private final List<TrainAtStation> arrDeptSchedule;

    public Station(String id, String name, double distance, boolean isDirectLineAvailable, int noOfUpPlatform,
                   int noOfDownPlatform, int noOfDualPlatform, int noOfUpTrack, int noOfDownTrack, int noOfDualTrack) {
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
        this.noOfUpTrack = noOfUpTrack;
        this.noOfDownTrack = noOfDownTrack;
        this.noOfDualTrack =noOfDualTrack;
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
        return this.noOfUpPlatform;
    }

    public int getNoOfDownPlatform() {
        return this.noOfDownPlatform;
    }

    public int getNoOfDualPlatform() {
        return this.noOfDualPlatform;
    }

    public int getNoOfUpTrack() {
        return this.noOfUpTrack;
    }

    public int getNoOfDownTrack() {
        return this.noOfDownTrack;
    }

    public int getNoOfDualTrack() {
        return this.noOfDualTrack;
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

    public List<Node> getNodesFreeList(TrainTime startTime, TrainTime endTime, boolean isSingleDay){
        List<Node> nextWeekNodes  = new ArrayList<>();
        if(endTime.compareTo(startTime)<0) {
            if(isSingleDay && startTime.day==endTime.day) {
                System.out.println("Single day scheduling");
                nextWeekNodes = getNodesFreeList(new TrainTime(startTime.day,0,0),endTime, isSingleDay);
                endTime = new TrainTime(endTime.day, 23, 59);
            }
            else{
                System.out.println("Complete scheduling");
                nextWeekNodes = getNodesFreeList(new TrainTime(0,0,0),endTime, isSingleDay);
                endTime = new TrainTime(6, 23, 59);
            }
        }
        List<Node> stationNodes = new ArrayList<>();
        TrainTime slotDept = new TrainTime(startTime);
        while(slotDept.compareTo(endTime)<0) {
            stationNodes.add(new Node(slotDept, this.id));
            slotDept.addMinutes(1);
        }
        stationNodes.add(new Node(slotDept, this.id));
        stationNodes.addAll(nextWeekNodes);
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
        stringBuilder.append('\n');
        stringBuilder.append("DirectLine: ");
        stringBuilder.append(this.isDirectLineAvailable);
        stringBuilder.append(" Platform Up No: ");
        stringBuilder.append(this.noOfUpPlatform);
        stringBuilder.append(" Platform Down No: ");
        stringBuilder.append(this.noOfDownPlatform);
        stringBuilder.append(" Platform Dual No: ");
        stringBuilder.append(this.noOfDownPlatform);
        stringBuilder.append('\n');
        stringBuilder.append("Track Up No: ");
        stringBuilder.append(this.noOfUpTrack);
        stringBuilder.append(" Track Down No: ");
        stringBuilder.append(this.noOfDownTrack);
        stringBuilder.append(" Track Dual No: ");
        stringBuilder.append(this.noOfDualTrack);
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
