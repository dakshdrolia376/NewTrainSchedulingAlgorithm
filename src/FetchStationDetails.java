import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            File file = new File(this.pathDatabaseStation + File.separator + "indexStation.db");
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
            FileWriter fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStation.db");
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
        String url = "https://indiarailinfo.com/station/map/" + stationIndexNo;
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

    public void putAllStationsInDatabase(){

        List<String> stationNames = new ArrayList<>();
        List<String> stationIds = new ArrayList<>();
        List<Integer> stationIndexes = new ArrayList<>();

        FileReader fReader;
        BufferedReader bReader;
        String line;
        String stationName;
        String stationId;
        Pattern pattern = Pattern.compile("<meta property=\"og:url\" content=\".*?\">");
        Matcher matcher;

        for(int i=1;i<20000;i++){
            System.out.println(i);
            stationName ="";
            stationId = "";
            try {
                String pathStation = this.pathDatabaseStation + File.separator + "station_details_" + i+".txt";
                if(!new File(pathStation).exists()){
                    continue;
                }
                fReader = new FileReader(pathStation);
                bReader = new BufferedReader(fReader);
                while ((line = bReader.readLine()) != null) {
                    matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String temp = matcher.group().split("\\s+")[2];
                        String temp1[] = temp.split("/");
                        if (temp1.length >= 5) {
                            stationName = temp1[4].toLowerCase();
                            stationId = stationName.trim().replaceAll(".*-", "").toLowerCase();
                        }
                        break;
                    }
                }
                bReader.close();
                fReader.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            if(!stationName.equals("") && !stationId.equals("")){
                stationNames.add(stationName);
                stationIds.add(stationId);
                stationIndexes.add(i);
            }
        }

        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<List<Integer>>(){}.getType();
            FileWriter fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStationNos.db");
            gson.toJson(stationIndexes,listType,fileWriter);
            fileWriter.close();
            listType = new TypeToken<List<String>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStationIds.db");
            gson.toJson(stationIds,listType,fileWriter);
            fileWriter.close();
            listType = new TypeToken<List<String>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStationNames.db");
            gson.toJson(stationNames,listType,fileWriter);
            fileWriter.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        DatabaseConnector databaseConnector = new DatabaseConnector();
        boolean ans = databaseConnector.insertIntoStationBatch(stationIndexes, stationIds, stationNames);
        if(!ans){
            System.out.println("Unable to put trains into database");
        }
        databaseConnector.closeConnection();
    }
}
