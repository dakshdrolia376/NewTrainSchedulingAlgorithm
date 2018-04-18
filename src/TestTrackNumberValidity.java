import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class StationTrack{
    List<List<Integer>> trackDetails;
    int noOfTrack ;
    public StationTrack(int noOfTrack){
        trackDetails = new ArrayList<>();
        this.noOfTrack = noOfTrack;
        for(int i=0;i<1440;i++){
            trackDetails.add(new ArrayList<>());
        }
    }

    public void add(TrainTime t1, TrainTime t2, int trainNO){
        for(int i=t1.getValue();i<=t2.getValue();i++){
            if(i>=1440){
                break;
            }
            if(!trackDetails.get(i).contains(trainNO)&& !trackDetails.get(i).contains(trainNO+900000)
                    && !trackDetails.get(i).contains(trainNO-900000) && !trackDetails.get(i).contains(trainNO-10000)
                    && !trackDetails.get(i).contains(trainNO+10000)) {
                trackDetails.get(i).add(trainNO);
            }
        }
    }
}

public class TestTrackNumberValidity {

    Map<String, StationTrack> stMap= new HashMap<>();
    private FetchStationDetails fetchStationDetails;

    public TestTrackNumberValidity(String pathStationDatabase){
        fetchStationDetails = new FetchStationDetails(pathStationDatabase);
    }

    private boolean addTrainFromFile(int trainNo, String pathTrainSchedule, int trainDay){
        System.out.println(trainNo);
        int stoppageDay = trainDay;
        try {
            FileReader fReader = new FileReader(pathTrainSchedule);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            TrainTime arrival, departure=null;
            String data[];
            String data1[];
            String stationId;
            String prevStationId="";
            int stationNo = 0;

            while((line = bReader.readLine())!=null) {
                stationNo++;
                data = line.split("\\s+");
                stationId = Scheduler.getStationIdFromName(data[0]);
                data1 =data[1].split(":");
                arrival = new TrainTime(stoppageDay,Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure!=null && arrival.compareTo(departure)<0){
                    arrival.addDay(1);
                    stoppageDay = arrival.day;
                }

                if(stationNo>1) {
                    StationTrack st = stMap.getOrDefault(prevStationId + "->" + stationId, null);
                    if (st == null) {
                        int numOfTrack = fetchStationDetails.getNumberOfTracks(stationId);
                        if (numOfTrack <= 0) {
                            continue;
                        }
                        stMap.put(prevStationId + "->" + stationId, new StationTrack(numOfTrack));
                        st = stMap.get(prevStationId + "->" + stationId);
                    }
                    st.add(departure, arrival, trainNo);
                }

                data1 =data[2].split(":");
                departure = new TrainTime(stoppageDay,Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure.compareTo(arrival)<0){
                    departure.addDay(1);
                    stoppageDay = departure.day;
                }
                prevStationId = stationId;
            }
            bReader.close();
            fReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean addTrainFromFolderSingleDay(String pathOldTrainScheduleFolder, int trainDay){
        System.out.println("Loading day "+ trainDay);
        File[] listOfFiles = new File(pathOldTrainScheduleFolder).listFiles();
        if(listOfFiles==null) {
            System.out.println("No old trains found : " +pathOldTrainScheduleFolder);
            return true;
        }

        for (File file: listOfFiles) {
            if(file.isFile()) {
                int trainNo;
                try {
                    trainNo = Integer.parseInt(file.getName().split("\\.")[0]);
                }
                catch (Exception e) {
                    System.out.print("File name should be train Number.");
                    System.out.println("Skipping file : " + file.getPath());
                    e.printStackTrace();
                    continue;
                }
                if(!addTrainFromFile(trainNo,file.getPath(), trainDay)){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean addTrainFromFolder(String pathOldTrainScheduleFolder){

        return addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                "day0", 0) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day1", 1) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day2", 2) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day3", 3) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day4", 4) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day5", 5) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day6", 6);
    }

    public static void main(String... S){
        String pathTrainBase = "data"+ File.separator+"final";
        String pathTemp = "data"+File.separator+"temp";
        String pathStationDatabase = pathTemp+File.separator+"databaseStation";
        TestTrackNumberValidity tt = new TestTrackNumberValidity(pathStationDatabase);
        if (tt.addTrainFromFolder(pathTrainBase)) {
            for (String stationId : tt.stMap.keySet()) {
                StationTrack st = tt.stMap.get(stationId);
                boolean printedHeader = false;
                for(int i=0;i<st.trackDetails.size();i++){
                    if(st.trackDetails.get(i).size()>st.noOfTrack){
                        if(!printedHeader){
                            System.out.println("************ "+stationId+" "+ st.noOfTrack);
                            printedHeader = true;
                        }
                        System.out.println(new TrainTime(i).toString()+" "+ st.trackDetails.get(i).toString());
                    }
                }
            }
        }
    }
}
