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
 * Helper class to fetch station details.
 */
public class FetchStationDetails {

    private Map<String, Integer> myMap;
    private String pathDatabaseStation;
    private Map<Integer, List<String>> stationDetails;

    public FetchStationDetails(String pathDatabaseStation) {
        requireNonNull(pathDatabaseStation, "path of database cant be null.");
        this.pathDatabaseStation = pathDatabaseStation;
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<Map<String, Integer>>() {
            }.getType();
            File file = new File(this.pathDatabaseStation + File.separator + "indexStation.db");
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
            File file = new File(this.pathDatabaseStation + File.separator + "indexStationDetails.db");
            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                this.stationDetails = gson.fromJson(fileReader, listType);
                fileReader.close();
            } else {
                this.stationDetails = new HashMap<>();
            }
        } catch (Exception e) {
            this.myMap = new HashMap<>();
        }
    }

    /**
     * @param stationId station id.
     * @return the number of platforms in the station.
     */
    public int getNumberOfPlatform(String stationId) {
        int indexStation = this.myMap.getOrDefault(stationId.toLowerCase(), -1);
        if (indexStation == -1) {
            return -1;
        }
        int numOfPlatform = -1;
        try {
            numOfPlatform = Integer.parseInt(this.stationDetails.get(indexStation).get(7));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numOfPlatform;
    }

    /**
     * @param stationId station id.
     * @return the number of tracks for the station.
     */
    public int getNumberOfTracks(String stationId) {
        int indexStation = this.myMap.getOrDefault(stationId.toLowerCase(), -1);
        if (indexStation == -1) {
            System.out.println("Station Not found. Please try using google search. Station : " + stationId);
            return 1;
        }
        int numOfTrack;
        String trackType;
        String trackType1;
        try {
            trackType = this.stationDetails.get(indexStation).get(3);
            trackType1 = trackType;
            trackType = trackType.split("\\s+")[0];
            if (trackType.equalsIgnoreCase("double")) {
                numOfTrack = 2;
            } else if (trackType.equalsIgnoreCase("quadruple")) {
                numOfTrack = 4;
            } else if (trackType.equalsIgnoreCase("triple")) {
                numOfTrack = 3;
            } else if (trackType.equalsIgnoreCase("single")) {
                numOfTrack = 1;
            } else if (trackType1.contains("Single-Line")) {
                numOfTrack = 1;
            } else if (trackType1.contains("Double-Line")) {
                numOfTrack = 2;
            } else if (trackType1.contains("Doubling")) {
                numOfTrack = 2;
            } else {
                System.out.println("Unknown track Type : " + trackType + " for station " + stationId);
                numOfTrack = 1;
                return numOfTrack;
            }
        } catch (Exception e) {
            e.printStackTrace();
            numOfTrack = 1;
        }

        return numOfTrack;
    }

    /**
     * Fetch all the station and store into indexStation.db.
     *
     * @return if successful.
     */
    public boolean fetchAll() {
        boolean ans = true;
        for (int i = 1; i < 15000; i++) {
            ans = fetchStation(i);
        }
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<Map<String, Integer>>() {
            }.getType();
            FileWriter fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStation.db");
            gson.toJson(myMap, listType, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return ans;
    }

    /**
     * Fetch a station info from indiarailinfo.com if not already fetched.
     *
     * @param stationIndexNo index number for station.
     * @return true if successful.
     */
    public boolean fetchStation(int stationIndexNo) {
        String url = "https://indiarailinfo.com/station/map/" + stationIndexNo;
        String pathStation = this.pathDatabaseStation + File.separator + "station_details_" + stationIndexNo + ".html";
        String pathStationDetailsComplete = this.pathDatabaseStation + File.separator + stationIndexNo + ".txt";

        if (new File(pathStationDetailsComplete).exists()) {
            System.out.println("Already exists details for Index : " + stationIndexNo + ". Skipping.");
            return true;
        }
        if (!(new File(pathStation).exists())) {
            if (!new GetWebsite().getWebsite(url, pathStation)) {
                System.out.println("Invalid Index : " + stationIndexNo);
                return false;
            }
        }
        if (!parseStationId(pathStation)) {
            System.out.println("Unable to parse station Id: " + pathStation);
            return false;
        }

        if (!parseStationDetails(pathStation, pathStationDetailsComplete)) {
            System.out.println("Unable to parse station details: " + pathStation);
            return false;
        }
        return true;
    }

    /**
     * Parse station id from file and store into map.
     *
     * @param fileName file name.
     * @return true if successful.
     */
    public boolean parseStationId(String fileName) {
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
                    if (temp.equalsIgnoreCase("content=\"https://indiarailinfo.com/station/map/-/0\">")) {
                        System.out.println("Unable to find station id: " + fileName);
                        bReader.close();
                        fReader.close();
                        return false;
                    }
                    String temp1[] = temp.split("/");
                    if (temp1.length >= 7) {
                        String stationName = temp1[5].toLowerCase();
                        String stationId = StationIdHelper.getStationIdFromName(stationName);
                        String stationIndex = temp1[6];
                        if (stationIndex.endsWith(">")) {
                            stationIndex = stationIndex.replace(">", "");
                        }
                        if (stationIndex.endsWith("\"")) {
                            stationIndex = stationIndex.replace("\"", "");
                        }
                        this.myMap.put(stationId, Integer.parseInt(stationIndex));
                        bReader.close();
                        fReader.close();
                        return true;
                    } else {
                        System.out.println("Unable to find station id: " + fileName);
                        bReader.close();
                        fReader.close();
                        return false;
                    }
                }
            }
            bReader.close();
            fReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Parse the station details.
     *
     * @param fileName        file path for station details.
     * @param pathStationFile path where parsed station details will be stored.
     * @return true if successful.
     */
    public boolean parseStationDetails(String fileName, String pathStationFile) {
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
                if (matcher1.find()) {
                    String temp = matcher1.group().split("\\s+")[2];
                    String temp1[] = temp.split("/");
                    if (temp1.length >= 7) {
                        String stationName = temp1[5].toLowerCase();
                        bWriter.write("Station Name: " + stationName);
                        bWriter.write('\n');
                    }
                } else if (matcher.find()) {
                    String temp[] = matcher.group().split("\\.");
                    for (int i = 1; i < temp.length - 3; i++) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Puts the parsed station info into indexStationDetails.db & indexStation.db.
     */
    public void putAllStationsInMap() {
        this.myMap = new HashMap<>();
        this.stationDetails = new HashMap<>();

        FileReader fReader;
        BufferedReader bReader;
        String line;
        String stationName, stationId, stationType, stationTrack, elevation, railwayZone;
        StringBuilder address;
        int originatingTrain, terminatingTrain, haltingTrain, platform;

        for (int i = 1; i < 15000; i++) {
            List<String> tempList = new ArrayList<>();
            System.out.println(i);
            stationName = "";
            stationId = "";
            stationType = "NA";
            stationTrack = "NA";
            elevation = "NA";
            railwayZone = "NA";
            address = new StringBuilder("");
            originatingTrain = 0;
            terminatingTrain = 0;
            haltingTrain = 0;
            platform = 0;
            try {
                String pathStation = this.pathDatabaseStation + File.separator + i + ".txt";
                if (!new File(pathStation).exists()) {
                    System.out.println("file not found " + pathStation);
                    continue;
                }
                fReader = new FileReader(pathStation);
                bReader = new BufferedReader(fReader);
                while ((line = bReader.readLine()) != null) {
                    if (line.contains("Station Name")) {
                        stationName = line.split(":")[1].trim();
                        stationId = StationIdHelper.getStationIdFromName(stationName);
                    } else if (line.contains("Type of Station")) {
                        stationType = line.split(":")[1].trim();
                    } else if (line.contains("Track")) {
                        stationTrack = line.split(":")[1].trim();
                    } else if (line.contains("Originating Trains")) {
                        try {
                            originatingTrain = Integer.parseInt(line.split(" ")[0]);
                        } catch (NumberFormatException e) {
                            originatingTrain = 0;
                        }
                    } else if (line.contains("Terminating Trains")) {
                        try {
                            terminatingTrain = Integer.parseInt(line.split(" ")[0]);
                        } catch (NumberFormatException e) {
                            terminatingTrain = 0;
                        }
                    } else if (line.contains("Halting Trains")) {
                        try {
                            haltingTrain = Integer.parseInt(line.split(" ")[0]);
                        } catch (NumberFormatException e) {
                            haltingTrain = 0;
                        }
                    } else if (line.contains("Platforms")) {
                        try {
                            platform = Integer.parseInt(line.split(" ")[0]);
                        } catch (NumberFormatException e) {
                            platform = 0;
                        }
                    } else if (line.contains("Elevation")) {
                        elevation = line.split(":")[1].trim();
                    } else if (line.contains("Railway Zone")) {
                        railwayZone = line.split(":")[1].trim().split("/")[0];
                    } else if (line.contains("Station Address")) {
                        address.append(line.split(":")[1].trim());
                    } else {
                        address.append(" ");
                        address.append(line.trim());
                    }
                }
                bReader.close();
                fReader.close();
            } catch (Exception e) {
                System.err.println("Error in executing index: " + i);
                e.printStackTrace();
            }

            if (!stationName.equals("") && !stationId.equals("")) {
                tempList.add(stationName);
                tempList.add(stationId);
                tempList.add(stationType);
                tempList.add(stationTrack);
                tempList.add(originatingTrain + "");
                tempList.add(terminatingTrain + "");
                tempList.add(haltingTrain + "");
                tempList.add(platform + "");
                tempList.add(elevation + "");
                tempList.add(railwayZone + "");
                tempList.add(address.toString());
                this.stationDetails.put(i, tempList);
                this.myMap.put(stationId, i);
            }
        }

        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<Map<Integer, List<String>>>() {
            }.getType();
            FileWriter fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStationDetails.db");
            gson.toJson(this.stationDetails, listType, fileWriter);
            fileWriter.close();
            listType = new TypeToken<Map<String, Integer>>() {
            }.getType();
            fileWriter = new FileWriter(this.pathDatabaseStation + File.separator + "indexStation.db");
            gson.toJson(this.myMap, listType, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert the station details into database.
     */
    public void putStationMapInDatabase() {
        DatabaseHelper databaseHelper = new DatabaseHelper();
        boolean ans = databaseHelper.insertIntoStationBatch(this.stationDetails);
        if (!ans) {
            System.out.println("Unable to put stations into database");
        }
        databaseHelper.closeConnection();
    }

}
