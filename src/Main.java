import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        String pathTrainList = "data" + File.separator+"trainList.txt";
        String pathRoute = "data"+File.separator+"route"+File.separator+"routeCreated1.txt";

        String pathPlotFile = "data"+File.separator+"plot"+File.separator+"plot1.pdf";
        String pathTemp = "data"+File.separator+"temp";
        String pathLog = "data"+File.separator+"logs";
        String pathTrainBase = "data"+File.separator+"final";

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
            double avgSpeed=80;
            double ratio = 1.3;
            int trainDay=0;
            boolean isSingleDay =true;

            Scheduler scheduler = new Scheduler();
            ScheduleByDivision scheduleByDivision = new ScheduleByDivision();
            scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 0,
                    usePreviousComputation, ratio, avgSpeed, pathLog, new TrainTime(0,13,12));
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 1,
            //         usePreviousComputation, ratio, avgSpeed, pathLog, null);
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 2,
            //         usePreviousComputation, ratio, avgSpeed, pathLog, null);
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 3,
            //         usePreviousComputation, ratio, avgSpeed, pathLog, null);
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 4,
            //         usePreviousComputation, ratio, avgSpeed, pathLog, null);
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 5,
            //         usePreviousComputation, ratio, avgSpeed, pathLog, null);
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, true, 6,
            //         usePreviousComputation, ratio, avgSpeed, pathLog, null);
            // scheduler.test(pathTemp, pathRoute, pathBestRoute, pathTrainBase, false, 7,
            //         usePreviousComputation, ratio, avgSpeed, pathLog, null);

            // scheduleByDivision.scheduleByBreaking(pathTemp, pathRoute, pathBestRoute, pathTrainBase,isSingleDay, trainDay, ratio,
            //         pathLog, avgSpeed, null);

            //             System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
            //                     " count "+count +" Type Break Day "+trainDay+" AvgSpeed "+avgSpeed +
            //                     " maxRatio "+ratio +" conditional ");




            // int newTrainNo = 9910;
            // int newTrainDay = 4;
            // // String pathNewTrainFile = pathBestRoute+File.separator+"Type Full Day 7 AvgSpeed 80.0 maxRatio 1.3 unconditional  path 1 cost 813.0 .path";
            // String pathNewTrainFile = pathBestRoute+File.separator+"Type Break Day 0 AvgSpeed 80.0 maxRatio 1.3 unconditional  path 1 cost 8.0 .path";
            // scheduler.showPlot(null,newTrainNo,pathPlotFile,pathRoute,pathTrainBase,
            //         newTrainDay);

            // scheduler.fetchStationInfo(pathStationDatabase);
            // scheduler.fetchTrainInfo(pathTrainDatabase);
            // scheduler.getTrainList(pathRoute, pathUpTrainList, pathDownTrainList);
            // scheduler.putStationIntoDatabase(pathStationDatabase);
            // scheduler.putTrainIntoDatabase(pathTrainDatabase);
            // scheduler.putStoppagesIntoDatabase(pathTrainDatabase);
            //
            // scheduler.updateRouteFile(pathRoute, pathStationDatabase);
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
