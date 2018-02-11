import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class FetchTrainDetails {
    private Map<Integer, Integer> myMap;
    private String pathDatabaseTrain;
    private boolean newMethod;

    public FetchTrainDetails(String pathDatabaseTrain){
        requireNonNull(pathDatabaseTrain, "path of database cant be null.");
        this.pathDatabaseTrain = pathDatabaseTrain;
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<Map<Integer, Integer>>(){}.getType();
            File file = new File(this.pathDatabaseTrain + File.separator + "indexTrains.db");
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
        this.newMethod=true;
    }

    public int getTrainIndexNo(int trainNo){
        return this.myMap.getOrDefault(trainNo, -1);
    }

    public boolean fetchTrainNumber(int trainNo, String pathTrainFile){
        int indexTrain = this.myMap.getOrDefault(trainNo, -1);
        if(indexTrain==-1){
            System.out.println("Train Not found. Please try using google search. trainNo: " + trainNo);
            return false;
        }
        String fileTrainIndex = this.pathDatabaseTrain + File.separator +indexTrain+".txt";
        try{
            FileReader fReader;
            BufferedReader bReader;
            FileWriter fWriter;
            BufferedWriter bWriter;
            fReader = new FileReader(fileTrainIndex);
            bReader = new BufferedReader(fReader);
            fWriter = new FileWriter(pathTrainFile);
            bWriter = new BufferedWriter(fWriter);
            String line;
            while((line = bReader.readLine()) != null){
                bWriter.write(line);
                bWriter.write('\n');
            }
            bReader.close();
            fReader.close();
            bWriter.close();
            fWriter.close();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean fetchAll(){
        boolean ans = true;
        for(int i=1;i<15000;i++){
            ans = fetchTrain(i);
        }
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<Map<Integer, Integer>>(){}.getType();
            FileWriter fileWriter = new FileWriter(this.pathDatabaseTrain + File.separator + "indexTrains.db");
            gson.toJson(myMap,listType,fileWriter);
            fileWriter.close();
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return ans;
    }

    public boolean fetchTrain(int trainIndexNo){
        String url = "https://indiarailinfo.com/train/timetable/all/" + trainIndexNo;
        String pathTrain = this.pathDatabaseTrain + File.separator + "train_details_" + trainIndexNo+".txt";
        File file = new File(pathTrain);
        if(!file.exists()) {
            if(!new GetWebsite().getWebsite(url, pathTrain)){
                System.out.println("Invalid Index : " + trainIndexNo);
                return false;
            }
        }
        if(!parseTrainNumber(pathTrain)){
            System.out.println("Unable to parse train Number " + pathTrain);
            return false;
        }
        String pathTrainScheduleComplete = this.pathDatabaseTrain + File.separator +trainIndexNo+".txt";
        if(!parseTrainScheduleWebsite(pathTrain, pathTrainScheduleComplete)){
            System.out.println("Unable to parse train schedule " + pathTrain);
            return false;
        }
        return true;
    }

    public boolean parseTrainNumber(String fileName){
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
                if (matcher.find()) {
                    String temp = matcher.group().split("\\s+")[2];
                    String temp1[] = temp.split("/");
                    if (temp1.length >= 7) {
                        String trainName = temp1[5].toLowerCase();
                        String trainNo = trainName.trim().replaceAll(".*-", "");
                        String trainIndex = temp1[6];
                        if(trainIndex.endsWith(">")){
                            trainIndex = trainIndex.replace(">", "");
                        }
                        if(trainIndex.endsWith("\"")){
                            trainIndex = trainIndex.replace("\"", "");
                        }
                        this.myMap.put(Integer.parseInt(trainNo), Integer.parseInt(trainIndex));
                        // System.out.print(temp + " ");
                        // System.out.println(trainNo+">" + trainIndex);
                        bReader.close();
                        fReader.close();
                        return true;
                    }
                    else{
                        System.out.println("Unable to find train No: " + fileName);
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

    private boolean parseTrainScheduleWebsite(String filename, String pathTrainFile) {
        System.out.println("Parsing " + filename);
        Set<String> stationIds = new HashSet<>();

        BufferedWriter bWriter;
        FileWriter fWriter;
        FileReader fReader;
        BufferedReader bReader;

        Pattern pattern = Pattern.compile("href=\"/departures/.*");
        Pattern pattern_time = Pattern.compile(">\\d+:\\d+<");
        Pattern pattern_km = Pattern.compile(">\\d+[.]\\d+<");
        Matcher matcher;
        try {
            fReader = new FileReader(filename);
            bReader = new BufferedReader(fReader);
            fWriter = new FileWriter(pathTrainFile);
            bWriter = new BufferedWriter(fWriter);
            boolean station_started = false;
            boolean second_line_station = false;
            String station_name = "";
            String arrival="";
            String departure="";
            String km = "";
            String line;
            int stoppage_no = 0;
            while((line = bReader.readLine()) != null) {
                if(!line.contains("href=\"/departures/") && !station_started) {
                    continue;
                }
                if(!station_started) {
                    matcher = pattern.matcher(line);
                    station_started = matcher.find();
                    continue;
                }
                else if(!second_line_station){
                    matcher = pattern.matcher(line);
                    if(matcher.find()) {
                        second_line_station = true;
                        String temp = matcher.group();
                        station_name = temp.split("/")[2];
                    }
                    else {
                        station_started = false;
                        second_line_station = false;
                    }
                    continue;
                }
                else {
                    if(stoppage_no==0) {
                        arrival = "start";
                    }
                    matcher = pattern_time.matcher(line);
                    if(matcher.find()) {
                        String temp_abc = matcher.group();
                        if(arrival.equals("")) {
                            arrival = temp_abc.substring(1,temp_abc.length()-1);
                        }
                        else {
                            departure = temp_abc.substring(1,temp_abc.length()-1);
                        }
                    }
                    else {
                        matcher = pattern_km.matcher(line);
                        if(matcher.find()) {
                            String temp_abc = matcher.group();
                            km = temp_abc.substring(1,temp_abc.length()-1);
                        }
                    }
                }

                if(!station_name.equals("") && !((arrival.equals("")||arrival.equals("start")) && departure.equals(""))
                        &&!km.equals("")) {
                    if(departure.equals("")) {
                        if(!arrival.equals("") && !arrival.equals("start")) {
                            String temp_arrival[] = arrival.split(":");
                            int hour = Integer.parseInt(temp_arrival[0]);
                            int minutes  = Integer.parseInt(temp_arrival[1]);
                            minutes = minutes + 20;
                            if(minutes>60) {
                                minutes = minutes - 60;
                                hour++;
                                if(hour>23) {
                                    hour = 0;
                                }
                            }
                            String hour1 = hour + "";
                            if(hour1.length()==1) {
                                hour1 = "0" +hour1;
                            }
                            String minutes1 = minutes + "";
                            if(minutes1.length()==1) {
                                minutes1 = "0" +minutes1;
                            }
                            departure = hour1 + ":" + minutes1;
                        }
                    }
                    else {
                        if(arrival.equalsIgnoreCase("start")) {
                            String temp_departure[] = departure.split(":");
                            int hour = Integer.parseInt(temp_departure[0]);
                            int minutes  = Integer.parseInt(temp_departure[1]);
                            minutes = minutes - 20;
                            if(minutes<0) {
                                minutes = minutes + 60;
                                hour--;
                                if(hour<0) {
                                    hour = 23;
                                }
                            }
                            String hour1 = hour + "";
                            if(hour1.length()==1) {
                                hour1 = "0" +hour1;
                            }
                            String minutes1 = minutes + "";
                            if(minutes1.length()==1) {
                                minutes1 = "0" +minutes1;
                            }
                            arrival = hour1 + ":" + minutes1;
                        }

                    }
                    if(stationIds.add(station_name.trim().replaceAll(".*-", "").toLowerCase())){
                        bWriter.write(station_name + "\t" +arrival + "\t" + departure + "\t" + km + "\n");
                    }
                    else{
                        System.out.println("Duplicate station id found. Skipping : "+ station_name);
                    }
                    //System.out.println(station_name + "\t" +arrival + "\t" + departure + "\t" + km + "\n");

                    station_name = "";
                    arrival = "";
                    departure = "";
                    km = "";
                    station_started = false;
                    second_line_station = false;
                    stoppage_no++;
                }
            }
            bWriter.close();
            fWriter.close();
            bReader.close();
            fReader.close();
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean getTrainStoppageAll(String trainNo, String pathTemp, String pathTrainSchedule){
        FileReader fReader;
        BufferedReader bReader;
        FileReader fReader2;
        BufferedReader bReader2;
        Boolean parsable=false;
        Pattern pattern = Pattern.compile("<a href=\"/url\\?q=.*?</a>");
        Pattern patternIndiaRail = Pattern.compile("https://indiarailinfo.com/train/(timetable/)?.*?/\\d+");
        Pattern patternIndiaRailAll = Pattern.compile("<a href=\".*?\">Show ALL intermediate Stations</a>");
        Matcher matcher;
        Matcher matcher2;
        Matcher matcher3;
        String searchUrl = "https://www.google.com/search?q="+trainNo+"%20indiarailinfo%20timetable";
        String pathTrainGoogleSearchPage = pathTemp+File.separator+trainNo+"_google_search.txt";
        String pathTrainWebsite = pathTemp + File.separator+ trainNo + "_website.txt";
        String pathTrainScheduleWebsite = pathTemp + File.separator+ trainNo + "_schedule_website.txt";
        File file = new File(pathTrainGoogleSearchPage);
        if(!file.exists()) {
            new GetWebsite().getWebsite(searchUrl, pathTrainGoogleSearchPage);
        }
        try {
            fReader = new FileReader(pathTrainGoogleSearchPage);
            bReader = new BufferedReader(fReader);
            String temp_line;
            while ((temp_line = bReader.readLine()) != null) {
                matcher = pattern.matcher(temp_line);
                if (matcher.find()) {
                    matcher2 = patternIndiaRail.matcher(matcher.group());
                    if (matcher2.find()) {
                        file = new File(pathTrainWebsite);
                        if (!file.exists()) {
                            new GetWebsite().getWebsite(matcher2.group(), pathTrainWebsite);
                        }
                        fReader2 = new FileReader(pathTrainWebsite);
                        bReader2 = new BufferedReader(fReader2);
                        String line_all_stations;
                        while ((line_all_stations = bReader2.readLine()) != null) {
                            matcher3 = patternIndiaRailAll.matcher(line_all_stations);
                            if (matcher3.find()) {
                                String all_station = matcher3.group();
                                String link = "https://indiarailinfo.com" + all_station.split("\"")[1];
                                file = new File(pathTrainScheduleWebsite);
                                if (!file.exists()) {
                                    new GetWebsite().getWebsite(link, pathTrainScheduleWebsite);
                                }
                                parsable = true;
                                break;
                            }
                        }
                        fReader2.close();
                        bReader2.close();
                        break;
                    }
                }
            }
            fReader.close();
            bReader.close();

            if(parsable && parseTrainScheduleWebsite(pathTrainScheduleWebsite, pathTrainSchedule)) {
                return true;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public void getTrainStoppageFromFile(String filename, String pathTemp, String pathTrainBase) {
        FileReader fReader;
        BufferedReader bReader;
        try {
            fReader = new FileReader(filename);
            bReader = new BufferedReader(fReader);
            String line;

            while((line = bReader.readLine())!=null) {
                String[] data = line.split("\\s+");
                String day = "day" +data[0];
                String pathTrainScheduleParent = pathTrainBase + File.separator + day;
                if (!Scheduler.createFolder(pathTrainScheduleParent)) {
                    System.out.println("Unable to create folder");
                    bReader.close();
                    fReader.close();
                    return;
                }
                for(int i=1;i<data.length;i++) {
                    String pathTrainScheduleAll = pathTrainScheduleParent + File.separator + data[i] + ".txt";
                    if(this.newMethod){
                        if(!fetchTrainNumber(Integer.parseInt(data[i]),pathTrainScheduleAll)){
                            System.out.print("Unable to fetch train.");
                        }
                    }
                    else {
                        if (!getTrainStoppageAll(data[i], pathTemp, pathTrainScheduleAll)) {
                            System.out.print("Unable to fetch train.");
                        }
                    }
                }
            }
            fReader.close();
            bReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void putAllTrainsInDatabase(){

        List<Integer> trainNos = new ArrayList<>();
        List<String> trainNames = new ArrayList<>();
        List<String> travelDays = new ArrayList<>();
        List<Integer> trainIndexes = new ArrayList<>();

        FileReader fReader;
        BufferedReader bReader;
        String line;
        String trainName;
        StringBuilder travelDay;
        Pattern pattern = Pattern.compile("<meta property=\"og:url\" content=\".*?\">");
        Matcher matcher;
        int trainNo;

        for(int i=1;i<15000;i++){
            System.out.println(i);
            trainName = "";
            trainNo = -1;
            travelDay = new StringBuilder("");
            try {
                String pathTrain = this.pathDatabaseTrain + File.separator + "train_details_"+i + ".txt";
                if(!new File(pathTrain).exists()){
                    System.out.println("file not found " + pathTrain);
                    continue;
                }
                fReader = new FileReader(pathTrain);
                bReader = new BufferedReader(fReader);
                while ((line = bReader.readLine()) != null) {
                    matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String temp = matcher.group().split("\\s+")[2];
                        String temp1[] = temp.split("/");
                        if (temp1.length >= 7) {
                            trainName = temp1[5].toLowerCase();
                            trainNo = Integer.parseInt(trainName.trim().replaceAll(".*-", ""));
                        }
                    }
                    else if(line.contains("class=\"deparrgrid\">")){
                        bReader.readLine();
                        bReader.readLine();
                        for(int j=0;j<7;j++){
                            line = bReader.readLine();
                            line = line.replaceFirst(".*?>", "");
                            line = line.replaceFirst("<.*","");
                            if(line.length()==1){
                                travelDay.append(1);
                            }
                            else{
                                travelDay.append(0);
                            }
                        }
                        break;
                    }
                }
                bReader.close();
                fReader.close();
            }
            catch (NumberFormatException e){
                System.err.println("Number Format Exception : " + i);
                continue;
            }
            catch (Exception e){
                e.printStackTrace();
            }
            if(trainNo!=-1 && !trainName.equals("") && !travelDay.toString().equals("")){
                trainNos.add(trainNo);
                trainNames.add(trainName);
                travelDays.add(travelDay.toString());
                trainIndexes.add(i);
            }
        }
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<List<Integer>>(){}.getType();
            FileWriter fileWriter = new FileWriter(this.pathDatabaseTrain + File.separator + "indexTrainNos.db");
            gson.toJson(trainNos,listType,fileWriter);
            fileWriter.close();
            listType = new TypeToken<List<String>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseTrain + File.separator + "indexTrainNames.db");
            gson.toJson(trainNames,listType,fileWriter);
            fileWriter.close();
            listType = new TypeToken<List<String>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseTrain + File.separator + "indexTrainTravelDays.db");
            gson.toJson(travelDays,listType,fileWriter);
            fileWriter.close();
            listType = new TypeToken<List<Integer>>(){}.getType();
            fileWriter = new FileWriter(this.pathDatabaseTrain + File.separator + "indexTrainIndexes.db");
            gson.toJson(trainIndexes,listType,fileWriter);
            fileWriter.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        DatabaseConnector databaseConnector = new DatabaseConnector();
        boolean ans = databaseConnector.insertIntoTrainBatch(trainIndexes, trainNos, trainNames, travelDays);
        if(!ans){
            System.out.println("Unable to put trains into database");
        }
        databaseConnector.closeConnection();
    }

    public void putAllStoppagesInDatabase(){
        Map<Integer, Integer> myMapReverse = new HashMap<>();

        for(int trainNo: this.myMap.keySet()){
            myMapReverse.put(this.myMap.get(trainNo), trainNo);
        }

        FileReader fReader;
        BufferedReader bReader;
        String line;
        String[] data;
        String stationId;
        TrainTime arrival;
        TrainTime departure;
        double distance;
        DatabaseConnector databaseConnector = new DatabaseConnector();

        for(int i=1;i<15000;i++){
            int trainNo = myMapReverse.getOrDefault(i,-1);
            if(trainNo==-1){
                System.out.println("No train for this index : " + i);
                continue;
            }
            System.out.println("index :" + i + " trainNo : " + trainNo);
            List<Integer> trainNos = new ArrayList<>();
            List<String> stationIds = new ArrayList<>();
            List<TrainTime> arrivals = new ArrayList<>();
            List<TrainTime> departures = new ArrayList<>();
            List<Double> distances = new ArrayList<>();

            String pathTrain = this.pathDatabaseTrain + File.separator +i + ".txt";

            try {
                fReader = new FileReader(pathTrain);
                bReader = new BufferedReader(fReader);
                while((line = bReader.readLine()) != null) {
                    data = line.split("\\s+");
                    stationId = data[0].trim().replaceAll(".*-", "");
                    arrival = new TrainTime("0:"+data[1]);
                    departure = new TrainTime("0:"+data[2]);
                    distance = Double.parseDouble(data[3]);
                    stationIds.add(stationId);
                    arrivals.add(arrival);
                    departures.add(departure);
                    distances.add(distance);
                    trainNos.add(trainNo);
                }
                bReader.close();
                fReader.close();
                boolean ans = databaseConnector.insertIntoStoppageBatch(trainNos, stationIds, arrivals,
                        departures, distances);
                if(!ans){
                    System.out.println("Unable to put trains stoppage into database : " + trainNo);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        databaseConnector.closeConnection();
    }
}
