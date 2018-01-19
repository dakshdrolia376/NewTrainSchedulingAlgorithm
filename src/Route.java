import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class Route {
    private final Map<String, Station> mapStation;
    private final List<String> stationOrder;

    public Route(){
        mapStation = new HashMap<>();
        stationOrder = new ArrayList<>();
    }

    public boolean addStation(String id, String name, double distance){
        requireNonNull(id, "Station id is null.");
        requireNonNull(name, "Station name is null.");
        mapStation.put(id, new Station(id, name, distance));
        return stationOrder.add(id);
    }

    public List<String> getStationList() {
        return new ArrayList<>(this.stationOrder);
    }

    public int getNumberOfStation(){
        return this.stationOrder.size();
    }

    public Station getStation(String id) {
        requireNonNull(id, "Station id is null.");
        return mapStation.get(id);
    }

    @SuppressWarnings("unused")
    public void printInfo() {
        for (Station station:this.mapStation.values()) {
            station.sortArr();
            station.printInfo();
        }
    }

    List<List<String>> getFreeSlots(int minDelayBwTrains, int startHrs, int startMinutes, int endHrs, int endMinutes) {
        if(minDelayBwTrains <0){
            throw new IllegalArgumentException("Min delay between two consecutive train is negative.");
        }
        if(startHrs <0 || startHrs >=24 || endHrs <0 || endHrs >=24 || startMinutes <0 || startMinutes >=60 || endMinutes <0 || endMinutes >=60){
            throw new IllegalArgumentException("Invalid start/end timings");
        }

        List<List<String>> nextDaySlots = new ArrayList<>();
        if(endHrs<startHrs || (endHrs==startHrs && endMinutes < startMinutes)) {
            nextDaySlots = getFreeSlots(minDelayBwTrains, 0,0,endHrs,endMinutes);
            endHrs = 23;
            endMinutes = 59;
        }

        LocalTime start,end,slotDept;
        LocalTime scheduleSlotDept,scheduleSlotDept1,scheduleSlotDept2;

        start = LocalTime.of(startHrs, startMinutes);
        end = LocalTime.of(endHrs, endMinutes);
        List<List<String>> nodes = new ArrayList<>(this.stationOrder.size());

        LocalTime temp1;
        for(String stationId: this.stationOrder) {
            Station station = this.mapStation.get(stationId);
            if(station==null){
                throw  new RuntimeException("Unable to load station");
            }
            slotDept = start;
            stationId = station.getId();
            station.sortDept();
            List<TrainAtStation> schedule = station.getStationSchedule();
            List<String> stationNodes = new ArrayList<>();

            boolean endNodeRequired = true;
            for(TrainAtStation trainAtStation: schedule) {
                scheduleSlotDept  = trainAtStation.getDept();
                scheduleSlotDept1 = Scheduler.subMinutes(scheduleSlotDept, minDelayBwTrains);
                scheduleSlotDept2 = Scheduler.addMinutes(scheduleSlotDept, minDelayBwTrains);

                temp1 = slotDept;
                while(temp1.compareTo(scheduleSlotDept1)<=0 && temp1.compareTo(end)<=0) {
                    stationNodes.add(KBestSchedule.getNodeLabel(stationId,temp1));
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
                    stationNodes.add(KBestSchedule.getNodeLabel(stationId,temp1));
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
