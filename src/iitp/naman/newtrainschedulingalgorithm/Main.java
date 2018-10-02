package iitp.naman.newtrainschedulingalgorithm;

import iitp.naman.newtrainschedulingalgorithm.datahelper.*;
import iitp.naman.newtrainschedulingalgorithm.util.*;

import java.io.File;
import java.io.PrintStream;

public class Main {

    public static void main(String[] args) {
        String pathTrainList = "data" + File.separator + "trainList.txt";
        String pathName = "PnbeMgs";
        String pathRoute = "data" + File.separator + "route" + File.separator + "route" + pathName + ".txt";
        String pathRouteStopTime = "data" + File.separator + "route" + File.separator + "routeStopTime" + pathName + "Exp.txt";

        String pathRouteTimeMin = pathRoute.split("\\.")[0] + "TimeMin.txt";
        String pathRouteTimeAvg = pathRoute.split("\\.")[0] + "TimeAvg.txt";
        String pathRouteTimeMed = pathRoute.split("\\.")[0] + "TimeMed.txt";

        String pathPlotFile = "data" + File.separator + "plot" + File.separator + "plot.pdf";
        String pathTemp = "data" + File.separator + "temp";
        String pathLog = "data" + File.separator + "logs";
        String pathTrainBase = "data" + File.separator + "final";
        String pathTrainTypeFile = "data" + File.separator + "route" + File.separator + "trainTypeFile.txt";

        String pathBestRoute = "data" + File.separator + "bestRoute";
        String pathStationDatabase = pathTemp + File.separator + "databaseStation";
        String pathTrainDatabase = pathTemp + File.separator + "databaseTrain";
        boolean usePreviousComputation = false;

        if (!FolderHelper.createParentFolder(pathTrainList) || !FolderHelper.createParentFolder(pathRoute)
                || !FolderHelper.createParentFolder(pathPlotFile) || !FolderHelper.createFolder(pathTemp)
                || !FolderHelper.createFolder(pathLog) || !FolderHelper.createFolder(pathTrainBase)
                || !FolderHelper.createFolder(pathBestRoute) || !FolderHelper.createFolder(pathStationDatabase)
                || !FolderHelper.createFolder(pathTrainDatabase)) {
            System.out.println("Unable to create directory");
            System.exit(1);
        }

        if (!InternetHelper.isNetAvailable()) {
            System.out.println("No internet Connection.. Some functionality may not work...");
        }

        try {
            // Creating a File object that represents the disk file.
            PrintStream o = new PrintStream(new File(pathLog + File.separator + "err.log"));
            PrintStream o1 = new PrintStream(new File(pathLog + File.separator + "output.log"));
            // Store current System.out before assigning a new value
            PrintStream console = System.err;
            PrintStream console1 = System.out;
            //
            // Assign o to output stream
            System.setErr(o);
            System.setOut(o1);

            DataHelper.fetchStationInfo(pathStationDatabase);
            DataHelper.fetchTrainInfo(pathTrainDatabase);
            DataHelper.putStationIntoDatabase(pathStationDatabase);
            DataHelper.putTrainIntoDatabase(pathTrainDatabase);
            DataHelper.putStoppagesIntoDatabase(pathTrainDatabase);
            TrainHelper.updateTrainTypeFile(pathTrainTypeFile);

            RouteHelper.updateRouteFile(pathTrainTypeFile, pathRoute, pathRouteTimeMin,pathRouteTimeAvg, pathRouteTimeMed, pathStationDatabase);
            RouteHelper.initializeStopTimeFile(pathRouteStopTime,pathRoute);
            TrainHelper.createTrainList(pathRoute, pathTrainList);
            DataHelper.fetchTrainSchedule(pathTrainList,pathTemp, pathTrainBase, pathTrainDatabase);

            double ratio = 1.3;
            int trainDay = 0;
            boolean isSingleDay = true;
            String newTrainType;
            TrainTime sourceTime;
            int trainNotToLoad;

            Scheduler scheduler = new Scheduler();
//            ScheduleByDivision scheduleByDivision = new ScheduleByDivision(pathStationDatabase);

            newTrainType = "Mail-Express";
            sourceTime = null;
            trainNotToLoad = -1;
            pathRouteStopTime = "data" + File.separator + "route" + File.separator + "routeStopTimePnbeMgsExp.txt";
            scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 0,
                    usePreviousComputation, ratio, pathRouteTimeMed, newTrainType, pathLog, sourceTime, pathRouteStopTime,
                    trainNotToLoad, pathStationDatabase);

            System.setOut(console1);
            System.setErr(console);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
