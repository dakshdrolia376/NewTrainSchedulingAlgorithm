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

    public ArrayList<String> getStationList() {
        ArrayList<String> stationList = new ArrayList<>();
        for (Station station: this.path) {
            stationList.add(station.getId());
        }
        return stationList;
    }

    ArrayList<ArrayList<String>> getFreeSlots(int startHrs, int startMinutes, int endHrs, int endMinutes) {
        ArrayList<ArrayList<String>> nextDaySlots = new ArrayList<>();
        if(endHrs<startHrs || (endHrs==startHrs && endMinutes < startMinutes)) {
            nextDaySlots = getFreeSlots(0,0,endHrs,endMinutes);
            endHrs = 23;
            endMinutes = 59;
        }

        LocalTime start,end,slotDept;
        LocalTime scheduleSlotDept,scheduleSlotDept1,scheduleSlotDept2;

        start = LocalTime.of(startHrs, startMinutes);
        end = LocalTime.of(endHrs, endMinutes);
        ArrayList<ArrayList<String>> nodes = new ArrayList<>();

        LocalTime temp1;
        String stationId;

        for(Station station: this.path) {
            slotDept = start;
            stationId = station.getId();
            station.sortDept();
            ArrayList<TrainAtStation> schedule = station.getStationSchedule();

            ArrayList<String> stationNodes = new ArrayList<>();
            boolean endNodeRequired = true;
            for(TrainAtStation trainAtStation: schedule) {
                scheduleSlotDept  = trainAtStation.getDept();
                scheduleSlotDept1 = Scheduler.subMinutes(scheduleSlotDept, 3);
                scheduleSlotDept2 = Scheduler.addMinutes(scheduleSlotDept, 3);

                temp1 = slotDept;
                while(temp1.compareTo(scheduleSlotDept1)<=0 && temp1.compareTo(end)<=0) {
                    stationNodes.add(Scheduler.getNodeLabel(stationId,temp1));
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
                    stationNodes.add(Scheduler.getNodeLabel(stationId,temp1));
                    if(comp==0) {
                        break;
                    }
                    temp1 = Scheduler.addMinutes(temp1, 1);
                    comp = temp1.compareTo(end);
                }
            }
            nodes.add(stationNodes);
        }
        for(int i=0;i<nextDaySlots.size();i++) {
            nodes.get(i).addAll(nextDaySlots.get(i));
        }
        return nodes;
    }
}
