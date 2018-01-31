import java.io.File;
import java.io.PrintStream;
import java.util.*;

public class Main {
    public static List<Path> getSmallPart(String pathTemp, List<String> stationIdList, List<String> stationNameList,
                                    List<Double> stationDistanceList, List<Boolean> stationIsDirectLineList,
                                    List<Integer> stationNoOFUpPlatformList, List<Integer> stationNoOFDownPlatformList,
                                    List<Integer> stationNoOFDualPlatformList, List<Double> stopTimeList,
                                          int noOfPaths,TrainTime sourceTime,int minDelayBwTrains, double avgSpeed,
                                          String pathOldTrainSchedule, int startDay, int startHrs, int startMinutes,
                                          int endDay, int endHrs, int endMinutes,int maxDelayBwStations,
                                          boolean isSingleDay, int trainDay){
        Scheduler scheduler = new Scheduler();
        if(!scheduler.addRoute(stationIdList, stationNameList, stationDistanceList, stationIsDirectLineList,
                stationNoOFUpPlatformList, stationNoOFDownPlatformList, stationNoOFDualPlatformList)){
            System.out.println("Error in route info");
            return Collections.emptyList();
        }
        return new KBestSchedule().getScheduleNewTrain(pathTemp, scheduler.getStationIdList(),
                scheduler.getStationNameList(), scheduler.getStationDistanceList(),
                scheduler.getStationDirectLineList(), scheduler.getStationNoOfUpPlatformLineList(),
                scheduler.getStationNoOfDownPlatformLineList(), scheduler.getStationNoOfDualPlatformLineList(),
                noOfPaths, sourceTime, minDelayBwTrains, avgSpeed,
                stopTimeList, pathOldTrainSchedule, trainDay,startDay,startHrs, startMinutes,
                endDay,endHrs, endMinutes,maxDelayBwStations, isSingleDay, false);
    }

    public static Collection<Path> getPathsRecur(String pathTemp, int i, int stationGroupSizeForPart, Scheduler scheduler,
                                            List<Double> stopTime, int noOfPaths, TrainTime sourceTime,
                                            int minDelayBwTrains, double avgSpeed, String pathOldTrainSchedule,
                                            int maxDelayBwStations,boolean isSingleDay, int trainDay){
        if(i>=scheduler.getStationIdList().size()){
            return Collections.emptyList();
        }
        int last;
        if((i+stationGroupSizeForPart+1)<scheduler.getStationIdList().size()){
            last = i+stationGroupSizeForPart+1;
        }
        else{
            last = scheduler.getStationIdList().size();
        }
        Queue<Path> ans = new PriorityQueue<>(Collections.reverseOrder(Comparator.comparingDouble(Path::pathCost)));
        int startDay;
        int endDay;
        if(isSingleDay){
            startDay = trainDay;
            endDay = trainDay;
        }
        else{
            startDay = 0;
            endDay = 6;
        }

        // add 0 to wait time of first
        List<Path> paths = getSmallPart(pathTemp, scheduler.getStationIdList().subList(i,last),
                scheduler.getStationNameList().subList(i,last),
                scheduler.getStationDistanceList().subList(i,last),
                scheduler.getStationDirectLineList().subList(i,last),
                scheduler.getStationNoOfUpPlatformLineList().subList(i,last),
                scheduler.getStationNoOfDownPlatformLineList().subList(i,last),
                scheduler.getStationNoOfDualPlatformLineList().subList(i,last),
                stopTime.subList(i,last),noOfPaths,sourceTime,
                minDelayBwTrains,avgSpeed,pathOldTrainSchedule,startDay,0,0,endDay,23,
                59, maxDelayBwStations, isSingleDay, trainDay);

        for(Path path: paths){
            System.out.println("i= " + i + " last = " +last + " > " +path.toString());
            if(last<scheduler.getStationIdList().size()){
                List<Node> nodes1;
                List<Double> weights1;
                TrainTime sourceTime1;
                try {
                    sourceTime1 = path.getNodeList().get(path.getNodeList().size() - 2).getTime();
                }
                catch (Exception e){
                    sourceTime1 = null;
                }
                int i1 = i+stationGroupSizeForPart;
                Collection<Path> paths1= getPathsRecur(pathTemp, i1,stationGroupSizeForPart,scheduler,stopTime,noOfPaths,sourceTime1,
                        minDelayBwTrains,avgSpeed,pathOldTrainSchedule,maxDelayBwStations, isSingleDay, trainDay);
                for(Path path1: paths1){
                    Path tempPath=path.removeLastNode();
                    if(tempPath==null){
                        break;
                    }
                    nodes1 = path1.getNodeList();
                    weights1 = path1.getWeightList();
                    double tempPathCost = tempPath.pathCost();
                    for(int i2 = 1;i2<nodes1.size();i2++){
                        tempPath = tempPath.append(nodes1.get(i2), tempPathCost + weights1.get(i2));
                    }
                    System.out.println("********after addition " + tempPath.toString());
                    ans.add(tempPath);
                    if(ans.size()>=noOfPaths){
                        ans.remove();
                    }
                }
            }
            else{
                ans.add(path);
                if(ans.size()>=noOfPaths){
                    ans.remove();
                }
            }
        }
        return ans;
    }

