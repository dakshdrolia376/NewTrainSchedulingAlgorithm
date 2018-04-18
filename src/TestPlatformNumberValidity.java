import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class StationPlatform{
    List<List<Integer>> platformDetails;
    int noOfPlatform ;
    public StationPlatform(int noOfPlatform){
        platformDetails = new ArrayList<>();
        this.noOfPlatform = noOfPlatform;
        for(int i=0;i<10080;i++){
            platformDetails.add(new ArrayList<>());
        }
    }

    public void add(TrainTime t1, TrainTime t2, int trainNO){
        for(int i=t1.getValue();i<=t2.getValue();i++){
            if(!platformDetails.get(i).contains(trainNO)&& !platformDetails.get(i).contains(trainNO+900000)
                    && !platformDetails.get(i).contains(trainNO-900000) && !platformDetails.get(i).contains(trainNO-10000)
                    && !platformDetails.get(i).contains(trainNO+10000)) {
                platformDetails.get(i).add(trainNO);
            }
        }
    }
}

public class TestPlatformNumberValidity {

    Map<String, StationPlatform> stMap= new HashMap<>();
    private FetchStationDetails fetchStationDetails;

    public TestPlatformNumberValidity(String pathStationDatabase){
        fetchStationDetails = new FetchStationDetails(pathStationDatabase);
    }

    private boolean addTrainFromFile(int trainNo, String pathTrainSchedule, int trainDay){
        int stoppageDay = trainDay;
        try {
            FileReader fReader = new FileReader(pathTrainSchedule);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            TrainTime arrival, departure=null;
            String data[];
            String data1[];
            String stationId;
            int stationNo = 0;
            line = bReader.readLine();
            while(true) {
                if(line==null){
                    break;
                }
                stationNo++;
                data = line.split("\\s+");
                stationId = Scheduler.getStationIdFromName(data[0]);
                StationPlatform st = stMap.getOrDefault(stationId,null);
                if(st==null){
                    int numOfPlatform = fetchStationDetails.getNumberOfPlatform(stationId);
                    if(numOfPlatform<=0){
                        line = bReader.readLine();
                        continue;
                    }
                    stMap.put(stationId, new StationPlatform(numOfPlatform));
                    st = stMap.get(stationId);
                }
                data1 =data[1].split(":");
                arrival = new TrainTime(stoppageDay,Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure!=null && arrival.compareTo(departure)<0){
                    arrival.addDay(1);
                    stoppageDay = arrival.day;
                }

                data1 =data[2].split(":");
                departure = new TrainTime(stoppageDay,Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure.compareTo(arrival)<0){
                    departure.addDay(1);
                    stoppageDay = departure.day;
                }
                line = bReader.readLine();
                if(arrival.compareTo(departure)!=0 && stationNo>1 && line!=null) {
                    st.add(arrival, departure, trainNo);
                }
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
        TestPlatformNumberValidity tt = new TestPlatformNumberValidity(pathStationDatabase);
        if (tt.addTrainFromFolder(pathTrainBase)) {
            for (String stationId : tt.stMap.keySet()) {
                StationPlatform st = tt.stMap.get(stationId);
                boolean printedHeader = false;
                for(int i=0;i<st.platformDetails.size();i++){
                    if(st.platformDetails.get(i).size()>st.noOfPlatform){
                        if(!printedHeader){
                            System.out.println("************ "+stationId+" "+ st.noOfPlatform);
                            printedHeader = true;
                        }
                        System.out.println(new TrainTime(i).toString()+" "+ st.platformDetails.get(i).toString());
                    }
                }
            }
        }
    }
}
