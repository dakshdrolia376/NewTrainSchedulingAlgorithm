package iitp.naman.newtrainschedulingalgorithm;

import iitp.naman.newtrainschedulingalgorithm.util.TrainTime;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Class to store train info.
 */
public class Train {
    private final int trainNo;
    private final String name;
    private final Map<String, TrainAtStation> stoppageMap;

    public Train(int trainNo, String name) {
        requireNonNull(name, "The Train name is null.");
        this.stoppageMap = new HashMap<>();
        this.trainNo = trainNo;
        this.name = name;
    }

    /**
     * @return train name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return train number.
     */
    public int getTrainNo() {
        return this.trainNo;
    }

    /**
     * @param stId station id.
     * @return train stoppage details at station.
     */
    public TrainAtStation getStationInfo(String stId) {
        requireNonNull(stId, "Station id is null.");
        return this.stoppageMap.getOrDefault(stId, null);
    }

    /**
     * @param stId     first station id.
     * @param stIdNext next station id.
     * @param first    controls whether to get details of first station or next.
     * @return train stoppage details at station.
     */
    public TrainAtStation getStationInfo(String stId, String stIdNext, boolean first) {
        requireNonNull(stId, "Station id is null.");
        requireNonNull(stIdNext, "Next Station id is null.");
        if (!this.stoppageMap.containsKey(stId)) {
            return null;
        }
        if (!this.stoppageMap.containsKey(stIdNext)) {
            return this.stoppageMap.get(stId);
        }
        TrainAtStation s1, s2;
        s1 = this.stoppageMap.get(stId);
        s2 = this.stoppageMap.get(stIdNext);
        if (s1.getStoppageNo() == (s2.getStoppageNo() - 1)) {
            if (first) {
                return s1;
            } else {
                return s2;
            }
        }
        return null;
    }

    /**
     * Add stoppage for the train.
     *
     * @param station    Station info.
     * @param arrival    arrival time.
     * @param departure  departure time.
     * @param stoppageNo stoppage number in train route.
     * @return true if successful.
     */
    public boolean addStoppage(Station station, TrainTime arrival, TrainTime departure, int stoppageNo) {
        if (station == null) {
            System.err.println("Station is not in route or some error occurred in adding stoppage for train : " + this.trainNo);
            return false;
        }
        requireNonNull(arrival, "Arrival is null.");
        requireNonNull(departure, "Departure is null.");
        TrainAtStation trainAtStation = new TrainAtStation(station.getId(), this.trainNo, arrival, departure, stoppageNo);
        String stId = station.getId();
        if (this.stoppageMap.containsKey(stId)) {
            System.out.println("Station already exists... Please check once again : " + trainNo);
            return false;
        }
        this.stoppageMap.put(stId, trainAtStation);
        return station.addTrain(trainAtStation);
    }

    /**
     * Given a list of stoppages, sorts them according to arrival time.
     *
     * @param trainAtStationsList list of stoppage.
     * @return sorted list of stoppage.
     */
    public List<TrainAtStation> sortArr(List<TrainAtStation> trainAtStationsList) {
        trainAtStationsList.sort((o1, o2) -> {
            if (o1.getArr().equals(o2.getArr())) {
                return o1.getDept().compareTo(o2.getDept());
            }
            return o1.getArr().compareTo(o2.getArr());
        });
        return trainAtStationsList;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("Train No: ");
        stringBuilder.append(this.trainNo);
        stringBuilder.append(" name: ");
        stringBuilder.append(this.name);
        stringBuilder.append('\n');
        stringBuilder.append("No.\tStation\tArrival\tDeparture");
        stringBuilder.append('\n');
        List<TrainAtStation> trainAtStationsList = new ArrayList<>(this.stoppageMap.values());
        trainAtStationsList = sortArr(trainAtStationsList);
        for (TrainAtStation trainAtStation : trainAtStationsList) {
            stringBuilder.append(trainAtStation.getStoppageNo());
            stringBuilder.append('\t');
            stringBuilder.append(trainAtStation.getStationId());
            stringBuilder.append('\t');
            stringBuilder.append(trainAtStation.getArr());
            stringBuilder.append('\t');
            stringBuilder.append(trainAtStation.getDept());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}