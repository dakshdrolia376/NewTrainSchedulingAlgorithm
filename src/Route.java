import java.util.*;

import static java.util.Objects.requireNonNull;

public class Route {
    private final Map<String, Station> mapStation;
    private final List<String> stationOrder;

    public Route(){
        this.mapStation = new HashMap<>();
        this.stationOrder = new ArrayList<>();
    }

    public boolean addStation(String id, String name, double distance, boolean isDirectLineAvailable,
                              int noOfUpPlatform, int noOfDownPlatform, int noOfDualPlatform, int noOfUpTrack,
                              int noOfDownTrack, int noOfDualTrack){
        requireNonNull(id, "Station id is null.");
        requireNonNull(name, "Station name is null.");
        this.mapStation.put(id, new Station(id, name, distance, isDirectLineAvailable, noOfUpPlatform,
                noOfDownPlatform, noOfDualPlatform, noOfUpTrack, noOfDownTrack, noOfDualTrack));
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
        return mapStation.getOrDefault(id, null);
    }

    public List<List<Node>> getFreeSlots(TrainTime start, TrainTime end) {
        List<List<Node>> nodes = new ArrayList<>(this.stationOrder.size());
        for(String stationId: this.stationOrder) {
            Station station = this.mapStation.get(stationId);
            if(station==null){
                throw  new RuntimeException("Unable to load station");
            }
            nodes.add(station.getNodesFreeList(start, end));
        }
        return nodes;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        for (Station station:this.mapStation.values()) {
            stringBuilder.append(station.toString());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}
