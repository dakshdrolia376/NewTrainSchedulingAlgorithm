package iitp.naman.newtrainschedulingalgorithm.datahelper;

import iitp.naman.newtrainschedulingalgorithm.util.FolderHelper;

public class DataHelper {

    public static void fetchStationInfo(String pathStationDatabase) {
        if (!new FetchStationDetails(pathStationDatabase).fetchAll()) {
            System.out.println("unable to fetch Station Info..");
        }
    }

    public static void fetchTrainInfo(String pathTrainDatabase) {
        if (!new FetchTrainDetails(pathTrainDatabase).fetchAll()) {
            System.out.println("unable to fetch Train Info..");
        }
    }

    public static void putTrainIntoDatabase(String pathTrainDatabase) {
        new FetchTrainDetails(pathTrainDatabase).putAllTrainsInMap();
        new FetchTrainDetails(pathTrainDatabase).putTrainsMapInDatabase();
    }

    public static void putStationIntoDatabase(String pathStationDatabase) {
        new FetchStationDetails(pathStationDatabase).putAllStationsInMap();
        new FetchStationDetails(pathStationDatabase).putStationMapInDatabase();
    }

    public static void putStoppagesIntoDatabase(String pathTrainDatabase) {
        new FetchTrainDetails(pathTrainDatabase).putAllStoppagesInDatabase();
    }

    public static void fetchTrainSchedule(String pathTrainList, String pathTemp, String pathTrainBase, String pathTrainDatabase) {
        FolderHelper.deleteFolderContent(pathTrainBase);
        new FetchTrainDetails(pathTrainDatabase).getTrainStoppageFromFile(pathTrainList, pathTrainBase);
    }
}
