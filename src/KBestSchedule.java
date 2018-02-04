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

        int sizeStation = stationIdList.size();
        if(stationNameList.size() != sizeStation || stationDistanceList.size() != sizeStation ||
                isDirectLineAvailableList.size() != sizeStation || noOfUpPlatformList.size() != sizeStation ||
                noOfDownPlatformList.size() != sizeStation || noOfDualPlatformList.size() != sizeStation){
            throw new IllegalArgumentException("Invalid arguments for route");
        }
        for(int i=0;i<sizeStation;i++){
            if(!this.route.addStation(stationIdList.get(i), stationNameList.get(i),stationDistanceList.get(i),
                    isDirectLineAvailableList.get(i), noOfUpPlatformList.get(i), noOfDownPlatformList.get(i),
                    noOfDualPlatformList.get(i))){
                throw new RuntimeException("Unable to add station to route");
            }
        }
        return true;
    }

    private int addTrain(int trainNo, String trainName){
        requireNonNull(trainName, "Train name is null.");
        int temp = trainNo;
        while(this.trainMap.containsKey(trainNo)){
            System.out.println("Train already present. Key :" + trainNo);
            trainNo += 100000;
            System.out.println("Train already present. Updating key: " +trainNo);
        }
        this.trainMap.put(trainNo, new Train(temp, trainName));
        return trainNo;
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
            trainNo = addTrain(trainNo, trainName);
            if(trainNo<0){
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

    private boolean addTrainFromFolder(String pathOldTrainScheduleFolder, int trainDay, boolean isSingleDay){
        if(!isSingleDay && (trainDay>=7 || trainDay<0)){
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
                    trainNo = newTrainNo++;
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
        this.stationList = this.route.getStationList();
        this.stationList.add(0,"source");
        this.stationList.add("dest");
    }

    private int isValidEdge(double distanceBwStation, double avgSpeed, double waitTimeStationEnd,
                            Node nodeStart, Node nodeEnd, int maxDelayBwStations, boolean isSingleDay,
                            int minDelayBwTrains){
        requireNonNull(nodeStart, "start node is null");
        requireNonNull(nodeEnd, "end node is null");
        // if(!nodeStart.isValid() || !nodeEnd.isValid()){
        //     return -6;
        // }
        if(nodeStart.getTime()==null || nodeEnd.getTime()==null){
            return -7;
        }

        int delay = (int) Math.ceil((((distanceBwStation)/avgSpeed )*60));
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
            boolean isDirectLineAvailable = this.route.getStation(nodeEnd.getStationId()).isDirectLineAvailable();

            if(!nodeStart.getStationId().equalsIgnoreCase("source") &&
                    !nodeEnd.getStationId().equalsIgnoreCase("dest") ) {
                for (Train train : this.trainMap.values()) {
                    isUpDirection = train.isUpDirection();
                    TrainTime oldTrainDeptStationStart = train.getDept(nodeStart.getStationId());
                    TrainTime oldTrainArrStationEnd = train.getArr(nodeEnd.getStationId());
                    TrainTime oldTrainDeptStationEnd = train.getDept(nodeEnd.getStationId());

                    if(oldTrainArrStationEnd==null || oldTrainDeptStationEnd==null){
                        //different route train and next station is different
                        System.out.println("Train goes to different route after station" + nodeStart.toString());
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
                            minNumberOfPlatformRequired++;
                        }
                    }

                    if(oldTrainDeptStationStart==null){
                        //different route train but next station is same
                        System.out.println("Train comes from different route before station" + nodeEnd.toString());
                        continue;
                    }

                    int timeOldTrainStationStartDept = oldTrainDeptStationStart.getValue();

                    if(timeNodeStart<(timeOldTrainStationStartDept + minDelayBwTrains) &&
                            timeNodeStart>(timeOldTrainStationStartDept-minDelayBwTrains)){
                        // System.out.println("Min delay constraint" + oldTrainDeptStationStart);
                        return -8;
                    }

                    if(timeNodeEnd<(timeOldTrainStationEndArr+minDelayBwTrains) &&
                            timeNodeEnd>(timeOldTrainStationEndArr-minDelayBwTrains)){
                        // System.out.println("Min delay constraint" + oldTrainArrStationEnd);
                        return -9;
                    }

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
                        System.out.println("Train goes to different route after station" + nodeStart.toString());
                        continue;
                    }
                    int timeOldTrainStationEndArr = oldTrainArrStationEnd.getValue();
                    int timeOldTrainStationEndDept = oldTrainDeptStationEnd.getValue();
                    if(timeOldTrainStationEndDept<timeOldTrainStationEndArr){
                        timeOldTrainStationEndDept += isSingleDay?1440:10080;
                    }
                    if(timeNodeEnd<(timeOldTrainStationEndArr+minDelayBwTrains) &&
                            timeNodeEnd>(timeOldTrainStationEndArr-minDelayBwTrains)){
                        // System.out.println("Min delay constraint" + oldTrainArrStationEnd);
                        return -9;
                    }

                    if(!(isDirectLineAvailable && oldTrainArrStationEnd.equals(oldTrainDeptStationEnd))){
                        //need to check availability of platform

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
                if(!nodeEnd.isValid() || !nodeStart.isValid()){
                    System.out.println("Invalid node Edge added... " + nodeStart.toString() + " " + nodeEnd.toString());
                }
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

    private boolean addEdgeBwStations(int i,int maxDelayBwStations,
                                      double distanceBwStation, double avgSpeed, double waitTimeStationEnd,
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

        for(int j=0;j<this.nodes.get(i).size();j++) {
            nodeStart = this.nodes.get(i).get(j);
            // if(!nodeStart.isValid()){
            //     continue;
            // }
            int countLoopMax = maxDelayBwStations;
            if(countLoopMax>this.nodes.get(i+1).size() || nodeStart.getStationId().toLowerCase().contains("source")){
                countLoopMax = this.nodes.get(i+1).size();
            }

            for(int countLoop= 0;countLoop<countLoopMax; countLoop++) {
                int k = Math.floorMod(j+countLoop,this.nodes.get(i+1).size());
                nodeEnd = this.nodes.get(i+1).get(k);

                // if(!nodeEnd.isValid()){
                //     continue;
                // }

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
                        codeValidEdge = isValidEdge(distanceBwStation, avgSpeed, waitTimeStationEnd, nodeStart, nodeEnd,
                                maxDelayBwStations, isSingleDay, minDelayBwTrains);
                        if(codeValidEdge>0){
                            break;
                        }
                        else if(codeValidEdge==-1){
                            return false;
                        }
                        else if(codeValidEdge==-5 || codeValidEdge==-6 || codeValidEdge==-7|| codeValidEdge==-9){
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
                else{
                    int codeValidEdge = isValidEdge(distanceBwStation, avgSpeed, waitTimeStationEnd, nodeStart,
                            nodeEnd, maxDelayBwStations, isSingleDay, minDelayBwTrains);
                    if(codeValidEdge==-1){
                        return false;
                    }
                    else if(codeValidEdge==-5){
                        break;
                    }
                    else if(codeValidEdge==-9){
                        // System.out.println("Min delay constraint at dest not valid in adding edge bw " +
                        //         nodeStart.toString() + " and " + nodeEnd.toString());
                        continue;
                    }
                    else if(codeValidEdge==-8){
                        // System.out.println("Min delay constraint at source not valid in adding edge bw " +
                        //         nodeStart.toString() + " and " + nodeEnd.toString());
                        continue;
                    }
                    if(codeValidEdge>0){
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
                               int maxDelayBwStations, int minDelayBwTrains, List<Double> stopTime,
                               double avgSpeed, int startDay, int startHrs, int startMinutes, int endDay,
                               int endHrs, int endMinutes, boolean isSingleDay,
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
                    // Scheduler.getRuntimeMemory();
                    distanceStationStart = distanceStationEnd;
                    if (i < this.stationList.size() - 2) {
                        distanceStationEnd = this.route.getStation(this.stationList.get(i + 1)).getDistance();
                        waitTimeStationEnd = stopTime.get(i);
                    }
                    distanceBwStation = (i==0)?0:(distanceStationEnd - distanceStationStart);
                    if (!addEdgeBwStations(i, maxDelayBwStations, distanceBwStation, avgSpeed, waitTimeStationEnd,
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
            paths = kShortestPathFinder.findShortestPaths(new Node(sourceTime, "source"),
                    new Node(destTime, "dest"), this.graphKBestPath, noOfPaths);

            if(!this.graphKBestPath.disconnect()){
                System.out.println("Some error occurred with graph.");
            }
            // System.out.println(this.graphKBestPath.toString());
            this.graphKBestPath = null;
            if(paths==null || paths.isEmpty()){
                TrainTime tempTrainTime;
                tempTrainTime = isSingleDay?new TrainTime(startDay,23,29):
                        new TrainTime(6,23,29);
                if(sourceTime==null){
                    System.out.println(" No path found in this iteration");
                    return Collections.emptyList();
                }
                System.out.println(" No path found in this iteration " + sourceTime.toString());

                if(sourceTime.compareTo(tempTrainTime)>=0){
                    System.out.println(sourceTime + " No path found in this iteration");
                    return Collections.emptyList();
                }
                sourceTime.addMinutes(30);
                return scheduleKBestPathOptimized(pathTemp, noOfPaths, sourceTime, destTime, maxDelayBwStations,
                        minDelayBwTrains, stopTime, avgSpeed, startDay, startHrs, startMinutes,
                        endDay, endHrs, endMinutes, isSingleDay, false);
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
                                          int noOfPaths, TrainTime sourceTime,
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
        paths= scheduleKBestPathOptimized(pathTemp, noOfPaths, sourceTime, null, maxDelayBwStations,
                minDelayBwTrains, stopTime,avgSpeed, startDay, startHrs,startMinutes,
                endDay, endHrs,endMinutes, isSingleDay, usePreviousComputation);
        milli = new Date().getTime() - milli;
        System.out.println("Duration: " + milli + "ms");
        return paths;
    }
}
