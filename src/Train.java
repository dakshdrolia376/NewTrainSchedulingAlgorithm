import java.util.*;

import static java.util.Objects.requireNonNull;

public class Train {

    private final int trainNo;
    private final String name;
    private final Map<String, TrainAtStation> stoppageMap;
    private final Map<String, Integer> duplicateStationIds;
    private int stoppageNo=0;

    public Train(int trainNo, String name) {
        requireNonNull(name, "The Train name is null.");
        this.stoppageMap = new HashMap<>();
        this.trainNo = trainNo;
        this.name = name;
        this.duplicateStationIds = new HashMap<>();
    }

    @SuppressWarnings("ununsed")
    public String getName() {
        return this.name;
    }

    @SuppressWarnings("unused")
    public int getTrainNo() {
        return this.trainNo;
    }

    public TrainAtStation getStationInfo(String stId, int count){
        requireNonNull(stId,"Station id is null.");
        if(count==0){
            return this.stoppageMap.getOrDefault(stId,null);
        }
        else{
            return this.stoppageMap.getOrDefault(stId+count, null);
        }
    }

    public TrainAtStation getStationInfo(String stId, String stIdNext, boolean first) {
        requireNonNull(stId,"Station id is null.");
        requireNonNull(stIdNext,"Next Station id is null.");
        if(!this.stoppageMap.containsKey(stId)){
            return null;
        }
        if(!this.stoppageMap.containsKey(stIdNext)){
            return this.stoppageMap.get(stId);
        }
        TrainAtStation s1, s2;
        int count1 = this.duplicateStationIds.getOrDefault(stId, 0);
        int count2 = this.duplicateStationIds.getOrDefault(stIdNext, 0);
        String st1 = stId;
        for(int i=0;i<=count1;i++){
            s1 = this.stoppageMap.get(st1);
            String st2 = stIdNext;
            for(int j=0;j<=count2;j++){
                s2 = this.stoppageMap.get(st2);
                if(s1.getStoppageNo()==(s2.getStoppageNo()-1)){
                    if(first) {
                        return s1;
                    }
                    else{
                        return s2;
                    }
                }
                st2=stIdNext+ (j+1);
            }
            st1 = stId+ (i+1);
        }
        return null;
    }

    public boolean addStoppage(Station station, TrainTime arrival, TrainTime departure) {
        if(station==null){
            System.err.println("Station is not in route or some error occurred in adding stoppage for train : "+ this.trainNo);
            return false;
        }
        requireNonNull(arrival, "Arrival is null.");
        requireNonNull(departure, "Departure is null.");
        TrainAtStation trainAtStation = new TrainAtStation(station.getId(), this.trainNo, arrival, departure, stoppageNo++);
        String stId = station.getId();
        if(this.stoppageMap.containsKey(stId)){
            int count = this.duplicateStationIds.getOrDefault(stId, 0);
            count++;
            this.duplicateStationIds.put(stId, count);
            stId+=count;
        }
        this.stoppageMap.put(stId, trainAtStation);
        return station.addTrain(trainAtStation);
    }

    public List<TrainAtStation> sortArr(List<TrainAtStation> trainAtStationsList) {
        trainAtStationsList.sort(( o1, o2) ->{
            if(o1.getArr().equals(o2.getArr())) {
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
        for (TrainAtStation trainAtStation: trainAtStationsList) {
            stringBuilder.append( trainAtStation.getStoppageNo());
            stringBuilder.append('\t');
            stringBuilder.append( trainAtStation.getStationId());
            stringBuilder.append('\t');
            stringBuilder.append(trainAtStation.getArr());
            stringBuilder.append( '\t');
            stringBuilder.append( trainAtStation.getDept());
            stringBuilder.append( '\n');
        }
        return stringBuilder.toString();
    }
}