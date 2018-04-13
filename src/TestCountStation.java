import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCountStation {

    public static void testStationTrain(){
        File file = new File("data/temp/databaseTrain");
        FileReader fReader;
        BufferedReader bReader;
        Map<String, Integer> map;
        String line;

        File[] listOfFiles = file.listFiles();
        if(listOfFiles==null) {
            System.out.println("No trains found : ");
            System.exit(0);
        }

        for (File file1: listOfFiles) {
            if(!file1.getName().endsWith(".route")){
                continue;
            }
            map = new HashMap<>();
            try {
                fReader = new FileReader(file1);
                bReader = new BufferedReader(fReader);
                while ((line = bReader.readLine()) != null) {
                    String[] data = line.split("\\s+");
                    String data1 = Scheduler.getStationIdFromName(data[0]);
                    // if(!data.replaceAll(".*-", "").toLowerCase().equalsIgnoreCase(data1)){
                    //     System.out.println(line+" "+ file1.getName());
                    // }
                    int c = map.getOrDefault(data1+"->"+ data[3], 0);
                    c++;
                    map.put(data1+"->"+ data[3], c);
                    // count++;
                    // prev = data1;
                }

                for(String st: map.keySet()){
                    if(map.get(st)>=2){
                        System.out.println(st+ " "+ file1.getName());
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void testStationOnly(){
        File file = new File("data/temp/databaseStation");
        FileReader fReader;
        BufferedReader bReader;
        String line;

        File[] listOfFiles = file.listFiles();
        if(listOfFiles==null) {
            System.out.println("No trains found : ");
            System.exit(0);
        }

        Map<String,List<String>> map= new HashMap<>();

        for (File file1: listOfFiles) {
            if(!file1.getName().endsWith(".txt")){
                continue;
            }
            try {
                // System.out.print(file1.getName()+"\t");
                fReader = new FileReader(file1);
                bReader = new BufferedReader(fReader);
                while ((line = bReader.readLine()) != null) {
                    String data;
                    if(line.contains("Station Name")){
                        data = line.split(":")[1].trim();
                        String data1 = Scheduler.getStationIdFromName(data);
                        map.putIfAbsent(data1, new ArrayList<>());
                        map.get(data1).add(file1.getName()+ " "+ line);
                        if(!data.replaceAll(".*-", "").toLowerCase().equalsIgnoreCase(data1)){
                            System.out.println(line+" "+ file1.getName());
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        for(List<String> values: map.values()){
            if(values.size()>=2){
                System.out.println(values.toString());
            }
        }
    }

    public static void main(String... S){
        testStationOnly();
        testStationTrain();
    }
}
