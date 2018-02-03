import java.io.File;
import java.io.PrintStream;

public class Main {

    public static void main(String[] args) {
        String pathTrainList = "data" + File.separator +"trainList.txt";

        String pathRoute = "data"+File.separator+"route"+File.separator+"routePnbeNdls.txt";
        String pathOldTrainSchedule = "data"+File.separator+"final" + File.separator + "dayall";

        String pathPlotFile = "data"+File.separator+"plot"+File.separator+"plot1.pdf";
        String pathTemp = "data"+File.separator+"temp";
        String pathLog = "data"+File.separator+"logs";
        String pathFinal = "data"+File.separator+"final";
        String pathBestRoute = "data"+File.separator+"bestRoute";
        String pathStationDatabase = pathTemp+File.separator+"databaseStation";
        String pathTrainDatabase = pathTemp+File.separator+"databaseTrain";
        int trainDay = 0;
        boolean isSingleDay = true;
        boolean usePreviousComputation = true;
        double ratio = 1.6;

        if(!Scheduler.createParentFolder(pathTrainList) || !Scheduler.createParentFolder(pathRoute)
                || !Scheduler.createParentFolder(pathPlotFile) || !Scheduler.createFolder(pathTemp)
                || !Scheduler.createFolder(pathLog) || !Scheduler.createFolder(pathFinal)
                || !Scheduler.createFolder(pathBestRoute) || !Scheduler.createFolder(pathOldTrainSchedule)
                || !Scheduler.createFolder(pathStationDatabase) || !Scheduler.createFolder(pathTrainDatabase)){
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
            // Scheduler.test(pathTemp, pathRoute,pathBestRoute,pathOldTrainSchedule, isSingleDay, trainDay, usePreviousComputation);

            // Scheduler.fetchTrainInfo(pathTrainDatabase);
            // Scheduler.fetchStationInfo(pathStationDatabase);
            // Scheduler.getTrainList(pathTrainList);

            // new ScheduleByDivision().scheduleByBreaking(pathTemp, pathRoute, pathBestRoute, pathOldTrainSchedule,
            //         isSingleDay, trainDay, ratio);

            // int newTrainNo = 9910;
            // String pathNewTrainFile = pathBestRoute+File.separator+"Type 2 AvgSpeed 80.0 path 1 cost 24.0 .path";
            // Scheduler.showPlot(pathBestRoute,newTrainNo,pathPlotFile,pathRoute,pathOldTrainSchedule,
            //         true, isSingleDay, trainDay);

            // Scheduler.putStationIntoDatabase(pathStationDatabase);
            // Scheduler.putTrainIntoDatabase(pathTrainDatabase);
            // Scheduler.putStoppagesIntoDatabase(pathTrainDatabase);

            Scheduler.updateRouteFile(pathRoute, pathStationDatabase);
            Scheduler.createTrainList(pathRoute, pathTrainList, pathTrainDatabase);
            Scheduler.fetchTrainSchedule(pathTrainList,pathTemp, pathFinal, pathTrainDatabase);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
