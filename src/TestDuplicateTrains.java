import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.*;

public class TestDuplicateTrains {

    Map<String, List<Integer>> duplicateTrains= new HashMap<>();

    private FetchStationDetails fetchStationDetails;

    public TestDuplicateTrains(String pathStationDatabase){
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
            String prevStationId="";
            int stationNo = 0;
            while((line = bReader.readLine())!=null) {
                stationNo++;
                data = line.split("\\s+");
                stationId = Scheduler.getStationIdFromName(data[0]);
                int numOfTrack = fetchStationDetails.getNumberOfTracks(stationId);
                if (numOfTrack <= 0) {
                    continue;
                }
                data1 =data[1].split(":");
                arrival = new TrainTime(stoppageDay,Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure!=null && arrival.compareTo(departure)<0){
                    arrival.addDay(1);
                    stoppageDay = arrival.day;
                }

                if(stationNo>1) {
                    duplicateTrains.putIfAbsent(prevStationId+":"+departure.toString() + "->" +
                            stationId+":"+arrival.toString(), new ArrayList<>());
                    duplicateTrains.get(prevStationId+":"+departure.toString() + "->" +
                            stationId+":"+arrival.toString()).add(trainNo);
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
        try {
            String pathLog = "data" + File.separator + "logs";
            PrintStream o = new PrintStream(new File(pathLog + File.separator + "errDuplicateTrains.log"));
            PrintStream o1 = new PrintStream(new File(pathLog + File.separator + "outputDuplicateTrains.log"));
            // Store current System.out before assigning a new value
            PrintStream console = System.err;
            PrintStream console1 = System.out;
            //
            // Assign o to output stream
            System.setErr(o);
            System.setOut(o1);
            String pathTrainBase = "data" + File.separator + "final";
            String pathTemp = "data" + File.separator + "temp";
            String pathStationDatabase = pathTemp + File.separator + "databaseStation";
            TestDuplicateTrains tt = new TestDuplicateTrains(pathStationDatabase);
            Map<String, Integer> sameTrains = new HashMap<>();
            if (tt.addTrainFromFolder(pathTrainBase)) {
                for (String key : tt.duplicateTrains.keySet()) {
                    List<Integer> trainList = tt.duplicateTrains.get(key);
                    Collections.sort(trainList);
                    if (trainList.size() > 1) {
                        int count = sameTrains.getOrDefault(trainList.toString(),0);
                        sameTrains.put(trainList.toString(), ++count);
                        // System.out.println(key + "\t" + trainList.toString());
                    }
                }
            }
            for(String key:sameTrains.keySet()){
                System.out.println(sameTrains.get(key)+"\t"+ key);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
