import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        String pathTrainList = "data" + File.separator+"trainList.txt";
        String pathRoute = "data"+File.separator+"route"+File.separator+"routePnbeNdls.txt";
        String pathName = "Pnbe-Ndls";
        String pathRouteStopTime = "data"+File.separator+"route"+File.separator+"routeStopTimeMgs.txt";

        String pathRouteTimeMin = pathRoute.split("\\.")[0]+"TimeMin.txt";
        String pathRouteTimeAvg = pathRoute.split("\\.")[0]+"TimeAvg.txt";

        String pathPlotFile = "data"+File.separator+"plot"+File.separator+"plot.pdf";
        String pathTemp = "data"+File.separator+"temp";
        String pathLog = "data"+File.separator+"logs";
        String pathTrainBase = "data"+File.separator+"final";
        String pathTrainTypeFile = "data"+File.separator+"route"+File.separator+"trainTypeFile.txt";

        String pathBestRoute = "data"+File.separator+"bestRoute";
        String pathStationDatabase = pathTemp+File.separator+"databaseStation";
        String pathTrainDatabase = pathTemp+File.separator+"databaseTrain";
        boolean usePreviousComputation = false;

        if(!Scheduler.createParentFolder(pathTrainList) || !Scheduler.createParentFolder(pathRoute)
                || !Scheduler.createParentFolder(pathPlotFile) || !Scheduler.createFolder(pathTemp)
                || !Scheduler.createFolder(pathLog) || !Scheduler.createFolder(pathTrainBase)
                || !Scheduler.createFolder(pathBestRoute) || !Scheduler.createFolder(pathStationDatabase)
                || !Scheduler.createFolder(pathTrainDatabase)){
            System.out.println("Unable to create directory");
            System.exit(1);
        }

        if(!Scheduler.isNetAvailable()){
            System.out.println("No internet Connection.. Some functionality may not work...");
        }

        try {
            // Creating a File object that represents the disk file.
            PrintStream o = new PrintStream(new File(pathLog + File.separator+"err.log"));
            PrintStream o1 = new PrintStream(new File(pathLog + File.separator+"output.log"));
            // Store current System.out before assigning a new value
            PrintStream console = System.err;
            PrintStream console1 = System.out;
            //
            // Assign o to output stream
            System.setErr(o);
            System.setOut(o1);

            int count = 0;
            double ratio = 1.3;
            int trainDay=0;
            boolean isSingleDay =true;
            String newTrainType = "memu";

            TrainTime sourceTime=new TrainTime(0,8,0);
            int trainNotToLoad = 63227;
            sourceTime = null;
            trainNotToLoad = -1;

            Scheduler scheduler = new Scheduler();
            ScheduleByDivision scheduleByDivision = new ScheduleByDivision(pathStationDatabase);

            // newTrainType = "memu";
            // sourceTime = null;
            // trainNotToLoad = -1;
            // pathRouteStopTime = "data"+File.separator+"route"+File.separator+"routeStopTimePnbeMgsMemu.txt";
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 0,
            //         usePreviousComputation, ratio, pathRouteTimeMin, newTrainType, pathLog, sourceTime,pathRouteStopTime,
            //         trainNotToLoad,pathStationDatabase);

            // newTrainType = "Mail-Express";
            // sourceTime = null;
            // trainNotToLoad = -1;
            // pathRouteStopTime = "data"+File.separator+"route"+File.separator+"routeStopTimePnbeNdlsExp.txt";
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 0,
            //         usePreviousComputation, ratio, pathRouteTimeMin, newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad,pathStationDatabase);
            //
            // newTrainType = "SuperFast";
            // sourceTime = null;
            // trainNotToLoad = -1;
            // pathRouteStopTime = "data"+File.separator+"route"+File.separator+"routeStopTimePnbeNdlsSF.txt";
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 0,
            //         usePreviousComputation, ratio, pathRouteTimeMin, newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad,pathStationDatabase);
            //
            // newTrainType = "Rajdhani";
            // sourceTime = null;
            // trainNotToLoad = -1;
            // pathRouteStopTime = "data"+File.separator+"route"+File.separator+"routeStopTimePnbeNdlsRaj.txt";
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 0,
            //         usePreviousComputation, ratio, pathRouteTimeMin, newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad,pathStationDatabase);


            // newTrainType = "memu";
            // sourceTime = new TrainTime(0,8,0);
            // trainNotToLoad = 63227;
            // pathRouteStopTime = "data"+File.separator+"route"+File.separator+"routeStopTimePnbeNdlsMemu.txt";
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 0,
            //         usePreviousComputation, ratio, pathRouteTimeMin, newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad,pathStationDatabase);

            // newTrainType = "Mail-Express";
            // sourceTime = new TrainTime(0,21,25);
            // trainNotToLoad = 13007;
            // pathRouteStopTime = "data"+File.separator+"route"+File.separator+"routeStopTimePnbeNdlsExp.txt";
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 0,
            //         usePreviousComputation, ratio, pathRouteTimeMin, newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad,pathStationDatabase);
            //
            // newTrainType = "SuperFast";
            // sourceTime = new TrainTime(0,18,10);
            // trainNotToLoad = 12401;
            // pathRouteStopTime = "data"+File.separator+"route"+File.separator+"routeStopTimePnbeNdlsSF.txt";
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 0,
            //         usePreviousComputation, ratio, pathRouteTimeMin, newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad,pathStationDatabase);
            //
            // newTrainType = "Rajdhani";
            // sourceTime = new TrainTime(0,21,10);
            // trainNotToLoad = 12305;
            // pathRouteStopTime = "data"+File.separator+"route"+File.separator+"routeStopTimePnbeNdlsRaj.txt";
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 0,
            //         usePreviousComputation, ratio, pathRouteTimeMin, newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad),pathStationDatabase;

            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 1,
            //         usePreviousComputation, ratio, pathRouteTimeMin,newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad,pathStationDatabase);
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 2,
            //         usePreviousComputation, ratio, pathRouteTimeMin,newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad,pathStationDatabase);
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 3,
            //         usePreviousComputation, ratio, pathRouteTimeMin,newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad,pathStationDatabase);
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 4,
            //         usePreviousComputation, ratio, pathRouteTimeMin,newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad,pathStationDatabase);
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 5,
            //         usePreviousComputation, ratio, pathRouteTimeMin,newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad,pathStationDatabase);
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 6,
            //         usePreviousComputation, ratio, pathRouteTimeMin,newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad,pathStationDatabase);
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, false, 7,
            //         usePreviousComputation, ratio, pathRouteTimeMin,newTrainType, pathLog, sourceTime,pathRouteStopTime,trainNotToLoad,pathStationDatabase);
            //
            // scheduleByDivision.scheduleByBreaking(pathTemp, pathRoute, pathBestRoute, pathTrainBase,isSingleDay, trainDay, ratio,
            //         pathLog, pathRouteTimeMin,newTrainType, sourceTime,pathRouteStopTime,trainNotToLoad);

            //             System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
            //                     " count "+count +" Type Break Day "+trainDay+" AvgSpeed "+avgSpeed +
            //                     " maxRatio "+ratio +" conditional ");

            int newTrainNo = 9910;
            int newTrainDay = 0;
            // String pathNewTrainFile = pathBestRoute+File.separator+"Type Full Day 0 TrainType memu maxRatio 1.3 unconditional  path 1 cost 256.0 .txt";
            scheduler.showPlot(null,newTrainNo,pathPlotFile,pathRoute,pathTrainBase, newTrainDay,
                    pathStationDatabase,pathName);

            // scheduler.fetchStationInfo(pathStationDatabase);
            // scheduler.fetchTrainInfo(pathTrainDatabase);
            // scheduler.getTrainList(pathRoute, pathUpTrainList, pathDownTrainList);
            // scheduler.putStationIntoDatabase(pathStationDatabase);
            // scheduler.putTrainIntoDatabase(pathTrainDatabase);
            // scheduler.putStoppagesIntoDatabase(pathTrainDatabase);
            //
            // scheduler.updateTrainTypeFile(pathTrainTypeFile);
            // scheduler.updateRouteFile(pathTrainTypeFile, pathRoute, pathRouteTimeMin,pathRouteTimeAvg, pathStationDatabase);
            // scheduler.initializeStopTimeFile(pathRouteStopTime,pathRoute);
            // scheduler.createTrainList(pathRoute, pathTrainList);
            // scheduler.fetchTrainSchedule(pathTrainList,pathTemp, pathTrainBase, pathTrainDatabase);

            System.setOut(console1);
            System.setErr(console);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
