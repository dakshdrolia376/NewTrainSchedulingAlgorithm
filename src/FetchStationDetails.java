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
                FileReader fileReader = new FileReader(file);
                this.myMap = gson.fromJson(fileReader, listType);
                fileReader.close();
            }
            else{
                this.myMap = new HashMap<>();
            }
        }
        catch (Exception e){
            this.myMap = new HashMap<>();
        }
    }

    public int getNumberOfPlatform(String stationId){
        int indexStation = this.myMap.getOrDefault(stationId.toLowerCase(), -1);
        if(indexStation==-1){
            return -1;
        }
        int numOfPlatform = -1;
        String fileTrainIndex = this.pathDatabaseStation + File.separator +indexStation+".txt";
        try{
            FileReader fReader;
            BufferedReader bReader;
            fReader = new FileReader(fileTrainIndex);
            bReader = new BufferedReader(fReader);
            String line;
            while((line = bReader.readLine()) != null){
                if(line.contains("Platforms")){
                    numOfPlatform = Integer.parseInt(line.split(" ")[0]);
                    break;
                }
            }
            bReader.close();
            fReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(numOfPlatform<=0){
            return -1;
        }
        else{
            return numOfPlatform;
        }
    }
    public int getNumberOfTracks(String stationId){
        int indexStation = this.myMap.getOrDefault(stationId.toLowerCase(), -1);
        if(indexStation==-1){
            System.out.println("Station Not found. Please try using google search. Station : " + stationId);
            return 1;
        }
        int numOfTrack = -1;
        String fileTrainIndex = this.pathDatabaseStation + File.separator +indexStation+".txt";
        try{
            FileReader fReader;
            BufferedReader bReader;
            fReader = new FileReader(fileTrainIndex);
            bReader = new BufferedReader(fReader);
            String line;
            while((line = bReader.readLine()) != null){
                if(line.contains("Track")){
                    String trackType = line.split(" ")[1];
                    if(trackType.equalsIgnoreCase("double")){
                        numOfTrack = 2;
                    }
                    else if(trackType.equalsIgnoreCase("quadruple")){
                        numOfTrack = 4;
                    }
                    else if(trackType.equalsIgnoreCase("triple")){
                        numOfTrack = 3;
                    }
                    else if(trackType.equalsIgnoreCase("single")){
                        numOfTrack = 1;
                    }
                    else{
                        System.out.println("Unknown track Type : " + trackType);
                    }
                    break;
                }
            }
            bReader.close();
            fReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(numOfTrack<=0){
            return 1;
        }
        else{
            return numOfTrack;
        }
    }

    public boolean fetchAll(){
        boolean ans = true;
        for(int i=1;i<15000;i++){
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
            System.out.println("Unable to parse station Id: " + pathStation);
            return false;
        }

        String pathStationDetailsComplete = this.pathDatabaseStation + File.separator +stationIndexNo+".txt";
        if(!parseStationDetails(pathStation, pathStationDetailsComplete)){
            System.out.println("Unable to parse station details: " + pathStation);
            return false;
        }
        return true;
    }

    public boolean parseStationId(String fileName){
        Pattern pattern = Pattern.compile("<meta property=\"og:url\" content=\".*?\">");
        Matcher matcher;
        FileReader fReader;
        BufferedReader bReader;
        try {
            fReader = new FileReader(fileName);
            bReader = new BufferedReader(fReader);
            String line;
            while ((line = bReader.readLine()) != null) {
                matcher = pattern.matcher(line);
                if(matcher.find()) {
                    String temp = matcher.group().split("\\s+")[2];
                    String temp1[] = temp.split("/");
                    if(temp1.length >= 7) {
                        String stationName = temp1[5].toLowerCase();
                        String stationId = stationName.trim().replaceAll(".*-", "").toLowerCase();
                        String stationIndex = temp1[6];
                        if(stationIndex.endsWith(">")){
                            stationIndex = stationIndex.replace(">", "");
                        }
                        if(stationIndex.endsWith("\"")){
                            stationIndex = stationIndex.replace("\"", "");
                        }
                        this.myMap.put(stationId, Integer.parseInt(stationIndex));
                        bReader.close();
                        fReader.close();
                        return true;
                    }
                    else{
                        System.out.println("Unable to find station id: " + fileName);
                        bReader.close();
                        fReader.close();
                        return false;
                    }
                }
            }
            bReader.close();
            fReader.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean parseStationDetails(String fileName, String pathStationFile){
        System.out.println("Parsing " + fileName);
        Pattern pattern = Pattern.compile("<meta property=\"og:description\" content=\".*?\">");
        Pattern pattern1 = Pattern.compile("<meta property=\"og:url\" content=\".*?\">");
        Matcher matcher;
        Matcher matcher1;
        BufferedWriter bWriter;
        FileWriter fWriter;
        FileReader fReader;
        BufferedReader bReader;
        try {
            fReader = new FileReader(fileName);
            bReader = new BufferedReader(fReader);
            fWriter = new FileWriter(pathStationFile);
            bWriter = new BufferedWriter(fWriter);
            String line;
            while ((line = bReader.readLine()) != null) {
                matcher = pattern.matcher(line);
                matcher1 = pattern1.matcher(line);
                if(matcher1.find()){
                    String temp = matcher1.group().split("\\s+")[2];
                    String temp1[] = temp.split("/");
                    if(temp1.length >= 7) {
                        String stationName = temp1[5].toLowerCase();
                        bWriter.write("Station Name: " +stationName);
                        bWriter.write('\n');
                    }
                }
                else if(matcher.find()) {
                    String temp[] = matcher.group().split("\\.");
                    for(int i=1;i<temp.length-3;i++) {
                        bWriter.write(temp[i].trim());
                        bWriter.write('\n');
                    }
                    break;
                }
            }
            bWriter.close();
            fWriter.close();
            bReader.close();
            fReader.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void putAllStationsInDatabase(){

        List<String> stationNames = new ArrayList<>();
        List<String> stationIds = new ArrayList<>();
        List<Integer> stationIndexes = new ArrayList<>();
        List<String> stationTypes= new ArrayList<>();
        List<String> stationTracks= new ArrayList<>();
        List<Integer> originatingTrains= new ArrayList<>();
        List<Integer> terminatingTrains= new ArrayList<>();
        List<Integer> haltingTrains= new ArrayList<>();
        List<Integer> platforms= new ArrayList<>();
        List<String> elevations= new ArrayList<>();
        List<String> railwayZones= new ArrayList<>();
        List<String> addresses= new ArrayList<>();

        FileReader fReader;
        BufferedReader bReader;
        String line;
        String stationName, stationId, stationType, stationTrack, elevation, railwayZone, address;
        int originatingTrain,terminatingTrain,haltingTrain,platform;

        for(int i=1;i<15000;i++){
            System.out.println(i);
            stationName ="";
            stationId = "";
            stationType = "NA";
            stationTrack = "NA";
            elevation = "NA";
            railwayZone = "NA";
            address = "NA";
            originatingTrain=0;
            terminatingTrain =0;
            haltingTrain = 0;
            platform =0;
            try {
                String pathStation = this.pathDatabaseStation + File.separator + i+".txt";
                if(!new File(pathStation).exists()){
                    System.out.println("file not found " + pathStation);
                    continue;
                }
                fReader = new FileReader(pathStation);
                bReader = new BufferedReader(fReader);
                while ((line = bReader.readLine()) != null) {
                    if(line.contains("Station Name")){
                        stationName = line.split(":")[1].trim();
                        stationId = stationName.replaceAll(".*-", "").toLowerCase();
                    }
                    else if(line.contains("Type of Station")){
                        stationType = line.split(":")[1].trim();
                    }
                    else if(line.contains("Track")){
                        stationTrack = line.split(":")[1].trim();
                    }
                    else if(line.contains("Originating Trains")){
                        try {
                            originatingTrain = Integer.parseInt(line.split(" ")[0]);
                        }
                        catch (NumberFormatException e){
                            originatingTrain = 0;
                        }
                    }
                    else if(line.contains("Terminating Trains")){
                        try {
                            terminatingTrain = Integer.parseInt(line.split(" ")[0]);
                        }
                        catch (NumberFormatException e){
                            terminatingTrain = 0;
                        }
                    }
                    else if(line.contains("Halting Trains")){
                        try {
                            haltingTrain = Integer.parseInt(line.split(" ")[0]);
                        }
                        catch (NumberFormatException e){
                            haltingTrain = 0;
                        }
                    }
                    else if(line.contains("Platforms")){
                        try {
                            platform = Integer.parseInt(line.split(" ")[0]);
                        }
                        catch (NumberFormatException e){
                            platform = 0;
                        }
                    }
                    else if(line.contains("Elevation")){
                        elevation = line.split(":")[1].trim();
                    }
                    else if(line.contains("Railway Zone")){
                        railwayZone = line.split(":")[1].trim().split("/")[0];
                    }
                    else if(line.contains("Station Address")){
                        address = line.split(":")[1].trim();
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
                stationTypes.add(stationType);
                stationTracks.add(stationTrack);
                originatingTrains.add(originatingTrain);
                terminatingTrains.add(terminatingTrain);
                haltingTrains.add(haltingTrain);
                platforms.add(platform);
                elevations.add(elevation);
                railwayZones.add(railwayZone);
                addresses.add(address);
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
            listType = new TypeToken<List<String>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStationTracks.db");
            gson.toJson(stationTracks,listType,fileWriter);
            fileWriter.close();
            listType = new TypeToken<List<String>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStationTypes.db");
            gson.toJson(stationTypes,listType,fileWriter);
            fileWriter.close();
            listType = new TypeToken<List<Integer>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStationOriginatingTrains.db");
            gson.toJson(originatingTrains,listType,fileWriter);
            fileWriter.close();
            listType = new TypeToken<List<Integer>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStationTerminatingTrains.db");
            gson.toJson(terminatingTrains,listType,fileWriter);
            fileWriter.close();
            listType = new TypeToken<List<Integer>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStationHaltingTrains.db");
            gson.toJson(haltingTrains,listType,fileWriter);
            fileWriter.close();
            listType = new TypeToken<List<Integer>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStationPlatforms.db");
            gson.toJson(platforms,listType,fileWriter);
            fileWriter.close();
            listType = new TypeToken<List<String>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStationElevations.db");
            gson.toJson(elevations,listType,fileWriter);
            fileWriter.close();
            listType = new TypeToken<List<String>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStationRailwayZones.db");
            gson.toJson(railwayZones,listType,fileWriter);
            fileWriter.close();
            listType = new TypeToken<List<String>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStationAddresses.db");
            gson.toJson(addresses,listType,fileWriter);
            fileWriter.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        DatabaseConnector databaseConnector = new DatabaseConnector();
        boolean ans = databaseConnector.insertIntoStationBatch(stationIndexes, stationIds, stationNames,
                stationTypes, stationTracks,originatingTrains,terminatingTrains,haltingTrains,platforms,
                elevations,railwayZones,addresses);
        if(!ans){
            System.out.println("Unable to put stations into database");
        }
        databaseConnector.closeConnection();
    }
}
