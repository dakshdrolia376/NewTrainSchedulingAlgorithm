package iitp.naman.newtrainschedulingalgorithm;

import iitp.naman.newtrainschedulingalgorithm.util.TrainTime;

import static java.util.Objects.requireNonNull;

/**
 * Class to store stoppage detail.
 */
public class TrainAtStation {
    private final String stationId;
    private final int trainNo;
    private final TrainTime arrival;
    private final TrainTime departure;
    private final int stoppageNo;

    public TrainAtStation(String stationId, int trainNo, TrainTime arrival, TrainTime departure, int stoppageNo) {
        requireNonNull(stationId, "Station id is null.");
        requireNonNull(arrival, "Arrival is null.");
        requireNonNull(departure, "Departure is null.");
        this.stationId = stationId;
        this.trainNo = trainNo;
        this.arrival = arrival;
        this.departure = departure;
        this.stoppageNo = stoppageNo;
    }

    /**
     * Creates default instance of stoppage.
     */
    public TrainAtStation(String key) {
        requireNonNull(key, "Invalid key");
        if (key.equalsIgnoreCase("DefaultConstructorForNull")) {
            this.stationId = null;
            this.trainNo = 0;
            this.arrival = null;
            this.departure = null;
            this.stoppageNo = 0;
        } else {
            throw new IllegalArgumentException("Invalid key");
        }
    }

    /**
     * @return stoppage station id.
     */
    public String getStationId() {
        return this.stationId;
    }

    /**
     * @return stoppage train number.
     */
    public int getTrainNo() {
        return this.trainNo;
    }

    /**
     * @return stoppage arrival time.
     */
    public TrainTime getArr() {
        return this.arrival;
    }

    /**
     * @return stoppage departure time.
     */
    public TrainTime getDept() {
        return this.departure;
    }

    /**
     * @return stoppage number for a train.
     */
    public int getStoppageNo() {
        return this.stoppageNo;
    }
}