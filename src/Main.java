import java.io.File;
import java.io.PrintStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String pathTrainList = "data" + File.separator +"train_list.txt";
        String pathRoute = "data"+File.separator+"route"+File.separator+"route.txt";
        String pathPlotFile = "data"+File.separator+"plot"+File.separator+"plot1.pdf";
        String pathTemp = "data"+File.separator+"temp";
        String pathLog = "data"+File.separator+"logs";
        String pathFinal = "data"+File.separator+"final";
        String pathBestRoute = "data"+File.separator+"bestRoute";
        String pathOldTrainSchedule = "data"+File.separator+"final" + File.separator + "dayall";

        if(!Scheduler.createParentFolder(pathTrainList) || !Scheduler.createParentFolder(pathRoute) || !Scheduler.createParentFolder(pathPlotFile)
                || !Scheduler.createFolder(pathTemp) || !Scheduler.createFolder(pathLog) || !Scheduler.createFolder(pathFinal)
                || !Scheduler.createFolder(pathBestRoute) || !Scheduler.createFolder(pathOldTrainSchedule)){
            System.out.println("Unable to create directory");
            System.exit(1);
        }

        if(!Scheduler.isNetAvailable()){
            System.out.println("No internet Connection.. Try again");
            System.exit(0);
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
            // Scheduler.test(pathRoute,pathBestRoute,pathOldTrainSchedule);

            // new TrainList().getTrainList(pathTrainList);
            // new TrainStoppageList().getTrainStoppageFromFile(pathTrainList,pathTemp,pathFinal);

            Scheduler scheduler = new Scheduler();

            if(!scheduler.addRouteFromFile(pathRoute)){
                System.out.println("Unable to load route file");
                System.exit(0);
            }
            ArrayList<String> stationIdComplete= new ArrayList<>(scheduler.getStationIdList());
            ArrayList<String> stationNameComplete= new ArrayList<>(scheduler.getStationNameList());
            ArrayList<Double> stationDistanceComplete= new ArrayList<>(scheduler.getStationDistanceList());
            ArrayList<Double> stopTime = new ArrayList<>();
            for(int i=0;i<stationIdComplete.size();i++) {
                stopTime.add(0.0);
            }
            // stopTime.set(5, 2.0);
            // stopTime.set(12, 4.0);
            // stopTime.set(22, 4.0);

            Double avgSpeed = 80.0;
            int minDelayBwTrains = 3;
            LocalTime sourceTime = null;
            String pathBestRouteFile;
            int noOfPaths = 10;

            int stationGroupSizeForPart = 51;
            Scheduler scheduler1;
            for(int i=0;i<stationIdComplete.size();i+=stationGroupSizeForPart){
                pathBestRouteFile = pathBestRoute + File.separator + "AvgSpeed "+avgSpeed +" part " + (i/(stationGroupSizeForPart-1) + 1);
                 scheduler1= new Scheduler();
                int last;
                if((i+stationGroupSizeForPart)<stationIdComplete.size()){
                    last = i+stationGroupSizeForPart;
                    i--;
                }
                else{
                    last = stationIdComplete.size();
                }
                if(!scheduler1.addRoute(new ArrayList<>(stationIdComplete.subList(i,last)),new ArrayList<>(stationNameComplete.subList(i,last)),new ArrayList<>(stationDistanceComplete.subList(i,last)))){
                    System.out.println("Error in route info");
                    continue;
                }
                List<Path<String>> paths= new KBestSchedule().getScheduleNewTrain(scheduler1.getStationIdList(), scheduler1.getStationNameList(), scheduler1.getStationDistanceList(), noOfPaths, sourceTime, minDelayBwTrains, avgSpeed, new ArrayList<>(stopTime.subList(i,last)), pathOldTrainSchedule);
                System.out.println(paths.size());
                for(Path<String> path: paths) {
                    System.out.println(path.toString() + " cost: " + path.pathCost());
                }
                sourceTime = Scheduler.getNodeData(paths.get(0).getNodeList().get(paths.get(0).getNodeList().size()-2)).getSecond();
                scheduler1.writePathsToFile(paths,pathBestRouteFile,stopTime,avgSpeed);
            }

            int newTrainNo = 9910;
            String pathNewTrainFile = pathBestRoute+File.separator+"Type 2 AvgSpeed 80.0 path 1 cost 183.0 .path";
            Scheduler.showPlot(pathNewTrainFile,newTrainNo,pathPlotFile,pathRoute,pathOldTrainSchedule);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