    public static void scheduleByBreaking(String pathTemp, String pathRoute, String pathBestRoute, String pathOldTrainSchedule,
                                          boolean isSingleDay, int trainDay){
        Scheduler scheduler = new Scheduler();
        int maxDelayBwStations = 60;
        if(!scheduler.addRouteFromFile(pathRoute)){
            System.out.println("Unable to load route file");
            System.exit(0);
        }
        ArrayList<Double> stopTime = new ArrayList<>();
        for(int i=0;i<scheduler.getStationIdList().size();i++) {
            stopTime.add(0.0);
        }
        // stopTime.set(5, 2.0);
        // stopTime.set(12, 4.0);
        // stopTime.set(22, 4.0);

        Double avgSpeed = 80.0;
        int minDelayBwTrains = 3;
        TrainTime sourceTime = null;
        String pathBestRouteFile;
        int noOfPaths = 10;
        int stationGroupSizeForPart = 20;
        // for(int i=0;i<stationIdComplete.size();i+=stationGroupSizeForPart){
        //     pathBestRouteFile = pathBestRoute + File.separator + "AvgSpeed "+avgSpeed +" part " +
        //             (i/(stationGroupSizeForPart-1) + 1);
        //
        //     int count=0;
        //
        // }

        Collection<Path> paths = getPathsRecur(pathTemp,0,stationGroupSizeForPart,scheduler,stopTime,noOfPaths,null,
                minDelayBwTrains,avgSpeed,pathOldTrainSchedule,maxDelayBwStations, isSingleDay, trainDay);

        Queue<Path> pathQueue = new PriorityQueue<>(Collections.reverseOrder(Comparator.comparingDouble(Path::pathCost)));
        for(Path path: paths) {
            pathQueue.add(path);
            System.out.println(path.toString() + " cost: " + path.pathCost());
            if(pathQueue.size()>noOfPaths){
                pathQueue.remove();
                System.out.println("Removed");
            }
            // Scheduler.writePathsToFile(path,++count, pathBestRouteFile,stopTime,avgSpeed,stationIdComplete.subList(i,last),
            //         stationNameComplete.subList(i,last), stationDistanceComplete.subList(i,last) );
        }
    }

    public static void main(String[] args) {
        String pathTrainList = "data" + File.separator +"train_list.txt";

        String pathRoute = "data"+File.separator+"route"+File.separator+"route.txt";
        String pathOldTrainSchedule = "data"+File.separator+"final" + File.separator + "dayall";

        String pathPlotFile = "data"+File.separator+"plot"+File.separator+"plot1.pdf";
        String pathTemp = "data"+File.separator+"temp";
        String pathLog = "data"+File.separator+"logs";
        String pathFinal = "data"+File.separator+"final";
        String pathBestRoute = "data"+File.separator+"bestRoute";
        int trainDay = 0;
        boolean isSingleDay = true;
        boolean usePreviousComputation = false;

        if(!Scheduler.createParentFolder(pathTrainList) || !Scheduler.createParentFolder(pathRoute)
                || !Scheduler.createParentFolder(pathPlotFile) || !Scheduler.createFolder(pathTemp)
                || !Scheduler.createFolder(pathLog) || !Scheduler.createFolder(pathFinal)
                || !Scheduler.createFolder(pathBestRoute) || !Scheduler.createFolder(pathOldTrainSchedule)){
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
            Scheduler.test(pathTemp, pathRoute,pathBestRoute,pathOldTrainSchedule, isSingleDay, trainDay, usePreviousComputation);
            // Scheduler.getTrainList(pathTrainList);
            // Scheduler.fetchTrainSchedule(pathTrainList,pathTemp,pathFinal);

            // scheduleByBreaking(pathTemp, pathRoute,pathBestRoute,pathOldTrainSchedule, isSingleDay, trainDay);

            int newTrainNo = 9910;
            String pathNewTrainFile = pathBestRoute+File.separator+"Type 2 AvgSpeed 80.0 path 1 cost 24.0 .path";
            Scheduler.showPlot(pathBestRoute,newTrainNo,pathPlotFile,pathRoute,pathOldTrainSchedule,true);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
