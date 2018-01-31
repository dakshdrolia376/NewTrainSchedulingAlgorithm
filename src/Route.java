import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class Route {
    private final Map<String, Station> mapStation;
    private final List<String> stationOrder;

    public Route(){
        this.mapStation = new HashMap<>();
        this.stationOrder = new ArrayList<>();
    }

    public boolean addStation(String id, String name, double distance, boolean isDirectLineAvailable,
                              int noOfUpPlatform, int noOfDownPlatform, int noOfDualPlatform){
        requireNonNull(id, "Station id is null.");
        requireNonNull(name, "Station name is null.");
        this.mapStation.put(id, new Station(id, name, distance, isDirectLineAvailable, noOfUpPlatform,
                noOfDownPlatform, noOfDualPlatform));
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

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        for (Station station:this.mapStation.values()) {
            stringBuilder.append(station.toString());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    public List<List<Node>> getFreeSlots(int minDelayBwTrains, int startDay, int startHrs, int startMinutes,
                                           int endDay, int endHrs, int endMinutes, boolean isSingleDay) {
        if(minDelayBwTrains <0){
            throw new IllegalArgumentException("Min delay between two consecutive train is negative.");
        }

        List<List<Node>> nextWeekSlots = new ArrayList<>();
        if(endDay<startDay || (startDay==endDay && endHrs<startHrs) ||
                (startDay==endDay && endHrs==startHrs && endMinutes < startMinutes)) {
            if(isSingleDay && startDay==endDay) {
                System.out.println("Single day scheduling");
                nextWeekSlots = getFreeSlots(minDelayBwTrains, startDay,0,0,
                    endDay,endHrs,endMinutes,true);
                endHrs = 23;
                endMinutes = 59;
            }
            else{
                System.out.println("Complete scheduling");
                nextWeekSlots = getFreeSlots(minDelayBwTrains, 0,0,0,
                        endDay,endHrs,endMinutes,isSingleDay);
                endDay = 6;
                endHrs = 23;
                endMinutes = 59;
            }
        }

        TrainTime start, end;
        start = new TrainTime(startDay, startHrs, startMinutes);
        end = new TrainTime(endDay, endHrs, endMinutes);
        List<List<Node>> nodes = new ArrayList<>(this.stationOrder.size());

        for(String stationId: this.stationOrder) {
            Station station = this.mapStation.get(stationId);
            if(station==null){
                throw  new RuntimeException("Unable to load station");
            }
            nodes.add(station.getNodesFreeList(start, end, minDelayBwTrains));
        }
        for(int i=0;i<nextWeekSlots.size();i++) {
            nodes.get(i).addAll(nextWeekSlots.get(i));
        }
        return nodes;
    }
}
