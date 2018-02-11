import java.io.File;
import java.util.*;

public class ScheduleByDivision {

    private List<String> stationIdList;
    private List<String> stationNameList;
    private List<Double> stationDistanceList;
    private List<Boolean> stationIsDirectLineList;
    private List<Integer> stationNoOFUpPlatformList;
    private List<Integer> stationNoOFDownPlatformList;
    private List<Integer> stationNoOFDualPlatformList;
    private List<Integer> stationNoOfUpTrackList;
    private List<Integer> stationNoOfDownTrackList;
    private List<Integer> stationNoOfDualTrackList;
    private List<Integer> stopTimeList;
    private Collection<Path> bestAns;

    public List<Path> getSmallPart(String pathTemp, int firstIndex, int lastIndex, int noOfPaths, TrainTime sourceTime,
                                   int minDelayBwTrains, double avgSpeed, String pathOldUpTrainSchedule,
                                   String pathOldDownTrainSchedule, int startDay,
                                   int startHrs, int startMinutes, int endDay, int endHrs, int endMinutes,
                                   int maxDelayBwStations, boolean isSingleDay, int trainDay, double ratio){
        Scheduler scheduler = new Scheduler();
        if(!scheduler.addRoute(this.stationIdList.subList(firstIndex,lastIndex),
                this.stationNameList.subList(firstIndex,lastIndex), this.stationDistanceList.subList(firstIndex,lastIndex),
                this.stationIsDirectLineList.subList(firstIndex,lastIndex),
                this.stationNoOFUpPlatformList.subList(firstIndex,lastIndex),
                this.stationNoOFDownPlatformList.subList(firstIndex,lastIndex),
                this.stationNoOFDualPlatformList.subList(firstIndex,lastIndex),
                this.stationNoOfUpTrackList.subList(firstIndex,lastIndex),
                this.stationNoOfDownTrackList.subList(firstIndex,lastIndex),
                this.stationNoOfDualTrackList.subList(firstIndex,lastIndex))){
            System.out.println("Error in route info");
            return Collections.emptyList();
        }

        return new KBestSchedule().getScheduleNewTrain(pathTemp, scheduler.getStationIdList(),
                scheduler.getStationNameList(), scheduler.getStationDistanceList(),
                scheduler.getStationDirectLineList(), scheduler.getStationNoOfUpPlatformList(),
                scheduler.getStationNoOfDownPlatformList(), scheduler.getStationNoOfDualPlatformList(),
                scheduler.getStationNoOfUpTrackList(), scheduler.getStationNoOfDownTrackList(),
                scheduler.getStationNoOfDualTrackList(), noOfPaths, sourceTime, minDelayBwTrains, avgSpeed,
                this.stopTimeList.subList(firstIndex,lastIndex), pathOldUpTrainSchedule,pathOldDownTrainSchedule,
                trainDay,startDay,startHrs, startMinutes, endDay,endHrs, endMinutes,maxDelayBwStations,
                isSingleDay, false, ratio);
    }

