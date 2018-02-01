import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class FetchStationDetails {

    private Map<String, Integer> myMap;
    private String pathDatabaseStation;

    public FetchStationDetails(String pathDatabaseStation){
        requireNonNull(pathDatabaseStation, "path of database cant be null.");
        this.pathDatabaseStation = pathDatabaseStation;
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<Map<String, Integer>>(){}.getType();
            File file = new File(this.pathDatabaseStation + File.separator + "index.db");
            if(file.exists()) {
                this.myMap = gson.fromJson(new FileReader(file), listType);
            }
            else{
                this.myMap = new HashMap<>();
            }
        }
        catch (Exception e){
            this.myMap = new HashMap<>();
        }
    }

    public int fetchStationId(String stationId){
        return myMap.getOrDefault(stationId.toLowerCase(), -1);
    }

    public boolean fetchAll(){
        boolean ans = true;
        for(int i=1;i<10000;i++){
            ans = fetchStation(i);
        }
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<Map<String, Integer>>(){}.getType();
            FileWriter fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "index.db");
            gson.toJson(myMap,listType,fileWriter);
            fileWriter.close();
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return ans;
    }

    public boolean fetchStation(int stationIndexNo){
        String url = "https://indiarailinfo.com/departures/" + stationIndexNo;
        String pathStation = this.pathDatabaseStation + File.separator + "station_details_" + stationIndexNo+".txt";
        File file = new File(pathStation);
        if(!file.exists()) {
            if(!new GetWebsite().getWebsite(url, pathStation)){
                System.out.println("Invalid Index : " + stationIndexNo);
                return false;
            }
        }
        if(!parseStationId(pathStation)){
            System.out.println("Unable to parse station Number " + pathStation);
            return false;
        }
        return true;
    }

    public boolean parseStationId(String fileName){
        Pattern pattern = Pattern.compile("Departures from .*");
        Matcher matcher;
        FileReader fReader;
        BufferedReader bReader;
        try {
            fReader = new FileReader(fileName);
            bReader = new BufferedReader(fReader);
            String line;
            while ((line = bReader.readLine()) != null) {
                if(line.contains("Departures from")) {
                    matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String temp = matcher.group();
                        String temp1[] = temp.split("\\s+");
                        System.out.print(temp + " ");
                        if (temp1.length >= 5) {
                            String stationName = temp1[2].split("/")[0].toLowerCase();
                            int noOfPlatform = Integer.parseInt(temp1[temp1.length - 2].substring(1));
                            this.myMap.put(stationName, noOfPlatform);
                            System.out.println(stationName+">" + noOfPlatform);
                            return true;
                        }
                        else{
                            System.out.println("Unable to find Number of platforms");
                            return false;
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
