package iitp.naman.newtrainschedulingalgorithm.datahelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import iitp.naman.newtrainschedulingalgorithm.util.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Helper class to fetch train details.
 */
public class FetchTrainDetails {
    private Map<Integer, Integer> myMap;
    private String pathDatabaseTrain;
    private Map<Integer, List<String>> trainDetails;

    public FetchTrainDetails(String pathDatabaseTrain) {
        requireNonNull(pathDatabaseTrain, "path of database cant be null.");
        this.pathDatabaseTrain = pathDatabaseTrain;
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<Map<Integer, Integer>>() {
            }.getType();
            File file = new File(this.pathDatabaseTrain + File.separator + "indexTrains.db");
            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                this.myMap = gson.fromJson(fileReader, listType);
                fileReader.close();
            } else {
                this.myMap = new HashMap<>();
            }
        } catch (Exception e) {
            this.myMap = new HashMap<>();
        }

        try {
            Type listType = new TypeToken<Map<Integer, List<String>>>() {
            }.getType();
            File file = new File(this.pathDatabaseTrain + File.separator + "indexTrainDetails.db");
            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                this.trainDetails = gson.fromJson(fileReader, listType);
                fileReader.close();
            } else {
                this.trainDetails = new HashMap<>();
            }
        } catch (Exception e) {
            this.trainDetails = new HashMap<>();
        }
    }

    /**
     * Stores the parsed info of train into the new location.
     *
     * @param trainNo       train number.
     * @param pathTrainFile path where train details has to be copied.
     * @return true if successful.
     */
    public boolean fetchTrainNumber(int trainNo, String pathTrainFile) {
        int indexTrain = this.myMap.getOrDefault(trainNo, -1);
        if (indexTrain == -1) {
            System.out.println("Train Not found. Please try using google search. trainNo: " + trainNo);
            return false;
        }
        String fileTrainIndex = this.pathDatabaseTrain + File.separator + indexTrain + ".txt";
        try {
            FileReader fReader;
            BufferedReader bReader;
            FileWriter fWriter;
            BufferedWriter bWriter;
            fReader = new FileReader(fileTrainIndex);
            bReader = new BufferedReader(fReader);
            fWriter = new FileWriter(pathTrainFile);
            bWriter = new BufferedWriter(fWriter);
            String line;
            while ((line = bReader.readLine()) != null) {
                bWriter.write(line);
                bWriter.write('\n');
            }
            bReader.close();
            fReader.close();
            bWriter.close();
            fWriter.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetch all the train and store into indexTrains.db.
     *
     * @return true if successful.
     */
    public boolean fetchAll() {
        boolean ans = true;
        for (int i = 1; i <= 99999; i++) {
            ans = fetchTrain(i);
        }
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<Map<Integer, Integer>>() {
            }.getType();
            FileWriter fileWriter = new FileWriter(this.pathDatabaseTrain + File.separator + "indexTrains.db");
            gson.toJson(myMap, listType, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return ans;
    }

    /**
     * Fetch a train info from indiarailinfo.com if not already fetched.
     *
     * @param trainIndexNo index number for train.
     * @return true if successful.
     */
    public boolean fetchTrain(int trainIndexNo) {
        String url = "https://indiarailinfo.com/train/timetable/all/" + trainIndexNo;
        String pathTrain = this.pathDatabaseTrain + File.separator + "train_details_" + trainIndexNo + ".html";
        String pathTrainScheduleComplete = this.pathDatabaseTrain + File.separator + trainIndexNo + ".txt";

        if (new File(pathTrainScheduleComplete).exists()) {
            System.out.println("Already exists details for Index : " + trainIndexNo + ". Skipping.");
            return true;
        }

        if (!(new File(pathTrain).exists())) {
            if (!new GetWebsite().getWebsite(url, pathTrain)) {
                System.out.println("Invalid Index : " + trainIndexNo);
                return false;
            }
        }
        if (!parseTrainNumber(pathTrain, trainIndexNo)) {
            System.out.println("Unable to parse train Number " + pathTrain);
            return false;
        }
        if (!parseTrainScheduleWebsite(pathTrain, pathTrainScheduleComplete)) {
            System.out.println("Unable to parse train schedule " + pathTrain);
            return false;
        }
        return true;
    }

    /**
     * Parse train number from file and store into map.
     *
     * @param fileName     file name.
     * @param trainIndexNo index number for train.
     * @return true if successful.
     */
    public boolean parseTrainNumber(String fileName, int trainIndexNo) {
        Pattern pattern = Pattern.compile("<meta property=\"og:url\" content=\".*?\">");
        Matcher matcher;
        FileReader fReader;
        BufferedReader bReader;
        try {
            fReader = new FileReader(fileName);
            bReader = new BufferedReader(fReader);
            String line;
            int trainNoInt = -1;
            boolean checkValidityOnly = false;
            while ((line = bReader.readLine()) != null) {
                if (line.contains("IMAGINARY Train. NOT an ACTUAL running Train.")) {
                    System.out.println("Imaginary Train: " + fileName);
                    return false;
                } else if (line.contains("TRAIN IS CANCELLED")) {
                    System.out.println("Cancelled Train: " + fileName);
                    return false;
                }
                if (checkValidityOnly) {
                    continue;
                }
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String temp = matcher.group().split("\\s+")[2];
                    String temp1[] = temp.split("/");
                    if (temp1.length >= 7) {
                        String trainName = temp1[5].toLowerCase();
                        String trainNo = TrainNumberHelper.getTrainNoFromName(trainName);
                        String trainIndex = temp1[6];
                        if (trainIndex.endsWith(">")) {
                            trainIndex = trainIndex.replace(">", "");
                        }
                        if (trainIndex.endsWith("\"")) {
                            trainIndex = trainIndex.replace("\"", "");
                        }
                        try {
                            trainNoInt = Integer.parseInt(trainNo);
                        } catch (Exception e) {
                            System.out.println("\tInvalid train Number: " + trainNo);
                        }
                        int trainIndexInt = -1;
                        try {
                            trainIndexInt = Integer.parseInt(trainIndex);
                        } catch (Exception e) {
                            System.out.println("\tInvalid train Index: " + trainIndex);
                        }
                        if (trainIndexInt != trainIndexNo) {
                            System.out.println("Invalid train to parse as index num does not match");
                            return false;
                        }
                        if (trainIndexInt > 0 && trainNoInt > 0) {
                            checkValidityOnly = true;
                        }
                    } else {
                        System.out.println("Unable to find train No: " + fileName);
                    }
                }
            }
            bReader.close();
            fReader.close();
            if (trainNoInt > 0) {
                this.myMap.put(trainNoInt, trainIndexNo);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Parse the train details.
     *
     * @param filename      file path for train details.
     * @param pathTrainFile path where parsed train details will be stored.
     * @return true if successful.
     */
    private boolean parseTrainScheduleWebsite(String filename, String pathTrainFile) {
        System.out.println("Parsing " + filename);

        BufferedWriter bWriter;
        FileWriter fWriter;
        FileReader fReader;
        BufferedReader bReader;

        Pattern pattern_station_name = Pattern.compile("href=\"/station/map/.*");
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
            String arrival = "";
            String departure = "";
            String km = "";
            String line;
            int stoppage_no = -1;
            while ((line = bReader.readLine()) != null) {
                if (stoppage_no < 0 && !line.contains("Trk")) {
                    continue;
                } else if (stoppage_no < 0) {
                    stoppage_no = 0;
                }
                if (!station_started && !line.contains("href=\"/station/map/")) {
                    continue;
                }
                if (!station_started) {
                    matcher = pattern_station_name.matcher(line);
                    station_started = matcher.find();
                    continue;
                } else if (!second_line_station) {
                    matcher = pattern_station_name.matcher(line);
                    if (matcher.find()) {
                        second_line_station = true;
                        String temp = matcher.group();
                        station_name = temp.split("/")[3];
                    } else {
                        station_started = false;
                        second_line_station = false;
                    }
                    continue;
                } else {
                    if (stoppage_no <= 0) {
                        arrival = "start";
                    }
                    matcher = pattern_time.matcher(line);
                    if (matcher.find()) {
                        String temp_abc = matcher.group();
                        if (arrival.equals("")) {
                            arrival = temp_abc.substring(1, temp_abc.length() - 1);
                        } else {
                            departure = temp_abc.substring(1, temp_abc.length() - 1);
                        }
                    } else {
                        matcher = pattern_km.matcher(line);
                        if (matcher.find()) {
                            String temp_abc = matcher.group();
                            km = temp_abc.substring(1, temp_abc.length() - 1);
                        }
                    }
                }

                if (!station_name.equals("") && !((arrival.equals("") || arrival.equals("start")) && departure.equals(""))
                        && !km.equals("")) {
                    if (departure.equals("")) {
                        if (!arrival.equals("") && !arrival.equals("start")) {
                            String temp_arrival[] = arrival.split(":");
                            int hour = Integer.parseInt(temp_arrival[0]);
                            int minutes = Integer.parseInt(temp_arrival[1]);
                            minutes = minutes + 20;
                            if (minutes > 60) {
                                minutes = minutes - 60;
                                hour++;
                                if (hour > 23) {
                                    hour = 0;
                                }
                            }
                            String hour1 = hour + "";
                            if (hour1.length() == 1) {
                                hour1 = "0" + hour1;
                            }
                            String minutes1 = minutes + "";
                            if (minutes1.length() == 1) {
                                minutes1 = "0" + minutes1;
                            }
                            departure = hour1 + ":" + minutes1;
                        }
                    } else {
                        if (arrival.equalsIgnoreCase("start")) {
                            String temp_departure[] = departure.split(":");
                            int hour = Integer.parseInt(temp_departure[0]);
                            int minutes = Integer.parseInt(temp_departure[1]);
                            minutes = minutes - 20;
                            if (minutes < 0) {
                                minutes = minutes + 60;
                                hour--;
                                if (hour < 0) {
                                    hour = 23;
                                }
                            }
                            String hour1 = hour + "";
                            if (hour1.length() == 1) {
                                hour1 = "0" + hour1;
                            }
                            String minutes1 = minutes + "";
                            if (minutes1.length() == 1) {
                                minutes1 = "0" + minutes1;
                            }
                            arrival = hour1 + ":" + minutes1;
                        }

                    }
                    bWriter.write(station_name + "\t" + arrival + "\t" + departure + "\t" + km + "\n");

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
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param filename      file path for train with list of train indexes.
     * @param pathTrainBase file path where the train schedule has to be stored.
     */
    public void getTrainStoppageFromFile(String filename, String pathTrainBase) {
        FileReader fReader;
        BufferedReader bReader;
        try {
            fReader = new FileReader(filename);
            bReader = new BufferedReader(fReader);
            String line;

            while ((line = bReader.readLine()) != null) {
                String[] data = line.split("\\s+");
                String day = "day" + data[0];
                String pathTrainScheduleParent = pathTrainBase + File.separator + day;
                if (!FolderHelper.createFolder(pathTrainScheduleParent)) {
                    System.out.println("Unable to create folder");
                    bReader.close();
                    fReader.close();
                    return;
                }
                for (int i = 1; i < data.length; i++) {
                    String pathTrainScheduleAll = pathTrainScheduleParent + File.separator + data[i] + ".txt";
                    if (!fetchTrainNumber(Integer.parseInt(data[i]), pathTrainScheduleAll)) {
                        System.out.print("Unable to fetch train.");
                    }
                }
            }
            fReader.close();
            bReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Puts the parsed train info into indexTrainDetails.db & indexTrains.db.
     */
    public void putAllTrainsInMap() {
        this.trainDetails = new HashMap<>();
        this.myMap = new HashMap<>();

        FileReader fReader;
        BufferedReader bReader;
        String line;
        String trainName;
        StringBuilder travelDay;
        String trainType;
        Pattern pattern = Pattern.compile("<meta property=\"og:url\" content=\".*?\">");
        Matcher matcher;
        int trainNo;

        for (int i = 1; i <= 99999; i++) {
            List<String> tempList = new ArrayList<>();
            System.out.println(i);
            trainName = "";
            trainNo = -1;
            travelDay = new StringBuilder("");
            trainType = "";
            try {
                String pathTrain = this.pathDatabaseTrain + File.separator + "train_details_" + i + ".html";
                if (!new File(pathTrain).exists()) {
                    System.out.println("file not found " + pathTrain);
                    continue;
                }
                if (!parseTrainNumber(pathTrain, i)) {
                    System.out.println("Unable to parse train Number " + pathTrain);
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
                            trainNo = Integer.parseInt(TrainNumberHelper.getTrainNoFromName(trainName));
                            if (!this.myMap.containsKey(trainNo)) {
                                System.out.println("Not found : " + trainNo);
                            }
                        }
                    } else if (line.trim().equalsIgnoreCase("Type:")) {
                        String type = bReader.readLine();
                        type = type.replaceFirst(".*?>", "");
                        type = type.replaceFirst("<.*", "");
                        trainType = type;
                    } else if (line.contains("class=\"deparrgrid\">")) {
                        bReader.readLine();
                        bReader.readLine();
                        for (int j = 0; j < 7; j++) {
                            line = bReader.readLine();
                            line = line.replaceFirst(".*?>", "");
                            line = line.replaceFirst("<.*", "");
                            if (line.length() == 1) {
                                travelDay.append(1);
                            } else {
                                travelDay.append(0);
                            }
                        }
                        break;
                    }
                }
                bReader.close();
                fReader.close();
            } catch (NumberFormatException e) {
                System.err.println("Number Format Exception : " + i);
                continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (trainNo != -1 && !trainName.equals("") && !travelDay.toString().equals("")) {
                tempList.add(trainNo + "");
                tempList.add(trainName);
                tempList.add(travelDay.toString());
                tempList.add(trainType);
                this.trainDetails.put(i, tempList);
            }
        }

        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<Map<Integer, List<String>>>() {
            }.getType();
            FileWriter fileWriter = new FileWriter(this.pathDatabaseTrain + File.separator + "indexTrainDetails.db");
            gson.toJson(this.trainDetails, listType, fileWriter);
            fileWriter.close();

            listType = new TypeToken<Map<Integer, Integer>>() {
            }.getType();
            fileWriter = new FileWriter(this.pathDatabaseTrain + File.separator + "indexTrains.db");
            gson.toJson(this.myMap, listType, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert the train details into database.
     */
    public void putTrainsMapInDatabase() {
        DatabaseHelper databaseHelper = new DatabaseHelper();
        boolean ans = databaseHelper.insertIntoTrainBatch(this.trainDetails);
        if (!ans) {
            System.out.println("Unable to put trains into database");
        }
        databaseHelper.closeConnection();
    }

    /**
     * Insert the stoppage details into database.
     */
    public void putAllStoppagesInDatabase() {
        Map<Integer, Integer> myMapReverse = new HashMap<>();
        for (int trainNo : this.myMap.keySet()) {
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
        DatabaseHelper databaseHelper = new DatabaseHelper();

        for (int i = 1; i <= 99999; i++) {
            int trainNo = myMapReverse.getOrDefault(i, -1);
            if (trainNo == -1) {
                System.out.println("No train for this index : " + i);
                continue;
            }
            System.out.println("index :" + i + " trainNo : " + trainNo);
            List<Integer> trainNos = new ArrayList<>();
            List<String> stationIds = new ArrayList<>();
            List<TrainTime> arrivals = new ArrayList<>();
            List<TrainTime> departures = new ArrayList<>();
            List<Double> distances = new ArrayList<>();

            String pathTrain = this.pathDatabaseTrain + File.separator + i + ".txt";

            try {
                fReader = new FileReader(pathTrain);
                bReader = new BufferedReader(fReader);
                while ((line = bReader.readLine()) != null) {
                    data = line.split("\\s+");
                    stationId = StationIdHelper.getStationIdFromName(data[0]);
                    arrival = new TrainTime("0:" + data[1]);
                    departure = new TrainTime("0:" + data[2]);
                    distance = Double.parseDouble(data[3]);
                    stationIds.add(stationId);
                    arrivals.add(arrival);
                    departures.add(departure);
                    distances.add(distance);
                    trainNos.add(trainNo);
                }
                bReader.close();
                fReader.close();
                boolean ans = databaseHelper.insertIntoStoppageBatch(trainNos, stationIds, arrivals,
                        departures, distances);
                if (!ans) {
                    System.out.println("Unable to put trains stoppage into database : " + trainNo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        databaseHelper.closeConnection();
    }
}
