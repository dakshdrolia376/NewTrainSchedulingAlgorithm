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

    @SuppressWarnings("unused")
    public boolean isDirectLineAvailable() {
        return this.isDirectLineAvailable;
    }

    public int getNoOfUpPlatform() {
        return this.noOfUpPlatform + this.noOfDualPlatform;
    }

    public int getNoOfDownPlatform() {
        return this.noOfDownPlatform + this.noOfDualPlatform;
    }

    @SuppressWarnings("unused")
    public int getNoOfDualPlatform() {
        return this.noOfDualPlatform;
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

    public List<Node> getNodesFreeList(TrainTime startTime, TrainTime endTime, int minDelayBwTrains){
        List<Node> stationNodes = new ArrayList<>();
        TrainTime slotDept = new TrainTime(startTime);
        boolean endNodeRequired = true;
        this.sortDept();
        TrainTime scheduleSlotDept1,scheduleSlotDept2;
        TrainTime temp1;
        for(TrainAtStation trainAtStation: this.arrDeptSchedule) {
            scheduleSlotDept1 = new TrainTime(trainAtStation.getDept());
            scheduleSlotDept1.subMinutes(minDelayBwTrains);
            scheduleSlotDept2 = new TrainTime(trainAtStation.getDept());
            scheduleSlotDept2.addMinutes(minDelayBwTrains);
            temp1 = slotDept;
            while(temp1.compareTo(scheduleSlotDept1)<=0 && temp1.compareTo(endTime)<=0) {
                stationNodes.add(new Node(temp1, this.id));
                temp1.addMinutes(1);
            }
            while(temp1.compareTo(scheduleSlotDept2)<0 && temp1.compareTo(endTime)<=0) {
                stationNodes.add(new Node(temp1, this.id, false));
                temp1.addMinutes(1);
            }
            if(scheduleSlotDept1.compareTo(scheduleSlotDept2)>0) {
                // for train at minutes before end of week
                endNodeRequired = false;
                break;
            }
            if(slotDept.compareTo(scheduleSlotDept2)<=0 ) {
                slotDept = scheduleSlotDept2;
            }
        }
        if(endNodeRequired) {
            temp1 = slotDept;
            int comp = temp1.compareTo(endTime);
            while(comp<=0) {
                stationNodes.add(new Node(temp1, this.id));
                if(comp==0) {
                    break;
                }
                temp1.addMinutes(1);
                comp = temp1.compareTo(endTime);
            }
        }
        return stationNodes;
    }
}
