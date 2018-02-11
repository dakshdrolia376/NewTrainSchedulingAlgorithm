import java.io.File;
import java.io.PrintStream;

public class Main {

    public static void main(String[] args) {
        String pathUpTrainList = "data" + File.separator +"upTrainList.txt";
        String pathDownTrainList = "data" + File.separator +"downTrainList.txt";

        String pathRoute = "data"+File.separator+"route"+File.separator+"route.txt";

        String pathPlotFile = "data"+File.separator+"plot"+File.separator+"plot1.pdf";
        String pathTemp = "data"+File.separator+"temp";
        String pathLog = "data"+File.separator+"logs";
        String pathUpTrainBase = "data"+File.separator+"final" + File.separator+"upTrains";
        String pathDownTrainBase = "data"+File.separator+"final" +File.separator+"downTrains";

        String pathOldUpTrainSchedule = pathUpTrainBase+ File.separator+"day0";
        String pathOldDownTrainSchedule = pathDownTrainBase+"";

        String pathBestRoute = "data"+File.separator+"bestRoute";
        String pathStationDatabase = pathTemp+File.separator+"databaseStation";
        String pathTrainDatabase = pathTemp+File.separator+"databaseTrain";
        int trainDay = 0;
        boolean isSingleDay = true;
        boolean usePreviousComputation = false;
        double ratio = 1.3;

        if(!Scheduler.createParentFolder(pathUpTrainList) || !Scheduler.createParentFolder(pathRoute)
                || !Scheduler.createParentFolder(pathPlotFile) || !Scheduler.createFolder(pathTemp)
                || !Scheduler.createFolder(pathLog) || !Scheduler.createFolder(pathUpTrainBase)
                || !Scheduler.createFolder(pathDownTrainBase) || !Scheduler.createFolder(pathBestRoute)
                || !Scheduler.createFolder(pathOldUpTrainSchedule) || !Scheduler.createFolder(pathOldDownTrainSchedule)
                || !Scheduler.createFolder(pathStationDatabase) || !Scheduler.createFolder(pathTrainDatabase)
                || !Scheduler.createParentFolder(pathDownTrainList)){
            System.out.println("Unable to create directory");
            System.exit(1);
        }

        if(!Scheduler.isNetAvailable()){
            System.out.println("No internet Connection.. Try again");
            // System.exit(0);
        }

        try {
            // Creating a File object that represents the disk file.
            PrintStream o = new PrintStream(new File(pathLog + File.separator+"err.log"));
            PrintStream o1 = new PrintStream(new File(pathLog + File.separator+"output.log"));
            // Store current System.out before assigning a new value
            // PrintStream console = System.err;
            // PrintStream console1 = System.out;
            //
            // Assign o to output stream
            System.setErr(o);
            System.setOut(o1);
            Scheduler.test(pathTemp, pathRoute,pathBestRoute,pathOldUpTrainSchedule,pathOldDownTrainSchedule,
                    isSingleDay, trainDay, usePreviousComputation, ratio);

            // Scheduler.fetchStationInfo(pathStationDatabase);
            // Scheduler.fetchTrainInfo(pathTrainDatabase);
            // Scheduler.getTrainList(pathRoute, pathUpTrainList, pathDownTrainList);
            // Scheduler.putStationIntoDatabase(pathStationDatabase);
            // Scheduler.putTrainIntoDatabase(pathTrainDatabase);
            // Scheduler.putStoppagesIntoDatabase(pathTrainDatabase);

            // new ScheduleByDivision().scheduleByBreaking(pathTemp, pathRoute, pathBestRoute, pathOldUpTrainSchedule,
            //         pathOldDownTrainSchedule,isSingleDay, trainDay, ratio);

            // int newTrainNo = 9910;
            // String pathNewTrainFile = pathBestRoute+File.separator+"Type 2 AvgSpeed 80.0 path 1 cost 24.0 .path";
            // Scheduler.showPlot(null,newTrainNo,pathPlotFile,pathRoute,pathOldUpTrainSchedule,
            //         true, isSingleDay, trainDay);


            // Scheduler.updateRouteFile(pathRoute, pathStationDatabase);
            // Scheduler.createTrainList(pathRoute, pathUpTrainList,pathDownTrainList, pathTrainDatabase);
            // Scheduler.fetchTrainSchedule(pathUpTrainList,pathTemp, pathUpTrainBase, pathTrainDatabase);
            // Scheduler.fetchTrainSchedule(pathDownTrainList,pathTemp, pathDownTrainBase, pathTrainDatabase);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
