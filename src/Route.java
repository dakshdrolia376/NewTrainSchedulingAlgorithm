import java.time.LocalTime;
import java.util.ArrayList;

public class Route {
    private ArrayList<Station> path;

    public Route(ArrayList<Station> path) {
        super();
        this.path = path;
    }

    @SuppressWarnings("unused")
    public Station getSrc() {
        return this.path.get(0);
    }

    @SuppressWarnings("unused")
    public Station getDest() {
        return this.path.get(this.path.size()-1);
    }

    @SuppressWarnings("unused")
    public Station getStation(String id) {
        for (Station station: this.path) {
            if(station.getId().equals(id)){
                return station;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public void printInfo() {
        for (Station station:this.path) {
            station.sortArr();
            station.printInfo();
        }
    }

    @SuppressWarnings("unused")
    public ArrayList<String> getStationList() {
        ArrayList<String> stationList = new ArrayList<>();
        for (Station station: this.path) {
            stationList.add(station.getId());
        }
        return stationList;
    }

    @SuppressWarnings("unused")
    public ArrayList<ArrayList<Node>> findSchedule(){
        ArrayList<Double> waitTime = new ArrayList<>();
        for(int i=0;i<this.path.size();i++) {
            waitTime.add(0.0);
        }
        return findFreeSchedule(waitTime,0,0,23,59);
    }

    @SuppressWarnings("unused")
    public ArrayList<ArrayList<Node>> findSchedule(int startHrs, int startMinutes, int endHrs, int endMinutes){
        ArrayList<Double> waitTime = new ArrayList<>();
        for(int i=0;i<this.path.size();i++) {
            waitTime.add(0.0);
        }
        return findFreeSchedule(waitTime, startHrs, startMinutes, endHrs, endMinutes);
    }

    @SuppressWarnings("unused")
    public ArrayList<ArrayList<Node>> findSchedule(int endHrs, int endMinutes){
        ArrayList<Double> waitTime = new ArrayList<>();
        for(int i=0;i<this.path.size();i++) {
            waitTime.add(0.0);
        }
        return findFreeSchedule(waitTime, 0, 0, endHrs, endMinutes);
    }

    @SuppressWarnings("unused")
    public ArrayList<ArrayList<Node>> findSchedule(int hrs, int minutes, boolean start){
        ArrayList<Double> waitTime = new ArrayList<>();
        for(int i=0;i<this.path.size();i++) {
            waitTime.add(0.0);
        }
        if(start) {
            return findFreeSchedule(waitTime, hrs, minutes, 23, 59);
        }
        else {
            return findFreeSchedule(waitTime, 0, 0, hrs, minutes);
        }
    }

    @SuppressWarnings("unused")
    public ArrayList<ArrayList<Node>> findSchedule(ArrayList<Double> waitTime){
        return findFreeSchedule(waitTime,0,0,23,59);
    }

    @SuppressWarnings("unused")
    public ArrayList<ArrayList<Node>> findSchedule(ArrayList<Double> waitTime,int startHrs, int startMinutes, int endHrs, int endMinutes){
        return findFreeSchedule(waitTime, startHrs, startMinutes, endHrs, endMinutes);
    }

    @SuppressWarnings("unused")
    public ArrayList<ArrayList<Node>> findSchedule(ArrayList<Double> waitTime,int endHrs, int endMinutes){
        return findFreeSchedule(waitTime, 0, 0, endHrs, endMinutes);
    }

    @SuppressWarnings("unused")
    public ArrayList<ArrayList<Node>> findSchedule(ArrayList<Double> waitTime,int hrs, int minutes, boolean start){
        if(start) {
            return findFreeSchedule(waitTime, hrs, minutes, 23, 59);
        }
        else {
            return findFreeSchedule(waitTime, 0, 0, hrs, minutes);
        }
    }

    private ArrayList<ArrayList<Node>> findFreeSchedule(ArrayList<Double> waitTime, int startHrs, int startMinutes, int endHrs, int endMinutes) {
        ArrayList<ArrayList<Node>> nextDayNodes = new ArrayList<>();
        if(endHrs<startHrs || (endHrs==startHrs && endMinutes < startMinutes)) {
            nextDayNodes = findFreeSchedule(waitTime,0,0,endHrs,endMinutes);
            endHrs = 23;
            endMinutes = 59;
        }
        
        LocalTime start,end,slotDept;
        LocalTime scheduleSlotDept,scheduleSlotDept1,scheduleSlotDept2;

        start = LocalTime.of(startHrs, startMinutes);
        end = LocalTime.of(endHrs, endMinutes);
        ArrayList<ArrayList<Node>> nodes = new ArrayList<>();
        
        LocalTime temp1;
        Station station;
        
        for(int i=0;i<this.path.size();i++) {
            slotDept = start;
            station = this.path.get(i);
            station.sortDept();

            ArrayList<TrainAtStation> schedule = station.getStationSchedule();
            ArrayList<Node> stationNodes = new ArrayList<>();
            boolean endNodeRequired = true;

            for(TrainAtStation trainAtStation: schedule) {

                scheduleSlotDept  = trainAtStation.getDept();
                scheduleSlotDept1 = Scheduler.subMinutes(scheduleSlotDept, 3);
                scheduleSlotDept2 = Scheduler.addMinutes(scheduleSlotDept, 3);

                temp1 = slotDept;
                while(temp1.compareTo(scheduleSlotDept1)<=0 && temp1.compareTo(end)<=0) {
                    stationNodes.add(new Node(temp1, station.getId(), station.getDistance(), waitTime.get(i)));
                    temp1 = Scheduler.addMinutes(temp1, 1);
                }

                if(scheduleSlotDept1.compareTo(scheduleSlotDept2)>0) {
                    // for train at minutes before end of day
                    endNodeRequired = false;
                    break;
                }
                if(slotDept.compareTo(scheduleSlotDept2)<=0 ) {
                    slotDept = scheduleSlotDept2;
                }
            }
            if(endNodeRequired) {
                temp1 = slotDept;
                int comp = temp1.compareTo(end);
                while(comp<=0) {
                    stationNodes.add(new Node(temp1, station.getId(), station.getDistance(), waitTime.get(i)));
                    if(comp==0) {
                        break;
                    }
                    temp1 = Scheduler.addMinutes(temp1, 1);
                    comp = temp1.compareTo(end);
                }
            }
            nodes.add(stationNodes);
        }
        for(int i=0;i<nextDayNodes.size();i++) {
            nodes.get(i).addAll(nextDayNodes.get(i));
        }
        return nodes;
    }
}
