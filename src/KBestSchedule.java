import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class KBestSchedule {

    private Route route;
    private final Map<String, Train> upTrainMap;
    private final Map<String, Train> downTrainMap;
    private List<String> stationList;
    private List<List<Node>> nodes;
    private GraphKBestPath graphKBestPath;
    private long edgeCount;

    public KBestSchedule(){
        this.upTrainMap = new HashMap<>();
        this.downTrainMap = new HashMap<>();
    }

    private boolean addRoute(List<String> stationIdList, List<String> stationNameList,
                             List<Double> stationDistanceList, List<Boolean> isDirectLineAvailableList,
                             List<Integer> noOfUpPlatformList, List<Integer> noOfDownPlatformList,
                             List<Integer> noOfDualPlatformList, List<Integer> noOfUpTrackList,
                             List<Integer> noOfDownTrackList, List<Integer> noOfDualTrackList){
        requireNonNull(stationIdList, "Station id list is null.");
        requireNonNull(stationNameList, "Station name list is null.");
        requireNonNull(stationDistanceList, "Station distance list is null.");
        requireNonNull(isDirectLineAvailableList, "Station direct line list is null.");
        requireNonNull(noOfUpPlatformList, "Station no of up platform list is null.");
        requireNonNull(noOfDownPlatformList, "Station no of down platform list is null.");
        requireNonNull(noOfDualPlatformList, "Station no of dual platform list is null.");
        this.route = new Route();

        int sizeStation = stationIdList.size();
        if(stationNameList.size() != sizeStation || stationDistanceList.size() != sizeStation ||
                isDirectLineAvailableList.size() != sizeStation || noOfUpPlatformList.size() != sizeStation ||
                noOfDownPlatformList.size() != sizeStation || noOfDualPlatformList.size() != sizeStation){
            throw new IllegalArgumentException("Invalid arguments for route");
        }
        for(int i=0;i<sizeStation;i++){
            if(!this.route.addStation(stationIdList.get(i), stationNameList.get(i),stationDistanceList.get(i),
                    isDirectLineAvailableList.get(i), noOfUpPlatformList.get(i), noOfDownPlatformList.get(i),
                    noOfDualPlatformList.get(i), noOfUpTrackList.get(i), noOfDownTrackList.get(i),
                    noOfDualTrackList.get(i))){
                throw new RuntimeException("Unable to add station to route");
            }
        }
        return true;
    }

    private boolean addUpTrain(int trainDay, int trainNo, String trainName){
        requireNonNull(trainName, "Train name is null.");
        this.upTrainMap.put(trainDay+":"+trainNo, new Train(trainNo, trainName));
        return true;
    }

    private boolean addDownTrain(int trainDay, int trainNo, String trainName){
        requireNonNull(trainName, "Train name is null.");
        this.downTrainMap.put(trainDay+":"+trainNo, new Train(trainNo, trainName, false));
        return true;
    }

    private boolean addStoppageUpTrain(int trainDay, int trainNo, String stationId, TrainTime arrival, TrainTime departure){
        Train train = this.upTrainMap.getOrDefault(trainDay+":" +trainNo, null);
        if(train==null){
            System.out.println("Train not found "+ trainNo +" originating day "+ trainDay);
            return false;
        }
        Station station = this.route.getStation(stationId);
        //station==null represents that station is not in the route.
        return train.addStoppage(station,arrival,departure);
    }

    private boolean addStoppageDownTrain(int trainDay, int trainNo, String stationId, TrainTime arrival, TrainTime departure){
        Train train = this.downTrainMap.getOrDefault(trainDay+":" +trainNo, null);
        if(train==null){
            System.out.println("Train not found "+ trainNo +" originating day "+ trainDay);
            return false;
        }
        Station station = this.route.getStation(stationId);
        //station==null represents that station is not in the route.
        return train.addStoppage(station,arrival,departure);
    }

    private boolean addTrainFromFile(int trainNo, String trainName, String pathTrainSchedule, int trainDay,
                                     boolean isSingleDay, boolean isUpDirection){
        int stoppageDay = trainDay;
        try {
            if(isUpDirection && !addUpTrain(trainDay, trainNo, trainName)){
                return false;
            }
            else if(!addDownTrain(trainDay, trainNo, trainName)){
                return false;
            }
            FileReader fReader = new FileReader(pathTrainSchedule);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            TrainTime arrival, departure=null;
            String stationId;
            String data[];
            String data1[];
            while((line = bReader.readLine()) != null) {
                data = line.split("\\s+");
                stationId = data[0].trim().replaceAll(".*-", "");
                data1 =data[1].split(":");
                arrival = new TrainTime(stoppageDay,Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure!=null && arrival.compareTo(departure)<0 && !isSingleDay){
                    arrival.addDay(1);
                    stoppageDay = arrival.day;
                }
                data1 =data[2].split(":");
                departure = new TrainTime(stoppageDay,Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure.compareTo(arrival)<0 && !isSingleDay){
                    departure.addDay(1);
                    stoppageDay = departure.day;
                }
                if(isUpDirection && !addStoppageUpTrain(trainDay, trainNo,stationId,arrival,departure)){
                    bReader.close();
                    fReader.close();
                    return false;
                }
                else if(!addStoppageDownTrain(trainDay, trainNo,stationId,arrival,departure)){
                    bReader.close();
                    fReader.close();
                    return false;
                }
            }
            bReader.close();
            fReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean addTrainFromFolder(String pathOldTrainScheduleFolder, int trainDay, boolean isSingleDay,
                                       boolean isUpDirection){
        if(!isSingleDay){
            if(trainDay>=7 || trainDay<0) {
                return addTrainFromFolder(pathOldTrainScheduleFolder + File.separator +
                        "day0", 0, false, isUpDirection) &&
                        addTrainFromFolder(pathOldTrainScheduleFolder + File.separator +
                                "day1", 1, false, isUpDirection) &&
                        addTrainFromFolder(pathOldTrainScheduleFolder + File.separator +
                                "day2", 2, false, isUpDirection) &&
                        addTrainFromFolder(pathOldTrainScheduleFolder + File.separator +
                                "day3", 3, false, isUpDirection) &&
                        addTrainFromFolder(pathOldTrainScheduleFolder + File.separator +
                                "day4", 4, false, isUpDirection) &&
                        addTrainFromFolder(pathOldTrainScheduleFolder + File.separator +
                                "day5", 5, false, isUpDirection) &&
                        addTrainFromFolder(pathOldTrainScheduleFolder + File.separator +
                                "day6", 6, false, isUpDirection);
            }
            // else{
            //     System.out.println("Invalid Train day. Value should be >=7;");
            //     return false;
            // }
        }

        File[] listOfFiles = new File(pathOldTrainScheduleFolder).listFiles();
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
                catch (Exception e) {
                    System.out.println("File name should be train Number.");
                    System.out.println("Skipping file : " + file.getPath());
                    e.printStackTrace();
                    continue;
                }
                if(!addTrainFromFile(trainNo,file.getName(),file.getPath(), trainDay, isSingleDay, isUpDirection)){
                    return false;
                }
            }
        }

        return true;
    }

    private void getNodesFreeSlot(TrainTime sourceTime, TrainTime destTime, int startDay,
                                  int startHrs, int startMinutes, int endDay, int endHrs, int endMinutes,
                                  boolean isSingleDay){
        this.nodes = this.route.getFreeSlots(new TrainTime(startDay, startMinutes, startHrs),
                new TrainTime(endDay, endHrs, endMinutes), isSingleDay);
        List<Node> nodeSrcList = new ArrayList<>();
        nodeSrcList.add(new Node(sourceTime, "source"));
        this.nodes.add(0,nodeSrcList);
        List<Node> nodeDestList = new ArrayList<>();
        nodeDestList.add(new Node(destTime, "dest"));
        this.nodes.add(nodeDestList);
    }

    private void getStationList(){
        this.stationList = this.route.getStationList();
        this.stationList.add(0,"source");
        this.stationList.add("dest");
    }

    private int isValidEdge(int delayBwStation, int waitTimeStationEnd, Node nodeStart, Node nodeEnd,
                            int maxDelayBwStations, boolean isSingleDay, int minDelayBwTrains){
        requireNonNull(nodeStart, "start node is null");
        requireNonNull(nodeEnd, "end node is null");

        if(nodeStart.getTime()==null || nodeEnd.getTime()==null){
            return -1;
        }

        int timeNodeStart = nodeStart.getTime().getValue();
        int timeNodeEnd = nodeEnd.getTime().getValue();
        int timeEarliestToReach = timeNodeStart + delayBwStation;
        int timeEarliestToDepart = timeEarliestToReach + waitTimeStationEnd;
        int timeMaxToDepart = timeEarliestToDepart + maxDelayBwStations;

        if(timeNodeEnd<timeNodeStart){
            timeNodeEnd += isSingleDay?1440:10080;
        }

        if(timeEarliestToDepart<=timeNodeEnd && timeNodeEnd <=timeMaxToDepart){
            int minNumberOfUpPlatformRequired = 1;
            int minNumberOfUpTrackRequired = 1;

            boolean isDirectLineAvailable = this.route.getStation(nodeEnd.getStationId()).isDirectLineAvailable();

            if(!nodeStart.getStationId().equalsIgnoreCase("source") &&
                    !nodeEnd.getStationId().equalsIgnoreCase("dest") ) {
                for (Train train : this.upTrainMap.values()) {
                    TrainTime oldTrainDeptStationStart = train.getDept(nodeStart.getStationId());
                    TrainTime oldTrainArrStationEnd = train.getArr(nodeEnd.getStationId());
                    TrainTime oldTrainDeptStationEnd = train.getDept(nodeEnd.getStationId());

                    if(oldTrainArrStationEnd==null || oldTrainDeptStationEnd==null){
                        //different route train and next station is different
                        // System.out.println("Train goes to different route after station" + nodeStart.toString());
                        continue;
                    }

                    int timeOldTrainStationEndArr = oldTrainArrStationEnd.getValue();
                    int timeOldTrainStationEndDept = oldTrainDeptStationEnd.getValue();

                    if(timeOldTrainStationEndDept<timeOldTrainStationEndArr){
                        timeOldTrainStationEndDept += isSingleDay?1440:10080;
                    }

                    if(!( isDirectLineAvailable && timeOldTrainStationEndArr==timeOldTrainStationEndDept)){
                        //need to check availability of platform
                        if((timeOldTrainStationEndDept>=timeEarliestToReach && timeOldTrainStationEndDept<=timeNodeEnd) ||
                                (timeNodeEnd <= timeOldTrainStationEndDept && timeNodeEnd >=timeOldTrainStationEndArr)){
                            minNumberOfUpPlatformRequired++;
                        }
                    }

                    if(oldTrainDeptStationStart==null){
                        //different route train but next station is same
                        // System.out.println("Train comes from different route before station" + nodeEnd.toString());
                        continue;
                    }

                    int timeOldTrainStationStartDept = oldTrainDeptStationStart.getValue();
                    if(timeOldTrainStationEndArr<timeOldTrainStationStartDept){
                        timeOldTrainStationEndArr += isSingleDay?1440:10080;
                    }

                    if(timeNodeStart<(timeOldTrainStationStartDept + minDelayBwTrains) &&
                            timeNodeStart>(timeOldTrainStationStartDept-minDelayBwTrains)){
                        // System.out.println("Min delay constraint" + oldTrainDeptStationStart);
                        // return -8;
                        minNumberOfUpTrackRequired++;
                        continue;
                    }

                    if(timeEarliestToReach<(timeOldTrainStationEndArr+minDelayBwTrains) &&
                            timeEarliestToReach>(timeOldTrainStationEndArr-minDelayBwTrains)){
                        // System.out.println("Min delay constraint" + oldTrainArrStationEnd);
                        // return -9;
                        minNumberOfUpTrackRequired++;
                        continue;
                    }

                    if((timeOldTrainStationStartDept<=timeNodeStart && timeOldTrainStationEndArr >=timeEarliestToReach)||
                            (timeOldTrainStationStartDept>=timeNodeStart && timeOldTrainStationEndArr <=timeEarliestToReach)){
                        // return -2;
                        minNumberOfUpTrackRequired++;
                    }
                }
            }
            else if(nodeStart.getStationId().equalsIgnoreCase("source")){
                for (Train train : this.upTrainMap.values()) {
                    TrainTime oldTrainArrStationEnd = train.getArr(nodeEnd.getStationId());
                    TrainTime oldTrainDeptStationEnd = train.getDept(nodeEnd.getStationId());
                    if(oldTrainArrStationEnd==null || oldTrainDeptStationEnd==null){
                        ////different route train
                        // System.out.println("Train goes to different route after station" + nodeStart.toString());
                        continue;
                    }
                    int timeOldTrainStationEndArr = oldTrainArrStationEnd.getValue();
                    int timeOldTrainStationEndDept = oldTrainDeptStationEnd.getValue();
                    if(timeOldTrainStationEndDept<timeOldTrainStationEndArr){
                        timeOldTrainStationEndDept += isSingleDay?1440:10080;
                    }
                    if(!(isDirectLineAvailable && oldTrainArrStationEnd.equals(oldTrainDeptStationEnd))){
                        //need to check availability of platform

                        if((timeOldTrainStationEndDept>=timeEarliestToReach && timeOldTrainStationEndDept<=timeNodeEnd) ||
                                (timeNodeEnd <= timeOldTrainStationEndDept && timeNodeEnd >=timeOldTrainStationEndArr)){
                            minNumberOfUpPlatformRequired++;
                        }
                    }
                    if(timeEarliestToReach<(timeOldTrainStationEndArr+minDelayBwTrains) &&
                            timeEarliestToReach>(timeOldTrainStationEndArr-minDelayBwTrains)){
                        // System.out.println("Min delay constraint" + oldTrainArrStationEnd);
                        // return -9;
                        minNumberOfUpTrackRequired++;
                    }
                }
            }
            else if(nodeEnd.getStationId().equalsIgnoreCase("dest")){
                return 1;
            }


            if(minNumberOfUpTrackRequired>(this.route.getStation(nodeEnd.getStationId()).getNoOfUpTrack() +
                    this.route.getStation(nodeEnd.getStationId()).getNoOfDualTrack())){
                if(nodeStart.getStationId().equalsIgnoreCase("tlam")){
                    System.out.print("Rejected due to track const " + nodeStart.toString() + " -> " + nodeEnd.toString()) ;
                    System.out.println(" Available: " + (this.route.getStation(nodeEnd.getStationId()).getNoOfUpTrack() +
                            this.route.getStation(nodeEnd.getStationId()).getNoOfDualTrack()));
                }
                // return -2;
            }

            if((minNumberOfUpPlatformRequired<=(this.route.getStation(nodeEnd.getStationId()).getNoOfUpPlatform() +
                    this.route.getStation(nodeEnd.getStationId()).getNoOfDualPlatform()))) {
                return 2;
            }
            else{
                if(nodeStart.getStationId().equalsIgnoreCase("tlam")) {
                    System.out.print("Rejected due to platform const " + nodeStart.toString() + " -> " + nodeEnd.toString());
                    System.out.println(" Available: " + (this.route.getStation(nodeEnd.getStationId()).getNoOfUpPlatform() +
                            this.route.getStation(nodeEnd.getStationId()).getNoOfDualPlatform()));
                }
                return -3;
            }
        }
        else if(timeEarliestToDepart>timeNodeEnd){
            // System.out.println("Rejected as earliest to depart is more than node time :" + nodeStart.toString() +
            //         " -> " + nodeEnd.toString());
            return -4;
        }
        else{
            return -5;
        }
    }

    private boolean addEdgeBwStations(int i,int maxDelayBwStations, int delayBwStation, int waitTimeStationEnd,
                                      boolean isSingleDay, int minDelayBwTrains){
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
            System.out.println("Some error occurred in adding source nodes");
            return false;
        }
        if(!this.graphKBestPath.addMultipleNode(this.nodes.get(i+1))){
            System.out.println("Some error occurred in adding nodes " + this.nodes.get(i+1).get(0).toString());
            return false;
        }

        System.out.println("Adding edge bw stations " + this.stationList.get(i) +"->" + this.stationList.get(i+1) );

        for(int j=0;j<this.nodes.get(i).size();j++) {
            nodeStart = this.nodes.get(i).get(j);

            int countLoopMax = maxDelayBwStations;
            if(countLoopMax>this.nodes.get(i+1).size() || nodeStart.getStationId().toLowerCase().contains("source")){
                countLoopMax = this.nodes.get(i+1).size();
            }

            for(int countLoop= 0;countLoop<countLoopMax; countLoop++) {
                int k = Math.floorMod(j+countLoop,this.nodes.get(i+1).size());
                nodeEnd = this.nodes.get(i+1).get(k);
                if(nodeStart.getTime()==null || nodeEnd.getTime()==null){
                    if(this.graphKBestPath.addEdge(new Edge(nodeStart, nodeEnd,0))){
                        this.edgeCount++;
                    }
                    else{
                        System.err.println("Some error occurred in adding edge bw " + nodeStart.toString() +
                                " and " + nodeEnd.toString() + " cost 0");
                    }
                }
                else if(nodeStart.getStationId().equalsIgnoreCase("source")){
                    boolean validEdge = true;
                    int codeValidEdge;
                    TrainTime tempTrainTime = isSingleDay?new TrainTime(nodeStart.getTime().day,23,55):
                            new TrainTime(6,23,54);

                    do{
                        codeValidEdge = isValidEdge(delayBwStation, waitTimeStationEnd, nodeStart, nodeEnd,
                                maxDelayBwStations, isSingleDay, minDelayBwTrains);
                        if(codeValidEdge>0){
                            break;
                        }
                        else if(codeValidEdge==-1 ||codeValidEdge==-4 || codeValidEdge==-5 ){
                            validEdge = false;
                            break;
                        }
                        else if(addedFirstEdge){
                            validEdge = false;
                            break;
                        }
                        else if(nodeStart.getTime().compareTo(nodeEnd.getTime())>0){
                            validEdge = false;
                            break;
                        }
                        else if(nodeStart.getTime().compareTo(tempTrainTime)>=0){
                            System.out.println("No place on source station platform");
                            return false;
                        }
                        nodeStart.getTime().addMinutes(1);
                    } while(codeValidEdge<0);

                    if(validEdge) {
                        addedFirstEdge = true;
                        // int edgeCost = nodeEnd.getTime().compareTo(nodeStart.getTime());
                        // if(edgeCost<0){
                        //     edgeCost+= isSingleDay?1440:10080;
                        // }
                        //edge cost 0 means choose best path in between  source time and max delay allowed.
                        if(this.graphKBestPath.addEdge(new Edge(nodeStart, nodeEnd,0))){
                            this.edgeCount++;
                        }
                        else{
                            System.err.println("Some error occurred in adding edge bw " + nodeStart.toString() +
                                    " and " + nodeEnd.toString());
                        }
                    }
                }
                else{
                    int codeValidEdge = isValidEdge(delayBwStation, waitTimeStationEnd, nodeStart,
                            nodeEnd, maxDelayBwStations, isSingleDay, minDelayBwTrains);
                    if(codeValidEdge==-5){
                        break;
                    }
                    else if(codeValidEdge>0){
                        int edgeCost = nodeEnd.getTime().compareTo(nodeStart.getTime());
                        if(edgeCost<0){
                            edgeCost+= isSingleDay?1440:10080;
                        }
                        if(this.graphKBestPath.addEdge(new Edge(nodeStart, nodeEnd,edgeCost))){
                            this.edgeCount++;
                        }
                        else{
                            System.err.println("Some error occurred in adding edge bw " + nodeStart.toString() +
                                    " and " + nodeEnd.toString() + " cost " + edgeCost);
                        }
                    }
                }
            }
        }
        return true;
    }

    public List<Path>
    scheduleKBestPathOptimized(String pathTemp, int noOfPaths, TrainTime sourceTime, TrainTime destTime,
                               int maxDelayBwStations, int minDelayBwTrains, List<Integer> stopTime,
                               double avgSpeed, int startDay, int startHrs, int startMinutes, int endDay,
                               int endHrs, int endMinutes, boolean isSingleDay,
                               boolean usePreviousComputation, List<Double> maxCostList){
        try{
            this.graphKBestPath = new GraphKBestPath(usePreviousComputation, pathTemp);
            if(!usePreviousComputation) {
                getStationList();
                getNodesFreeSlot(sourceTime, destTime, startDay, startHrs, startMinutes,
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
                int waitTimeStationEnd;
                double distanceBwStation;
                int delayBwStation;
                double delaySecondsAdded=0;
                double delayBwStationActual;

                for (int i = 0; i < this.stationList.size() - 1; i++) {
                    // Scheduler.getRuntimeMemory();
                    distanceStationStart = distanceStationEnd;
                    if (i < this.stationList.size() - 2) {
                        distanceStationEnd = this.route.getStation(this.stationList.get(i + 1)).getDistance();
                        waitTimeStationEnd = stopTime.get(i);
                    }
                    else{
                        waitTimeStationEnd = 0;
                    }
                    distanceBwStation = (i==0)?0:(distanceStationEnd - distanceStationStart);
                    delayBwStationActual =((distanceBwStation)/avgSpeed )*60;
                    delayBwStation = (int) Math.ceil(delayBwStationActual - delaySecondsAdded);
                    if(waitTimeStationEnd==0) {
                        delaySecondsAdded = delayBwStation - (delayBwStationActual - delaySecondsAdded);
                    }
                    else{
                        delaySecondsAdded = 0;
                    }
                    System.out.println("distance Bw station : " + distanceBwStation + " delayBwStation Actual : " +
                            delayBwStationActual + " delayBwStation : " + delayBwStation +
                            " delaySecondsAdded : " + delaySecondsAdded);
                    if (!addEdgeBwStations(i, maxDelayBwStations, delayBwStation, waitTimeStationEnd,
                            isSingleDay, minDelayBwTrains)) {
                        System.out.println("Some error occurred in adding edges.");
                        return Collections.emptyList();
                    }
                }
                if (!this.graphKBestPath.flushData()) {
                    System.out.println("Some error occurred in graph");
                    return Collections.emptyList();
                }
                System.out.println("Edge size: " + this.edgeCount);
                // System.out.println(this.graphKBestPath.toString());
            }

            List<Path> paths;
            KShortestPathFinder kShortestPathFinder = new KShortestPathFinder();
            paths = kShortestPathFinder.findShortestPaths(this.nodes.get(0).get(0),
                    this.nodes.get(this.nodes.size()-1).get(0), this.graphKBestPath, noOfPaths, maxCostList);
            System.out.println("Before disconnect :" +paths.toString());

            if(!this.graphKBestPath.disconnect()){
                System.out.println("Some error occurred with graph.");
            }
            System.out.println("After disconnect :" +paths.toString());
            // System.out.println(this.graphKBestPath.toString());
            this.graphKBestPath = null;
            // if(paths==null || paths.isEmpty()){
            //     TrainTime tempTrainTime;
            //     tempTrainTime = isSingleDay?new TrainTime(startDay,23,29):
            //             new TrainTime(6,23,29);
            //     if(sourceTime==null){
            //         System.out.println(" No path found in this iteration");
            //         return Collections.emptyList();
            //     }
            //     System.out.println(" No path found in this iteration " + sourceTime.toString());
            //
            //     if(sourceTime.compareTo(tempTrainTime)>=0){
            //         System.out.println(sourceTime + " No path found in this iteration");
            //         return Collections.emptyList();
            //     }
            //     sourceTime.addMinutes(30);
            //     return scheduleKBestPathOptimized(pathTemp, noOfPaths, sourceTime, destTime, maxDelayBwStations,
            //             minDelayBwTrains, stopTime, avgSpeed, startDay, startHrs, startMinutes,
            //             endDay, endHrs, endMinutes, isSingleDay, false, maxCostList);
            // }
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
                                          List<Integer> noOfDualPlatformList, List<Integer> noOfUpTrackList,
                                          List<Integer> noOfDownTrackList, List<Integer> noOfDualTrackList,
                                          int noOfPaths, TrainTime sourceTime,
                                          int minDelayBwTrains, double avgSpeed , List<Integer> stopTime,
                                          String pathOldUpTrainSchedule, String pathOldDownTrainSchedule, int trainDay,
                                          int startDay, int startHrs, int startMinutes, int endDay, int endHrs,
                                          int endMinutes, int maxDelayBwStations, boolean isSingleDay,
                                          boolean usePreviousComputation, double ratio) {
        TrainTime.updateIsSingleDay(isSingleDay);
        if(ratio<1){
            System.out.println("Ratio must be greater than 1.0");
            return Collections.emptyList();
        }
        long milli = new Date().getTime();
        System.out.println("***********************************************************************************");
        System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        System.out.println("Avg speed: " + avgSpeed);
        if(!addRoute(stationIdList,stationNameList,stationDistanceList,isDirectLineAvailableList,
                noOfUpPlatformList, noOfDownPlatformList, noOfDualPlatformList, noOfUpTrackList, noOfDownTrackList,
                noOfDualTrackList)){
            System.out.println("Some error occurred in adding route info");
            return Collections.emptyList();
        }
        if(!addTrainFromFolder(pathOldUpTrainSchedule, trainDay, isSingleDay, true)){
            System.out.println("Some error occurred in adding old train info");
            return Collections.emptyList();
        }
        if(!addTrainFromFolder(pathOldDownTrainSchedule, trainDay, isSingleDay, false)){
            System.out.println("Some error occurred in adding old train info");
            return Collections.emptyList();
        }
        // System.out.println(this.route.toString());
        // System.out.println("***********************************************************************************");
        // System.out.println(this.upTrainMap.values().toString());
        // System.out.println("***********************************************************************************");
        // System.out.println(this.downTrainMap.values().toString());
        if(stopTime.size() != this.route.getNumberOfStation()){
            System.out.println("Please give stop time for every station in route. if it does not stop at " +
                    "any particular station, give stop time as 0.");
            return Collections.emptyList();
        }

        List<Double> maxCostList = new ArrayList<>();
        if(stationDistanceList.size()!=stopTime.size()){
            System.out.println("Station distance and stop time size does not match");
            return Collections.emptyList();
        }
        maxCostList.add(0.0);

        for(int i=0;i<stationDistanceList.size();i++){
            maxCostList.add((Math.ceil((stationDistanceList.get(i)/avgSpeed)*60) + stopTime.get(i))*ratio);
        }
        maxCostList.add(maxCostList.get(maxCostList.size()-1));
        System.out.println(maxCostList.toString());
        List<Path> paths;
        paths= scheduleKBestPathOptimized(pathTemp, noOfPaths, sourceTime, null, maxDelayBwStations,
                minDelayBwTrains, stopTime,avgSpeed, startDay, startHrs,startMinutes,
                endDay, endHrs,endMinutes, isSingleDay, usePreviousComputation, maxCostList);
        milli = new Date().getTime() - milli;
        System.out.println("Duration: " + milli + "ms");
        System.out.println("After main done :" +paths.toString());
        return paths;
    }
}
