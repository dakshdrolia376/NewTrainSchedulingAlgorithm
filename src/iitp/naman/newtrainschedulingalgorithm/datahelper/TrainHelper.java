package iitp.naman.newtrainschedulingalgorithm.datahelper;

import iitp.naman.newtrainschedulingalgorithm.util.StationIdHelper;
import iitp.naman.newtrainschedulingalgorithm.util.WriteToFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainHelper {

    /**
     * Updates the type of trains & the train number at given path.
     *
     * @param pathTrainTypeFile file path where train type details has to be stored.
     */
    public static void updateTrainTypeFile(String pathTrainTypeFile) {
        List<List<String>> trainNames = new DatabaseHelper().getAllTrainNames();
        Map<String, List<String>> mapTrainTypes = new HashMap<>();
        for (List<String> trainType : trainNames) {
            String temp = trainType.get(2).replaceAll("\\s+", "-");
            temp = temp.replaceAll("/", "-");
            mapTrainTypes.putIfAbsent(temp, new ArrayList<>());
            mapTrainTypes.get(temp).add(trainType.get(0));
        }

        StringBuilder stringBuilder = new StringBuilder("");

        for (String type : mapTrainTypes.keySet()) {
            stringBuilder.append(type);
            List<String> traiNos = mapTrainTypes.get(type);
            for (String trainNo : traiNos) {
                stringBuilder.append('\t');
                stringBuilder.append(trainNo);
            }
            stringBuilder.append('\n');
        }
        new WriteToFile().write(pathTrainTypeFile, stringBuilder.toString(), false);
    }

    /**
     * @param pathRouteTimeFile path for train avg timing details for the route.
     * @param newTrainType      type of new train.
     * @return List of time taken by train between stations in route.
     */
    public static List<Double> loadNewTrainTimeData(String pathRouteTimeFile, String newTrainType) {
        List<Double> timeNewTrain = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(pathRouteTimeFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            int count = -1;
            String data[] = line.split("\\s+");
            for (int i = 0; i < data.length; i++) {
                if (data[i].equalsIgnoreCase(newTrainType)) {
                    count = i;
                    break;
                }
            }
            while (count > 0 && (line = bufferedReader.readLine()) != null) {
                String avgTime = line.split("\\s+")[count];
                double avgTimeDouble = 0;
                try {
                    if (!avgTime.equalsIgnoreCase("NA")) {
                        avgTimeDouble = Double.parseDouble(avgTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                timeNewTrain.add(avgTimeDouble);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeNewTrain;
    }

    /**
     * Creates the list of train in the given route.
     *
     * @param pathRoute     path for file containing route info.
     * @param pathTrainList path for file containing train info.
     */
    public static void createTrainList(String pathRoute, String pathTrainList) {
        FileReader fReader;
        BufferedReader bReader;
        String line;
        String stationId;
        DatabaseHelper databaseHelper = new DatabaseHelper();
        List<String> stationIds = new ArrayList<>();
        try {
            fReader = new FileReader(pathRoute);
            bReader = new BufferedReader(fReader);
            while ((line = bReader.readLine()) != null) {
                stationId = StationIdHelper.getStationIdFromName(line.split("\\s+")[0]);
                stationIds.add(stationId);
            }
            bReader.close();
            fReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Integer> trainNos = databaseHelper.getTrainNosForStation(stationIds);
        List<List<Integer>> Trains = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Trains.add(databaseHelper.getTrainNosForDay(i));
            Trains.get(i).retainAll(trainNos);
        }

        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i < 7; i++) {
            stringBuilder.append(i);
            for (int trainNo : Trains.get(i)) {
                stringBuilder.append('\t');
                stringBuilder.append(trainNo);
            }
            stringBuilder.append('\n');
        }
        new WriteToFile().write(pathTrainList, stringBuilder.toString(), false);
    }
}
