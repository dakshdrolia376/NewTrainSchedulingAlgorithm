package iitp.naman.newtrainschedulingalgorithm.datahelper;

import iitp.naman.newtrainschedulingalgorithm.util.StationIdHelper;
import iitp.naman.newtrainschedulingalgorithm.util.TrainTime;
import iitp.naman.newtrainschedulingalgorithm.util.WriteToFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.*;

public class RouteHelper {

    /**
     * Updates the rote files with latest information.
     *
     * @param pathTrainTypeFile    path for file containing train type info.
     * @param pathRouteFile        path for file containing route info.
     * @param pathRouteTimeMinFile path for file containing old trains minimum time taken info.
     * @param pathRouteTimeAvgFile path for file containing old trains avg time taken info.
     * @param pathRouteTimeMedFile path for file containing old trains median time taken info.
     * @param pathStationDatabase  path containing station data.
     */
    public static void updateRouteFile(String pathTrainTypeFile, String pathRouteFile, String pathRouteTimeMinFile,
                                String pathRouteTimeAvgFile, String pathRouteTimeMedFile, String pathStationDatabase) {
        FetchStationDetails fetchStationDetails = new FetchStationDetails(pathStationDatabase);
        int lengthMaxName = 18;
        StringBuilder newRouteData = new StringBuilder();
        StringBuilder routeTimeAvgData = new StringBuilder();
        StringBuilder routeTimeMinData = new StringBuilder();
        StringBuilder routeTimeMedData = new StringBuilder();
        DatabaseHelper databaseHelper = new DatabaseHelper();
        List<List<String>> trainTypesAndNumbers = new ArrayList<>();
        try {
            FileReader fReader = new FileReader(pathTrainTypeFile);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            while ((line = bReader.readLine()) != null) {
                String[] tempTrainNoArray = line.split("\\s+");
                if (tempTrainNoArray.length == 0) {
                    continue;
                }
                List<String> tempTrainNo = new ArrayList<>(Arrays.asList(tempTrainNoArray));
                trainTypesAndNumbers.add(tempTrainNo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        double distanceToSubtract = -1;
        double prevDistance;
        double currDistance = 0;
        try {
            FileReader fReader = new FileReader(pathRouteFile);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            Map<String, String> departure1;
            Map<String, String> arrival2;
            Map<String, String> departure2 = null;

            while ((line = bReader.readLine()) != null) {
                String data[] = line.split("\\s+");
                if (data.length < 2) {
                    System.out.println("Skipping station as incomplete data :" + line);
                    continue;
                }
                String id = StationIdHelper.getStationIdFromName(data[0]);
                int numOfPlatform = fetchStationDetails.getNumberOfPlatform(id);
                if (numOfPlatform <= 0) {
                    System.out.println("Unable to find Num of platforms in station : " + id + ". Skipping it.");
                    continue;
                }
                if (distanceToSubtract == -1) {
                    distanceToSubtract = Double.parseDouble(data[1]);
                    List<Map<String, String>> stationStoppages = databaseHelper.getScheduleForStation(id);
                    departure2 = stationStoppages.get(1);
                    routeTimeAvgData.append("Station");
                    routeTimeMinData.append("Station");
                    routeTimeMedData.append("Station");
                    for (int lengthString = "Station".length(); lengthString < (lengthMaxName + 20); lengthString++) {
                        routeTimeAvgData.append(' ');
                        routeTimeMinData.append(' ');
                        routeTimeMedData.append(' ');
                    }
                    for (List<String> trainNos : trainTypesAndNumbers) {
                        routeTimeAvgData.append('\t');
                        routeTimeMinData.append('\t');
                        routeTimeMedData.append('\t');
                        routeTimeAvgData.append(trainNos.get(0));
                        routeTimeMinData.append(trainNos.get(0));
                        routeTimeMedData.append(trainNos.get(0));
                        for (int lengthString = trainNos.get(0).length(); lengthString < lengthMaxName; lengthString++) {
                            routeTimeAvgData.append(' ');
                            routeTimeMinData.append(' ');
                            routeTimeMedData.append(' ');
                        }
                    }
                    routeTimeAvgData.append('\n');
                    routeTimeMinData.append('\n');
                    routeTimeMedData.append('\n');
                    routeTimeAvgData.append(data[0]);
                    routeTimeMinData.append(data[0]);
                    routeTimeMedData.append(data[0]);
                    for (int lengthString = data[0].length(); lengthString < (lengthMaxName + 20); lengthString++) {
                        routeTimeAvgData.append(' ');
                        routeTimeMinData.append(' ');
                        routeTimeMedData.append(' ');
                    }
                    for (List<String> trainNos : trainTypesAndNumbers) {
                        routeTimeAvgData.append('\t');
                        routeTimeMinData.append('\t');
                        routeTimeMedData.append('\t');
                        routeTimeAvgData.append("NA");
                        routeTimeMinData.append("NA");
                        routeTimeMedData.append("NA");
                        for (int lengthString = "NA".length(); lengthString < lengthMaxName; lengthString++) {
                            routeTimeAvgData.append(' ');
                            routeTimeMinData.append(' ');
                            routeTimeMedData.append(' ');
                        }
                    }
                    routeTimeAvgData.append('\n');
                    routeTimeMinData.append('\n');
                    routeTimeMedData.append('\n');
                    currDistance = distanceToSubtract;
                } else {
                    prevDistance = currDistance;
                    currDistance = Double.parseDouble(data[1]);
                    departure1 = departure2;
                    List<Map<String, String>> stationStoppages = databaseHelper.getScheduleForStation(id);
                    arrival2 = stationStoppages.get(0);
                    departure2 = stationStoppages.get(1);
                    List<String> trainNosOriginal;
                    routeTimeAvgData.append(data[0]);
                    routeTimeMinData.append(data[0]);
                    routeTimeMedData.append(data[0]);
                    for (int lengthString = data[0].length(); lengthString < (lengthMaxName + 20); lengthString++) {
                        routeTimeAvgData.append(' ');
                        routeTimeMinData.append(' ');
                        routeTimeMedData.append(' ');
                    }
                    for (List<String> trainNos : trainTypesAndNumbers) {
                        trainNosOriginal = new ArrayList<>(arrival2.keySet());
                        trainNosOriginal.retainAll(trainNos);
                        double timeTrainSum = 0;
                        double timeTrainCount = 0;
                        double timeTrainMin = Double.MAX_VALUE;
                        double tempTimeTrain;
                        List<Double> timings = new ArrayList<>();
                        for (String trainNo : trainNosOriginal) {
                            String dept = departure1.getOrDefault(trainNo, null);
                            String arr = arrival2.getOrDefault(trainNo, null);
                            if (dept == null || arr == null) {
                                continue;
                            }
                            int deptTime = new TrainTime("0:" + dept).getValue();
                            int arrTime = new TrainTime("0:" + arr).getValue();
                            if (arrTime <= deptTime) {
                                arrTime += 1440;
                            }
                            if (currDistance != prevDistance) {
                                tempTimeTrain = arrTime - deptTime;
                                if (tempTimeTrain > 600 || tempTimeTrain <= 0) {
                                    continue;
                                }
                                timings.add(tempTimeTrain);
                                timeTrainSum += tempTimeTrain;
                                if (tempTimeTrain < timeTrainMin) {
                                    timeTrainMin = tempTimeTrain;
                                }
                                timeTrainCount++;
                            }
                        }

                        if (timeTrainCount > 0) {
                            Collections.sort(timings);
                            timeTrainSum = timeTrainSum / timeTrainCount;
                            routeTimeAvgData.append('\t');
                            routeTimeMinData.append('\t');
                            routeTimeMedData.append('\t');
                            routeTimeAvgData.append(new DecimalFormat("#0.00").format(timeTrainSum));
                            routeTimeMinData.append(new DecimalFormat("#0.00").format(timeTrainMin));
                            routeTimeMedData.append(new DecimalFormat("#0.00").format(timings.get(timings.size() / 2)));
                            for (int lengthString = new DecimalFormat("#0.00").format(timeTrainSum).length(); lengthString < lengthMaxName; lengthString++) {
                                routeTimeAvgData.append(' ');
                                routeTimeMinData.append(' ');
                                routeTimeMedData.append(' ');
                            }
                        } else {
                            routeTimeAvgData.append('\t');
                            routeTimeMinData.append('\t');
                            routeTimeMedData.append('\t');
                            routeTimeAvgData.append("NA");
                            routeTimeMinData.append("NA");
                            routeTimeMedData.append("NA");
                            for (int lengthString = "NA".length(); lengthString < lengthMaxName; lengthString++) {
                                routeTimeAvgData.append(' ');
                                routeTimeMinData.append(' ');
                                routeTimeMedData.append(' ');
                            }
                        }
                    }
                    routeTimeAvgData.append('\n');
                    routeTimeMinData.append('\n');
                    routeTimeMedData.append('\n');
                }
                int numOfUpPlatform = numOfPlatform / 2;
                int numOfTrack = fetchStationDetails.getNumberOfTracks(id);
                int numOfUpTrack = numOfTrack / 2;
                newRouteData.append(data[0]);
                newRouteData.append(' ');
                newRouteData.append(new DecimalFormat("#0.00").format((Double.parseDouble(data[1])) - distanceToSubtract));
                newRouteData.append(' ');
                newRouteData.append(1);
                newRouteData.append(' ');
                newRouteData.append(numOfUpPlatform);
                newRouteData.append(' ');
                newRouteData.append(numOfUpPlatform);
                newRouteData.append(' ');
                newRouteData.append((numOfPlatform - (2 * numOfUpPlatform)));
                newRouteData.append(' ');
                newRouteData.append(numOfUpTrack);
                newRouteData.append(' ');
                newRouteData.append(numOfUpTrack);
                newRouteData.append(' ');
                newRouteData.append((numOfTrack - (2 * numOfUpTrack)));
                newRouteData.append('\n');
            }
            bReader.close();
            fReader.close();
            new WriteToFile().write(pathRouteFile, newRouteData.toString(), false);
            new WriteToFile().write(pathRouteTimeAvgFile, routeTimeAvgData.toString(), false);
            new WriteToFile().write(pathRouteTimeMinFile, routeTimeMinData.toString(), false);
            new WriteToFile().write(pathRouteTimeMedFile, routeTimeMedData.toString(), false);
        } catch (Exception e) {
            System.out.println("Unable to update route file");
            e.printStackTrace();
        }
    }

    /**
     * Initializes file containing info of new train stop timings at stations in route to default value of 0.
     */
    public static void initializeStopTimeFile(String pathRouteStopTime, String pathRouteFile) {
        int maxLength = 40;
        StringBuilder stringBuilder = new StringBuilder("");
        try {
            FileReader fileReader = new FileReader(pathRouteFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    String st = line.split("\\s+")[0];
                    stringBuilder.append(st);
                    for (int i = st.length(); i < maxLength; i++) {
                        stringBuilder.append(' ');
                    }
                    stringBuilder.append('\t');
                    stringBuilder.append('0');
                    stringBuilder.append('\n');
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        new WriteToFile().write(pathRouteStopTime, stringBuilder.toString(), false);
    }

    /**
     * Returns the stop timings of the new train at the station in the route.
     */
    public static ArrayList<Integer> getStopTime(String pathRouteStopTime) {
        ArrayList<Integer> stopTime = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(pathRouteStopTime);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    stopTime.add(Integer.parseInt(line.split("\\s+")[1]));
                } catch (Exception e) {
                    e.printStackTrace();
                    stopTime = new ArrayList<>();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stopTime;
    }
}
