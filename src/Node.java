import static java.util.Objects.requireNonNull;

public class Node {
    private final TrainTime time;
    private final String stationId;
    private boolean isValid;

    public Node(TrainTime time, String stationId) {
        requireNonNull(stationId, "The station id is null.");
        this.time = time!=null?(new TrainTime(time)):null;
        this.stationId = stationId;
        this.isValid = true;
    }

    public Node(TrainTime time, String stationId, boolean isValid) {
        requireNonNull(stationId, "The station id is null.");
        this.time = time!=null?(new TrainTime(time)):null;
        this.stationId = stationId;
        this.isValid = isValid;
    }

    public Node(String label) {
        requireNonNull(label, "The label is null.");
        String data[] = label.split(":");
        this.stationId = data[0];
        this.isValid = true;
        TrainTime trainTime = null;
        if(data.length==4){
            try{
                trainTime = new TrainTime(Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
            }
            catch (Exception e){
                System.out.println("Invalid label: " + label);
            }

        }
        this.time = trainTime;
    }

    public boolean equals(Node node){
        return this.toString().equalsIgnoreCase(node.toString());
    }

    public boolean isValid(){
        return this.isValid;
    }

    public String toString() {
        // if(!isValid){
        //     return "";
        // }
        if(this.time!=null) {
            return this.stationId +":" + this.time.toString();
        }
        else {
            return this.stationId;
        }
    }

    public String getStationId() {
        return this.stationId;
    }

    public TrainTime getTime() {
        return this.time;
    }

}
