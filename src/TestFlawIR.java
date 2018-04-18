import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class TestFlawIR {

    TreeMap<Integer, List<String>> flawMap = new TreeMap<>();

    private boolean addTrainFromFile(int trainNo, String pathTrainSchedule, int trainDay){
        int stoppageDay = trainDay;
        try {
            FileReader fReader = new FileReader(pathTrainSchedule);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            TrainTime arrival, departure=null;
            String data[];
            String data1[];
            String linePrev="";
            while((line = bReader.readLine()) != null) {
                data = line.split("\\s+");
                data1 =data[1].split(":");
                arrival = new TrainTime(stoppageDay,Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure!=null && arrival.compareTo(departure)<0){
                    arrival.addDay(1);
                    stoppageDay = arrival.day;
                }
                if(departure!=null){
                    int cost = Math.abs(arrival.compareTo(departure));
                    if(cost>1000){
                        cost = 10080-cost;
                    }
                    if(cost>=30){
                        flawMap.putIfAbsent(cost, new ArrayList<>());
                        flawMap.get(cost).add(trainNo+" -> "+ linePrev+" -> "+ line+" -> "+ cost);
                    }
                }

                data1 =data[2].split(":");
                departure = new TrainTime(stoppageDay,Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure.compareTo(arrival)<0){
                    departure.addDay(1);
                    stoppageDay = departure.day;
                }
                linePrev = line;
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
        TestFlawIR tt = new TestFlawIR();
        if (tt.addTrainFromFolder(pathTrainBase)) {
            for (Map.Entry m : tt.flawMap.entrySet()) {
                System.out.println(m.getKey() + "     ***************************************");
                System.out.println(m.getValue().toString());
            }
        }
    }
}