    public void getPathsRecur(String pathTemp, int i, int stationGroupSizeForPart, int noOfPaths, TrainTime sourceTime,
                              int minDelayBwTrains, double avgSpeed, String pathOldUpTrainSchedule,
                              String pathOldDownTrainSchedule, int maxDelayBwStations, boolean isSingleDay,
                              int trainDay, double ratio, Path pathPrevious){
        if(i!=0 && pathPrevious==null){
            System.out.println("Previous path cant be null");
            return;
        }
        if(i>=this.stationIdList.size()){
            return;
        }
        int last;
        if((i+stationGroupSizeForPart+1)<this.stationIdList.size()){
            last = i+stationGroupSizeForPart+1;
        }
        else{
            last = this.stationIdList.size();
        }

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
        List<Path> paths = getSmallPart(pathTemp, i, last, noOfPaths*10, sourceTime, minDelayBwTrains,
                avgSpeed, pathOldUpTrainSchedule,pathOldDownTrainSchedule,startDay,0,0,endDay,
                23, 59, maxDelayBwStations, isSingleDay, trainDay, ratio);

        for(Path path: paths){
            // System.out.println(" i = " + i + " last = " + last );
            if(this.bestAns.size()>=noOfPaths){
                System.out.println("Done returning");
                return;
            }
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

            nodes1 = path.getNodeList();
            weights1 = path.getWeightList();
            if(pathPrevious==null){
                pathPrevious = path;
            }
            Path tempPath=pathPrevious;
            try {
                double tempPathCost;
                tempPath = tempPath.removeLastNode();
                tempPathCost = tempPath.pathCost();
                tempPath = tempPath.removeLastNode();
                for (int i2 = 1; i2 < nodes1.size(); i2++) {
                    tempPath = tempPath.append(nodes1.get(i2), tempPathCost + weights1.get(i2));
                }
            }
            catch (Exception e){
                e.printStackTrace();
                continue;
            }
            if(last<this.stationIdList.size()){
                getPathsRecur(pathTemp, i1, stationGroupSizeForPart,noOfPaths, sourceTime1, minDelayBwTrains,
                        avgSpeed,pathOldUpTrainSchedule, pathOldDownTrainSchedule,maxDelayBwStations, isSingleDay,
                        trainDay, ratio, tempPath);
            }
            else{
                this.bestAns.add(tempPath);
                System.out.println("Path Found : " + tempPath.toString() + " cost: " + tempPath.pathCost());
            }
        }
    }

    public void scheduleByBreaking(String pathTemp, String pathRoute, String pathBestRoute,
                                   String pathOldUpTrainSchedule, String pathOldDownTrainSchedule,
                                   boolean isSingleDay, int trainDay, double ratio){
        if(ratio<1){
            System.out.println("Ratio must be greater than 1.0");
            return;
        }
        Scheduler scheduler = new Scheduler();
        int maxDelayBwStations = 60;
        if(!scheduler.addRouteFromFile(pathRoute)){
            System.out.println("Unable to load route file");
            return;
        }
        ArrayList<Integer> stopTime = new ArrayList<>();
        for(int i=0;i<scheduler.getStationIdList().size();i++) {
            stopTime.add(0);
        }
        // stopTime.set(5, 2.0);
        // stopTime.set(12, 4.0);
        // stopTime.set(22, 4.0);

        this.stationIdList = scheduler.getStationIdList();
        this.stationNameList = scheduler.getStationNameList();
        this.stationDistanceList = scheduler.getStationDistanceList();
        this.stationIsDirectLineList = scheduler.getStationDirectLineList();
        this.stationNoOFUpPlatformList = scheduler.getStationNoOfUpPlatformList();
        this.stationNoOFDownPlatformList = scheduler.getStationNoOfDownPlatformList();
        this.stationNoOFDualPlatformList = scheduler.getStationNoOfDualPlatformList();
        this.stationNoOfUpTrackList = scheduler.getStationNoOfUpTrackList();
        this.stationNoOfDownTrackList = scheduler.getStationNoOfDownTrackList();
        this.stationNoOfDualTrackList = scheduler.getStationNoOfDualTrackList();
        this.stopTimeList = stopTime;

        Double avgSpeed = 80.0;
        int minDelayBwTrains = 3;
        int noOfPaths = 2;
        int stationGroupSizeForPart = 10;

        String pathBestRouteFile = pathBestRoute + File.separator + "Type Break AvgSpeed "+avgSpeed;
        this.bestAns = new PriorityQueue<>(Collections.reverseOrder(Comparator.comparingDouble(Path::pathCost)));

        getPathsRecur(pathTemp,0,stationGroupSizeForPart, noOfPaths,null, minDelayBwTrains,avgSpeed,
                pathOldUpTrainSchedule,pathOldDownTrainSchedule, maxDelayBwStations, isSingleDay, trainDay, ratio,
                null);

        int count=0;
        for(Path path: this.bestAns) {
            System.out.println("Path Found : " + path.toString() + " cost: " + path.pathCost());
            Scheduler.writePathsToFile(path,++count, pathBestRouteFile, stopTime, avgSpeed,
                    scheduler.getStationNameList(), scheduler.getStationDistanceList());
        }
    }
}
