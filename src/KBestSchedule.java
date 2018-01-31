import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class KBestSchedule {

    private Route route;
    private final Map<Integer, Train> trainMap;
    private List<String> stationList;
    private List<List<Node>> nodes;
    private GraphKBestPath graphKBestPath;
    private long edgeCount;

    public KBestSchedule(){
        this.trainMap = new HashMap<>();
    }

    private boolean addRoute(List<String> stationIdList, List<String> stationNameList,
                             List<Double> stationDistanceList, List<Boolean> isDirectLineAvailableList,
                             List<Integer> noOfUpPlatformList, List<Integer> noOfDownPlatformList,
                             List<Integer> noOfDualPlatformList){
        requireNonNull(stationIdList, "Station id list is null.");
        requireNonNull(stationNameList, "Station name list is null.");
        requireNonNull(stationDistanceList, "Station distance list is null.");
        requireNonNull(isDirectLineAvailableList, "Station direct line list is null.");
        requireNonNull(noOfUpPlatformList, "Station no of up platform list is null.");
        requireNonNull(noOfDownPlatformList, "Station no of down platform list is null.");
        requireNonNull(noOfDualPlatformList, "Station no of dual platform list is null.");
        this.route = new Route();
        if(stationIdList.size() != stationNameList.size() || stationNameList.size() != stationDistanceList.size()){
            throw new IllegalArgumentException("Invalid arguments for route");
        }
        for(int i=0;i<stationIdList.size();i++){
            if(!this.route.addStation(stationIdList.get(i), stationNameList.get(i),stationDistanceList.get(i),
                    isDirectLineAvailableList.get(i), noOfUpPlatformList.get(i), noOfDownPlatformList.get(i),
                    noOfDualPlatformList.get(i))){
                throw new RuntimeException("Unable to add station to route");
            }
        }
        return true;
    }

    private boolean addTrain(int trainNo, String trainName){
        requireNonNull(trainName, "Train name is null.");
        this.trainMap.put(trainNo, new Train(trainNo, trainName));
        return true;
    }

    private boolean addStoppageTrain(int trainNo, String stationId, TrainTime arrival, TrainTime departure){
        Train train = this.trainMap.get(trainNo);
        Station station = this.route.getStation(stationId);
        //station==null represents that station is not in the route.
        return train !=null && train.addStoppage(station,arrival,departure);
    }

    private boolean addTrainFromFile(int trainNo, String trainName, String pathTrainSchedule, int trainDay,
                                     boolean isSingleDay){
        try {
            if(!addTrain(trainNo, trainName)){
                return false;
            }
            FileReader fReader = new FileReader(pathTrainSchedule);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            TrainTime arrival, departure=null;
            String stationId;
            while((line = bReader.readLine()) != null) {
                String data[] = line.split("\\s+");
                String stationCode[] = data[0].split("-");
                stationId = stationCode[stationCode.length-1];
                String data1[] =data[1].split(":");
                arrival = new TrainTime(trainDay,Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure!=null && arrival.compareTo(departure)<0 && !isSingleDay){
                    trainDay++;
                    arrival.addDay(1);
                }
                data1 =data[2].split(":");
                departure = new TrainTime(trainDay,Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure.compareTo(arrival)<0 && !isSingleDay){
                    trainDay++;
                    departure.addDay(1);
                }
                if(!addStoppageTrain(trainNo,stationId,arrival,departure)){
                    return false;
                }
            }
            fReader.close();
            bReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean addTrainFromFolder(String pathOldTrainScheduleFolder, int trainDay, boolean isSingleDay){
        if(!isSingleDay && trainDay==7){
            return addTrainFromFolder(pathOldTrainScheduleFolder+ File.separator +
                    "day0", 0, false) &&
                    addTrainFromFolder(pathOldTrainScheduleFolder+ File.separator +
                            "day1", 1, false) &&
                    addTrainFromFolder(pathOldTrainScheduleFolder+ File.separator +
                            "day2", 2, false) &&
                    addTrainFromFolder(pathOldTrainScheduleFolder+ File.separator +
                            "day3", 3, false) &&
                    addTrainFromFolder(pathOldTrainScheduleFolder+ File.separator +
                            "day4", 4, false) &&
                    addTrainFromFolder(pathOldTrainScheduleFolder+ File.separator +
                            "day5", 5, false) &&
                    addTrainFromFolder(pathOldTrainScheduleFolder+ File.separator +
                            "day6", 6, false);
        }

        File[] listOfFiles = new File(pathOldTrainScheduleFolder).listFiles();
        int newTrainNo = 1;
        if(listOfFiles==null) {
            System.out.println("No old trains found");
            return true;
        }

        for (File file: listOfFiles) {
            if (file.isFile()) {
                int trainNo;
                try {
                    trainNo = Integer.parseInt(file.getName().split("\\.")[0]);
                }
                catch (NumberFormatException e) {
                    trainNo = newTrainNo;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                if(!addTrainFromFile(trainNo,file.getName(),file.getPath(), trainDay, isSingleDay)){
                    return false;
                }
            }
        }

        return true;
    }

    private void getNodesFreeSlot(int minDelayBwTrains, TrainTime sourceTime, TrainTime destTime, int startDay,
                                  int startHrs, int startMinutes, int endDay, int endHrs, int endMinutes,
                                  boolean isSingleDay){
        this.nodes = this.route.getFreeSlots(minDelayBwTrains, startDay, startHrs,startMinutes,
                endDay, endHrs,endMinutes, isSingleDay);
        List<Node> nodeSrcList = new ArrayList<>();
        nodeSrcList.add(new Node(sourceTime, "source"));
        this.nodes.add(0,nodeSrcList);
        List<Node> nodeDestList = new ArrayList<>();
        nodeDestList.add(new Node(destTime, "dest"));
        this.nodes.add(nodeDestList);
    }

    private void getStationList(){
        this.stationList = new ArrayList<>(this.route.getStationList());
        this.stationList.add(0,"source");
        this.stationList.add("dest");
    }

    private int isValidEdge(double distanceBwStation, double avgSpeed, double waitTimeStationEnd,
                                Node nodeStart, Node nodeEnd, int maxDelayBwStations, boolean isSingleDay){
        requireNonNull(nodeStart, "start node is null");
        requireNonNull(nodeEnd, "end node is null");
        if(!nodeStart.isValid() || !nodeEnd.isValid()){
            return -6;
        }
        if(nodeStart.getTime()==null || nodeEnd.getTime()==null){
            return -7;
        }

        int delay = Scheduler.ceilOfDecimal((((distanceBwStation)/avgSpeed )*60));
        int timeNodeStart = nodeStart.getTime().getValue();
        int timeNodeEnd = nodeEnd.getTime().getValue();
        int timeEarliestToReach = timeNodeStart + delay;
        int timeEarliestToDepart = timeEarliestToReach + (int)(waitTimeStationEnd/1);
        int timeMaxToDepart = timeEarliestToDepart + maxDelayBwStations;

        if(timeNodeEnd<timeNodeStart){
            timeNodeEnd += isSingleDay?1440:10080;
        }

        if(timeEarliestToDepart<=timeNodeEnd && timeNodeEnd <=timeMaxToDepart){
            int minNumberOfPlatformRequired = 1;
            boolean isUpDirection = false;

            if(!nodeStart.getStationId().equalsIgnoreCase("source") &&
                    !nodeEnd.getStationId().equalsIgnoreCase("dest") ) {
                for (Train train : this.trainMap.values()) {
                    isUpDirection = train.isUpDirection();
                    TrainTime oldTrainDeptStationStart = train.getDept(nodeStart.getStationId());
                    TrainTime oldTrainArrStationEnd = train.getArr(nodeEnd.getStationId());
                    TrainTime oldTrainDeptStationEnd = train.getDept(nodeEnd.getStationId());

                    if(oldTrainArrStationEnd==null || oldTrainDeptStationEnd==null){
                        //different route train and next station is different
                        continue;
                    }

                    int timeOldTrainStationEndArr = oldTrainArrStationEnd.getValue();
                    int timeOldTrainStationEndDept = oldTrainDeptStationEnd.getValue();

                    if(timeOldTrainStationEndDept<timeOldTrainStationEndArr){
                        timeOldTrainStationEndDept += isSingleDay?1440:10080;
                    }

                    if(!(this.route.getStation(nodeEnd.getStationId()).isDirectLineAvailable() &&
                            timeOldTrainStationEndArr==timeOldTrainStationEndDept)){
                        //need to check availability of platform
                        if((timeOldTrainStationEndDept>=timeEarliestToReach && timeOldTrainStationEndDept<=timeNodeEnd) ||
                                (timeNodeEnd <= timeOldTrainStationEndDept && timeNodeEnd >=timeOldTrainStationEndArr)){
                            minNumberOfPlatformRequired++;
                        }
                    }

                    if(oldTrainDeptStationStart==null){
                        //different route train but next station is same
                        continue;
                    }

                    int timeOldTrainStationStartDept = oldTrainDeptStationStart.getValue();

                    if(timeOldTrainStationEndArr<timeOldTrainStationStartDept){
                        timeOldTrainStationEndArr += isSingleDay?1440:10080;
                    }

                    if((timeOldTrainStationStartDept<=timeNodeStart && timeOldTrainStationEndArr >=timeEarliestToReach)||
                            (timeOldTrainStationStartDept>=timeNodeStart && timeOldTrainStationEndArr <=timeEarliestToReach)){
                        return -2;
                    }
                }
            }
            else if(nodeStart.getStationId().equalsIgnoreCase("source")){
                for (Train train : this.trainMap.values()) {
                    isUpDirection = train.isUpDirection();
                    TrainTime oldTrainArrStationEnd = train.getArr(nodeEnd.getStationId());
                    TrainTime oldTrainDeptStationEnd = train.getDept(nodeEnd.getStationId());
                    if(oldTrainArrStationEnd==null || oldTrainDeptStationEnd==null){
                        ////different route train
                        continue;
                    }

                    if(!(this.route.getStation(nodeEnd.getStationId()).isDirectLineAvailable() &&
                            oldTrainArrStationEnd.equals(oldTrainDeptStationEnd))){
                        //need to check availability of platform
                        int timeOldTrainStationEndArr = oldTrainArrStationEnd.getValue();
                        int timeOldTrainStationEndDept = oldTrainDeptStationEnd.getValue();

                        if(timeOldTrainStationEndDept<timeOldTrainStationEndArr){
                            timeOldTrainStationEndDept += isSingleDay?1440:10080;
                        }

                        if((timeOldTrainStationEndDept>=timeEarliestToReach && timeOldTrainStationEndDept<=timeNodeEnd) ||
                                (timeNodeEnd <= timeOldTrainStationEndDept && timeNodeEnd >=timeOldTrainStationEndArr)){
                            minNumberOfPlatformRequired++;
                        }
                    }
                }
            }
            else if(nodeEnd.getStationId().equalsIgnoreCase("dest")){
                return 1;
            }

            if(minNumberOfPlatformRequired<=(isUpDirection?this.route.getStation(nodeEnd.getStationId()).getNoOfUpPlatform():
                    this.route.getStation(nodeEnd.getStationId()).getNoOfDownPlatform())) {
                return 2;
            }
            else{
                return -3;
            }
        }
        else if(timeEarliestToDepart>timeNodeEnd){
            return -4;
        }
        else{
            return -5;
        }
    }

    private boolean addEdgeBwStations(int i, boolean sourceDestPath,int maxDelayBwStations,
                                      double distanceBwStation, double avgSpeed, double waitTimeStationEnd,
                                      boolean isSingleDay){
        boolean addedFirstEdge = false;
        Node nodeStart;
        Node nodeEnd;
        if(this.nodes.get(i).isEmpty()) {
            System.out.println("No path found as no available slot for station "+ this.stationList.get(i));
            return false;
        }
        if(this.nodes.get(i+1).isEmpty()) {
            System.out.println("No path found as no available slot for station "+ this.stationList.get(i+1));
            return false;
        }

        if(!this.nodes.get(i).get(0).getStationId().equalsIgnoreCase(this.stationList.get(i)) ||
                !this.nodes.get(i+1).get(0).getStationId().equalsIgnoreCase(this.stationList.get(i+1))){
            System.out.println("Invalid path Info.");
            return false;
        }

        if(i==0 && !this.graphKBestPath.addMultipleNode(this.nodes.get(i))) {
            System.out.println("Some error occurred in adding nodes");
            return false;
        }
        if(!this.graphKBestPath.addMultipleNode(this.nodes.get(i+1))){
            System.out.println("Some error occurred in adding nodes");
            return false;
        }

        for(int j=0;j<this.nodes.get(i).size();j++) {
            if(!this.nodes.get(i).get(j).isValid()){
                continue;
            }
            nodeStart = this.nodes.get(i).get(j);
            int loopCount = maxDelayBwStations;
            if(loopCount>this.nodes.get(i+1).size() || nodeStart.getStationId().equalsIgnoreCase("source")){
                loopCount = this.nodes.get(i+1).size();
            }

            for(int countMaxDelay= 0;countMaxDelay<=loopCount; countMaxDelay++) {
                int k = Math.floorMod(j+countMaxDelay,this.nodes.get(i+1).size());
                if(!this.nodes.get(i+1).get(k).isValid()){
                    continue;
                }
                nodeEnd = this.nodes.get(i+1).get(k);
                if(nodeStart.getTime()==null || nodeEnd.getTime()==null){
                    if(sourceDestPath &&
                            this.graphKBestPath.addEdge(new Edge(nodeStart, nodeEnd,0))){
                        this.edgeCount++;
                    }
                    else if(!sourceDestPath &&
                            this.graphKBestPath.addEdge(new Edge(nodeEnd, nodeStart, 0))){
                        this.edgeCount++;
                    }
                }
                else if(nodeStart.getStationId().equalsIgnoreCase("source")){
                    boolean validEdge = true;
                    int codeValidEdge;
                    do{
                        codeValidEdge = isValidEdge(distanceBwStation, avgSpeed, waitTimeStationEnd, nodeStart, nodeEnd,
                                maxDelayBwStations, isSingleDay);
                        if(codeValidEdge==-1){
                            return false;
                        }
                        else if(codeValidEdge==-5){
                            validEdge = false;
                            break;
                        }
                        if(addedFirstEdge){
                            validEdge = false;
                            break;
                        }
                        if(nodeStart.getTime().compareTo(nodeEnd.getTime())>0){
                            validEdge = false;
                            break;
                        }
                        nodeStart.getTime().addMinutes(1);
                        if(nodeStart.getTime().compareTo(new TrainTime(6,23,59))==0){
                            System.out.println("No place on source station platform");
                            return false;
                        }
                    } while(codeValidEdge<0);

                    if(codeValidEdge==-5){
                        break;
                    }

                    if(validEdge) {
                        addedFirstEdge = true;
                        int edgeCost = nodeEnd.getTime().compareTo(nodeStart.getTime());
                        if(edgeCost<0){
                            if(isSingleDay){
                                edgeCost+= 1440; //60*24
                            }
                            else {
                                edgeCost += 10080; //60*24*7
                            }
                        }
                        if(edgeCost >= 0 && sourceDestPath && this.graphKBestPath.addEdge(new Edge(
                                nodeStart, nodeEnd,0))){
                            this.edgeCount++;
                        }
                        else if(edgeCost >= 0 && !sourceDestPath && this.graphKBestPath.addEdge(new Edge(
                                nodeEnd, nodeStart, 0))){
                            this.edgeCount++;
                        }
                    }
                }
                else{
                    int codeValidEdge = isValidEdge(distanceBwStation, avgSpeed, waitTimeStationEnd, nodeStart,
                            nodeEnd, maxDelayBwStations, isSingleDay);
                    if(codeValidEdge==-1){
                        return false;
                    }
                    else if(codeValidEdge==-5){
                        break;
                    }
                    if(codeValidEdge>0){
                        int edgeCost = nodeEnd.getTime().compareTo(nodeStart.getTime());
                        if(edgeCost<0){
                            if(isSingleDay){
                                edgeCost+= 1440; //60*24
                            }
                            else {
                                edgeCost += 10080; //60*24*7
                            }
                        }
                        if(edgeCost >= 0 && sourceDestPath && this.graphKBestPath.addEdge(new Edge(
                                nodeStart, nodeEnd,edgeCost))){
                            this.edgeCount++;
                        }
                        else if(edgeCost >= 0 && !sourceDestPath && this.graphKBestPath.addEdge(new Edge(
                                nodeEnd, nodeStart, edgeCost))){
                            this.edgeCount++;
                        }
                    }
                }
            }
        }
        return true;
    }

    public List<Path>
    scheduleKBestPathOptimized(String pathTemp, int noOfPaths, TrainTime sourceTime, TrainTime destTime,
                               int maxDelayBwStations, int minDelayBwTrains, List<Double> stopTime,
                               double avgSpeed, int startDay, int startHrs, int startMinutes, int endDay,
                               int endHrs, int endMinutes, boolean sourceDestPath, boolean isSingleDay,
                               boolean usePreviousComputation){
        try{
            this.graphKBestPath = new GraphKBestPath(usePreviousComputation, pathTemp);
            if(!usePreviousComputation) {
                getStationList();
                getNodesFreeSlot(minDelayBwTrains, sourceTime, destTime, startDay, startHrs, startMinutes,
                        endDay, endHrs, endMinutes, isSingleDay);
                if (this.nodes == null || this.stationList == null || this.nodes.isEmpty() || this.stationList.isEmpty()) {
                    System.out.println("Error in loading data");
                    return Collections.emptyList();
                }
                if (this.nodes.size() != this.stationList.size()) {
                    System.out.println("Invalid nodes in graph... exiting");
                    return Collections.emptyList();
                }
                this.edgeCount = 0;
                System.out.println("Station size: " + (this.nodes.size() - 2));
                System.out.println("Initializing graph");
                double distanceStationStart;
                double distanceStationEnd = 0.0;
                double waitTimeStationEnd = 0.0;
                double distanceBwStation;

                for (int i = 0; i < this.stationList.size() - 1; i++) {
                    Scheduler.getRuntimeMemory();
                    distanceStationStart = distanceStationEnd;
                    if (i < this.stationList.size() - 2) {
                        distanceStationEnd = this.route.getStation(this.stationList.get(i + 1)).getDistance();
                        waitTimeStationEnd = stopTime.get(i);
                    }
                    if (i == 0) {
                        distanceBwStation = 0;
                    } else {
                        distanceBwStation = distanceStationEnd - distanceStationStart;
                    }
                    if (!addEdgeBwStations(i, sourceDestPath, maxDelayBwStations, distanceBwStation, avgSpeed,
                            waitTimeStationEnd, isSingleDay)) {
                        System.out.println("Some error occurred in adding edges.");
                        return Collections.emptyList();
                    }
                }
                if (!this.graphKBestPath.flushEdgeList()) {
                    System.out.println("Some error occurred in graph");
                    return Collections.emptyList();
                }
                System.out.println("Edge size: " + this.edgeCount);
                // System.out.println(this.graphKBestPath.toString());
            }
            List<Path> paths;
            KShortestPathFinder kShortestPathFinder = new KShortestPathFinder();

            if(sourceDestPath) {
                paths = kShortestPathFinder.findShortestPaths(new Node(sourceTime, "source"),
                        new Node(destTime, "dest"), this.graphKBestPath, noOfPaths);
            }
            else{
                paths = kShortestPathFinder.findShortestPaths(new Node(destTime, "dest"),
                        new Node(sourceTime, "source"), this.graphKBestPath, noOfPaths);
            }

            if(!this.graphKBestPath.disconnect()){
                System.out.println("Some error occurred with database");
            }
            this.graphKBestPath = null;
            if(paths==null || paths.isEmpty()){
                TrainTime tempTrainTime;
                if(isSingleDay){
                    tempTrainTime = new TrainTime(startDay,23,59);
                }
                else{
                    tempTrainTime = new TrainTime(6,23,59);
                }
                if(sourceTime!=null && sourceTime.compareTo(tempTrainTime)>=0){
                    System.out.println(sourceTime + " No path found in this iteration");
                    return Collections.emptyList();
                }
                if(destTime!=null && destTime.compareTo(tempTrainTime)>=0){
                    System.out.println(sourceTime + " No path found in this iteration");
                    return Collections.emptyList();
                }
                boolean changedTimings=false;
                if(sourceTime!=null){
                    sourceTime.addMinutes(30);
                    changedTimings = true;
                }
                if(destTime!=null){
                    destTime.addMinutes(30);
                    changedTimings = true;
                }
                if(changedTimings) {
                    return scheduleKBestPathOptimized(pathTemp, noOfPaths, sourceTime, destTime, maxDelayBwStations,
                            minDelayBwTrains, stopTime, avgSpeed, startDay, startHrs, startMinutes,
                            endDay, endHrs, endMinutes, sourceDestPath, isSingleDay, false);
                }
                else{
                    return Collections.emptyList();
                }
            }
            return paths;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public List<Path> getScheduleNewTrain(String pathTemp, List<String> stationIdList, List<String> stationNameList,
                                          List<Double> stationDistanceList,
                                          List<Boolean> isDirectLineAvailableList,
                                          List<Integer> noOfUpPlatformList, List<Integer> noOfDownPlatformList,
                                          List<Integer> noOfDualPlatformList,
                                          int noOfPaths, TrainTime sourceTime, TrainTime destTime,
                                          int minDelayBwTrains, double avgSpeed , List<Double> stopTime,
                                          String pathOldTrainSchedule, int trainDay, int startDay, int startHrs,
                                          int startMinutes, int endDay, int endHrs, int endMinutes,
                                          int maxDelayBwStations, boolean isSingleDay, boolean usePreviousComputation) {
        TrainTime.updateIsSingleDay(isSingleDay);
        long milli = new Date().getTime();
        System.out.println("*********************************************************");
        System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        System.out.println("Avg speed: " + avgSpeed);
        if(!addRoute(stationIdList,stationNameList,stationDistanceList,isDirectLineAvailableList,
                noOfUpPlatformList, noOfDownPlatformList, noOfDualPlatformList)
                || !addTrainFromFolder(pathOldTrainSchedule, trainDay, isSingleDay)){
            return Collections.emptyList();
        }
        // System.out.println(this.route.toString());
        // System.out.println(this.trainMap.values().toString());
        if(stopTime.size() != this.route.getNumberOfStation()){
            System.out.println("Please give stop time for every station in route. if it does not stop at " +
                    "any particular station, give stop time as 0.");
            return Collections.emptyList();
        }

        List<Path> paths;
        if(destTime!=null){
            paths= scheduleKBestPathOptimized(pathTemp, noOfPaths, sourceTime, destTime, maxDelayBwStations, minDelayBwTrains,
                    stopTime,avgSpeed, startDay, startHrs,startMinutes, endDay, endHrs,endMinutes, false,
                    isSingleDay, usePreviousComputation);
            List<Path> pathsTemp = new ArrayList<>();
            for(Path path: paths){
                pathsTemp.add(path.reverse());
            }
            paths = pathsTemp;
        }
        else{
            paths= scheduleKBestPathOptimized(pathTemp, noOfPaths, sourceTime, null, maxDelayBwStations,
                    minDelayBwTrains, stopTime,avgSpeed, startDay, startHrs,startMinutes,
                    endDay, endHrs,endMinutes,true, isSingleDay, usePreviousComputation);
        }
        milli = new Date().getTime() - milli;
        System.out.println("Duration: " + milli + "ms");
        if(paths==null || paths.isEmpty()){
            System.out.println("No path found in any iteration");
            return Collections.emptyList();
        }
        return paths;
    }
}
