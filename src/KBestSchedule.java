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
        this.downTrainMap.put(trainDay+":"+trainNo, new Train(trainNo, trainName));
        return true;
    }

    private boolean addStoppageUpTrain(int trainDay, int trainNo, String stationId, TrainTime arrival,
                                       TrainTime departure, double distance){
        Train train = this.upTrainMap.getOrDefault(trainDay+":" +trainNo, null);
        if(train==null){
            System.out.println("Train not found "+ trainNo +" originating day "+ trainDay);
            return false;
        }
        Station station = this.route.getStation(stationId);
        //station==null represents that station is not in the route.
        return station==null || train.addStoppage(station, arrival, departure, distance);
    }

    private boolean addStoppageDownTrain(int trainDay, int trainNo, String stationId, TrainTime arrival,
                                         TrainTime departure, double distance){
        Train train = this.downTrainMap.getOrDefault(trainDay+":" +trainNo, null);
        if(train==null){
            System.out.println("Train not found "+ trainNo +" originating day "+ trainDay);
            return false;
        }
        Station station = this.route.getStation(stationId);
        //station==null represents that station is not in the route.
        return station==null || train.addStoppage(station, arrival, departure, distance);
    }

    private boolean addTrainFromFile(int trainNo, String trainName, String pathTrainSchedule, int trainDay,
                                     boolean isSingleDay, boolean isUpDirection){
        int stoppageDay = trainDay;
        try {
            if(isUpDirection && !addUpTrain(trainDay, trainNo, trainName)){
                return false;
            }
            else if(!isUpDirection && !addDownTrain(trainDay, trainNo, trainName)){
                return false;
            }
            FileReader fReader = new FileReader(pathTrainSchedule);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            TrainTime arrival, departure=null;
            String stationId;
            String data[];
            String data1[];
            double dist;
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
                try {
                    dist = Double.parseDouble(data[3]);
                }
                catch (Exception e){
                    dist=0;
                    e.printStackTrace();
                }
                if(isUpDirection && !addStoppageUpTrain(trainDay, trainNo,stationId,arrival,departure, dist)){
                    bReader.close();
                    fReader.close();
                    return false;
                }
                else if(!isUpDirection && !addStoppageDownTrain(trainDay, trainNo,stationId,arrival,departure, dist)){
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
        if(!isSingleDay && (trainDay>=7 || trainDay<0)) {
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

        File[] listOfFiles = new File(pathOldTrainScheduleFolder).listFiles();
        if(listOfFiles==null) {
            System.out.println("No old trains found : " +pathOldTrainScheduleFolder);
            return true;
        }

        for (File file: listOfFiles) {
            if(file.isFile()) {
                int trainNo;
                try {
                    trainNo = Integer.parseInt(file.getName().split("\\.")[0]);
                }
                catch (Exception e) {
                    System.out.print("File name should be train Number.");
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
                            int maxDelayBwStations, boolean isSingleDay, int minDelayBwTrains,
                            int totalUpPlatform ,int totalDownPlatform,int totalDualPlatform ,int totalUpTrack,
                            int totalDownTrack,int totalDualTrack, boolean isDirectLineAvailable){

        // System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS").format(new Date())+" Is valid edge start "+
        //         nodeStart.toString()+" > " +nodeEnd.toString());
        requireNonNull(nodeStart, "start node is null");
        requireNonNull(nodeEnd, "end node is null");
        // int bakTotalUpT = totalUpTrack+totalDownTrack+totalDualTrack;
        // int bakTotalUpP = totalUpPlatform+totalDownPlatform+totalDualPlatform;
        totalUpPlatform--;
        totalUpTrack--;

        if(nodeStart.getTime()==null || nodeEnd.getTime()==null){
            return -1;
        }
        boolean addedNodeEnd= false;
        int timeNodeStart = nodeStart.getTime().getValue();
        int timeNodeEnd = nodeEnd.getTime().getValue();
        if(timeNodeEnd<timeNodeStart){
            timeNodeEnd += (isSingleDay?1440:10080);
            addedNodeEnd = true;
        }
        int timeEarliestToReach = timeNodeStart + delayBwStation;
        int timeEarliestToDepart = timeEarliestToReach + waitTimeStationEnd;
        int timeMaxToDepart = timeEarliestToDepart + maxDelayBwStations;

        // List<Integer> obstructingTrains1 = new ArrayList<>();
        // List<Integer> obstructingTrains2 = new ArrayList<>();
        // List<Integer> obstructingTrains3 = new ArrayList<>();
        // List<Integer> obstructingTrains4 = new ArrayList<>();

        if(timeEarliestToDepart<=timeNodeEnd && timeNodeEnd <=timeMaxToDepart){

            if(!nodeStart.getStationId().equalsIgnoreCase("source") &&
                    !nodeEnd.getStationId().equalsIgnoreCase("dest") ) {
                for (Train train : this.upTrainMap.values()) {
                    int typeNextDay = 0;
                    TrainTime oldTrainDeptStation1 = train.getDept(nodeStart.getStationId());
                    TrainTime oldTrainArrStation2 = train.getArr(nodeEnd.getStationId());
                    TrainTime oldTrainDeptStation2 = train.getDept(nodeEnd.getStationId());

                    if(oldTrainArrStation2==null || oldTrainDeptStation2==null){
                        //different route train and next station is different
                        continue;
                    }

                    int timeOldTrainArrStation2 = oldTrainArrStation2.getValue();
                    int timeOldTrainDeptStation2 = oldTrainDeptStation2.getValue();

                    int timeOldTrainDeptStation1 = (oldTrainDeptStation1!=null)?oldTrainDeptStation1.getValue():-1;
                    if(timeOldTrainArrStation2<timeOldTrainDeptStation1){
                        if(addedNodeEnd) {
                            timeOldTrainArrStation2 += (isSingleDay ? 1440 : 10080);
                            timeOldTrainDeptStation2 += (isSingleDay ? 1440 : 10080);
                        }
                        typeNextDay = 1;
                    }

                    if(timeOldTrainDeptStation2<timeOldTrainArrStation2){
                        if(addedNodeEnd) {
                            timeOldTrainDeptStation2 += (isSingleDay ? 1440 : 10080);
                        }
                        typeNextDay = 2;
                    }

                    if(!( isDirectLineAvailable && timeOldTrainArrStation2==timeOldTrainDeptStation2)){
                        //need to check availability of platform
                        if(!addedNodeEnd && typeNextDay==2){
                            if((timeNodeEnd>timeOldTrainArrStation2 && timeNodeEnd<=(isSingleDay ? 1440 : 10080) )||
                                    (timeEarliestToReach <timeOldTrainDeptStation2 && timeEarliestToReach>=0)){
                                totalUpPlatform--;
                                // obstructingTrains1.add(train.getTrainNo());
                            }
                        }
                        else if(!(timeNodeEnd<timeOldTrainArrStation2 || timeEarliestToReach >timeOldTrainDeptStation2)){
                            totalUpPlatform--;
                            // obstructingTrains1.add(train.getTrainNo());
                        }
                    }

                    if(timeEarliestToReach<(timeOldTrainArrStation2+minDelayBwTrains) &&
                            timeEarliestToReach>(timeOldTrainArrStation2-minDelayBwTrains)){
                        totalUpTrack--;
                        // obstructingTrains2.add(train.getTrainNo());
                        continue;
                    }

                    if(timeOldTrainDeptStation1<0){
                        //different route train but next station is same
                        continue;
                    }

                    if(timeNodeStart<(timeOldTrainDeptStation1+minDelayBwTrains) &&
                            timeNodeStart>(timeOldTrainDeptStation1-minDelayBwTrains)){
                        totalUpTrack--;
                        // obstructingTrains2.add(train.getTrainNo());
                        continue;
                    }

                    if(typeNextDay==1 && !addedNodeEnd){
                        if((timeNodeStart>timeOldTrainDeptStation1 && timeEarliestToReach <(isSingleDay ? 1440 : 10080))||
                                (timeEarliestToReach < timeOldTrainArrStation2)){
                            totalUpTrack--;
                            // obstructingTrains2.add(train.getTrainNo());
                        }
                    }
                    else if((timeOldTrainDeptStation1<timeNodeStart && timeOldTrainArrStation2 >timeEarliestToReach)||
                            (timeOldTrainDeptStation1>timeNodeStart && timeOldTrainArrStation2 <timeEarliestToReach)){
                        totalUpTrack--;
                        // obstructingTrains2.add(train.getTrainNo());
                    }
                }

                for (Train train : this.downTrainMap.values()) {
                    int typeNextDay = 0;
                    TrainTime oldTrainArrStation1 = train.getArr(nodeStart.getStationId());
                    TrainTime oldTrainArrStation2 = train.getArr(nodeEnd.getStationId());
                    TrainTime oldTrainDeptStation2 = train.getDept(nodeEnd.getStationId());

                    if(oldTrainArrStation2==null || oldTrainDeptStation2==null){
                        //different route train and next station is different
                        continue;
                    }

                    int timeOldTrainArrStation2 = oldTrainArrStation2.getValue();
                    int timeOldTrainDeptStation2 = oldTrainDeptStation2.getValue();

                    if(timeOldTrainDeptStation2<timeOldTrainArrStation2){
                        if(addedNodeEnd) {
                            timeOldTrainDeptStation2 += (isSingleDay ? 1440 : 10080);
                        }
                        typeNextDay = 2;
                    }

                    if(!( isDirectLineAvailable && timeOldTrainArrStation2==timeOldTrainDeptStation2)){
                        //need to check availability of platform
                        if(!addedNodeEnd && typeNextDay==2){
                            if((timeNodeEnd>timeOldTrainArrStation2 && timeNodeEnd<=(isSingleDay ? 1440 : 10080) )||
                                    (timeEarliestToReach <timeOldTrainDeptStation2 && timeEarliestToReach>=0)){
                                totalDownPlatform--;
                                // obstructingTrains3.add(train.getTrainNo());
                            }
                        }
                        else if(!(timeNodeEnd<timeOldTrainArrStation2 || timeEarliestToReach >timeOldTrainDeptStation2)){
                            totalDownPlatform--;
                            // obstructingTrains3.add(train.getTrainNo());
                        }
                    }

                    if(timeEarliestToReach<(timeOldTrainDeptStation2+minDelayBwTrains) &&
                            timeEarliestToReach>(timeOldTrainDeptStation2-minDelayBwTrains)){
                        totalDownTrack--;
                        // obstructingTrains4.add(train.getTrainNo());
                        continue;
                    }

                    if(oldTrainArrStation1==null){
                        //different route train but next station is same
                        continue;
                    }

                    int timeOldTrainArrStation1 = (oldTrainArrStation1!=null)?oldTrainArrStation1.getValue():-1;
                    if(timeOldTrainArrStation1<timeOldTrainDeptStation2){
                        if(addedNodeEnd) {
                            timeOldTrainArrStation1 += (isSingleDay ? 1440 : 10080);
                        }
                        typeNextDay = 1;
                    }

                    if(timeNodeStart<(timeOldTrainArrStation1+minDelayBwTrains) &&
                            timeNodeStart>(timeOldTrainArrStation1-minDelayBwTrains)){
                        totalDownTrack--;
                        // obstructingTrains4.add(train.getTrainNo());
                        continue;
                    }

                    if(typeNextDay==1 && !addedNodeEnd){
                        if(timeNodeStart<timeOldTrainArrStation1 ||
                                (timeEarliestToReach > timeOldTrainDeptStation2)){
                            totalDownTrack--;
                            // obstructingTrains4.add(train.getTrainNo());
                        }
                    }
                    else if(timeOldTrainArrStation1>timeNodeStart && timeOldTrainDeptStation2 <timeEarliestToReach){
                        totalDownTrack--;
                        // obstructingTrains4.add(train.getTrainNo());
                    }
                }
            }
            else if(nodeStart.getStationId().equalsIgnoreCase("source")){
                for (Train train : this.upTrainMap.values()) {
                    TrainTime oldTrainArrStation2 = train.getArr(nodeEnd.getStationId());
                    TrainTime oldTrainDeptStation2 = train.getDept(nodeEnd.getStationId());
                    if(oldTrainArrStation2==null || oldTrainDeptStation2==null){
                        continue;
                    }
                    int timeOldTrainArrStation2 = oldTrainArrStation2.getValue();
                    int timeOldTrainDeptStation2 = oldTrainDeptStation2.getValue();
                    boolean typeNextDay = false;
                    if(timeOldTrainDeptStation2<timeOldTrainArrStation2){
                        if(addedNodeEnd) {
                            timeOldTrainDeptStation2 += (isSingleDay ? 1440 : 10080);
                        }
                        typeNextDay = true;
                    }
                    if(!(isDirectLineAvailable && timeOldTrainArrStation2==timeOldTrainDeptStation2)){
                        //need to check availability of platform

                        if(!addedNodeEnd && typeNextDay){
                            if(timeEarliestToReach>timeOldTrainArrStation2 || timeNodeEnd< timeOldTrainDeptStation2){
                                totalUpPlatform--;
                                // obstructingTrains1.add(train.getTrainNo());
                            }
                        }
                        else if(!(timeNodeEnd<timeOldTrainArrStation2 || timeEarliestToReach >timeOldTrainDeptStation2)){
                            totalUpPlatform--;
                            // obstructingTrains1.add(train.getTrainNo());
                        }
                    }

                    if(timeEarliestToReach<(timeOldTrainArrStation2+minDelayBwTrains) &&
                            timeEarliestToReach>(timeOldTrainArrStation2-minDelayBwTrains)){
                        totalUpTrack--;
                        // obstructingTrains2.add(train.getTrainNo());
                    }
                }

                for (Train train : this.downTrainMap.values()) {
                    TrainTime oldTrainArrStation2 = train.getArr(nodeEnd.getStationId());
                    TrainTime oldTrainDeptStation2 = train.getDept(nodeEnd.getStationId());
                    if(oldTrainArrStation2==null || oldTrainDeptStation2==null){
                        continue;
                    }
                    int timeOldTrainArrStation2 = oldTrainArrStation2.getValue();
                    int timeOldTrainDeptStation2 = oldTrainDeptStation2.getValue();
                    boolean typeNextDay = false;
                    if(timeOldTrainDeptStation2<timeOldTrainArrStation2){
                        if(addedNodeEnd) {
                            timeOldTrainDeptStation2 += (isSingleDay ? 1440 : 10080);
                        }
                        typeNextDay = true;
                    }
                    if(!(isDirectLineAvailable && timeOldTrainArrStation2==timeOldTrainDeptStation2)){
                        //need to check availability of platform

                        if(!addedNodeEnd && typeNextDay){
                            if(timeEarliestToReach>timeOldTrainArrStation2 || timeNodeEnd< timeOldTrainDeptStation2){
                                totalDownPlatform--;
                                // obstructingTrains3.add(train.getTrainNo());
                            }
                        }
                        else if(!(timeNodeEnd<timeOldTrainArrStation2 || timeEarliestToReach >timeOldTrainDeptStation2)){
                            totalDownPlatform--;
                            // obstructingTrains3.add(train.getTrainNo());
                        }
                    }

                    if(timeEarliestToReach<(timeOldTrainDeptStation2+minDelayBwTrains) &&
                            timeEarliestToReach>(timeOldTrainDeptStation2-minDelayBwTrains)){
                        totalDownTrack--;
                        // obstructingTrains4.add(train.getTrainNo());
                    }
                }
            }
            else if(nodeEnd.getStationId().equalsIgnoreCase("dest")){
                return 1;
            }

            if((totalUpTrack+ totalDualTrack+ totalDownTrack)<0){
                // System.out.println("Track Constraint: "+ bakTotalUpT+ " "+nodeStart.toString()+ " "+ nodeEnd.toString()+ " "+
                //         obstructingTrains1.toString()+" > "+ obstructingTrains2.toString()+" >> "+
                //         obstructingTrains3.toString()+" >>> "+ obstructingTrains4.toString());
                return -2;
            }

            if((totalUpPlatform+totalDualPlatform+ totalDownPlatform)<0) {
                // System.out.println("Platform Constraint: "+ bakTotalUpP+ " "+nodeStart.toString()+ " "+ nodeEnd.toString()+ " "+
                //         obstructingTrains1.toString()+" > "+ obstructingTrains2.toString()+" >> "+
                //         obstructingTrains3.toString()+" >>> "+ obstructingTrains4.toString());
                return -3;
            }
            else{
                return 2;
            }
        }
        else if(timeEarliestToDepart>timeNodeEnd){
            System.out.println("Rejected as earliest to depart is more than node time :" + nodeStart.toString() +
                    " -> " + nodeEnd.toString());
            return -4;
        }
        else{
            return -5;
        }
    }

    private boolean addEdgeBwStations(int i,int maxDelayBwStations, int delayBwStation, int waitTimeStationEnd,
                                      boolean isSingleDay, int minDelayBwTrains, boolean aroundSourceTime){
        // System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+" addEdge started");
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
        // System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+" after adding nodes");
        Station station2 = this.route.getStation(this.stationList.get(i+1));

        if((!this.stationList.get(i+1).equalsIgnoreCase("dest") && station2==null)){
            System.out.println("Some error occurred...");
            return false;
        }
        int totalUpPlatform,totalDownPlatform,totalDualPlatform,totalUpTrack,totalDownTrack,totalDualTrack;
        boolean isDirectLineAvailable;

        if(station2!=null){
            totalUpPlatform = station2.getNoOfUpPlatform();
            totalDownPlatform = station2.getNoOfDownPlatform();
            totalDualPlatform = station2.getNoOfDualPlatform();
            totalUpTrack = station2.getNoOfUpTrack();
            totalDownTrack = station2.getNoOfDownTrack();
            totalDualTrack = station2.getNoOfDualTrack();
            isDirectLineAvailable = station2.isDirectLineAvailable();
        }
        else{
            totalUpPlatform = 1000;
            totalDownPlatform = 1000;
            totalDualPlatform = 1000;
            totalUpTrack = 1000;
            totalDownTrack = 1000;
            totalDualTrack = 1000;
            isDirectLineAvailable = true;
        }

        if((totalUpPlatform+totalDownPlatform+totalDualPlatform)<=0 || (totalUpTrack+totalDownTrack+totalDualTrack)<=0){
            System.out.println("No platforms/tracks is available to schedule "+ station2.getId());
            return false;
        }

        if(!this.graphKBestPath.addMultipleNode(this.nodes.get(i+1))){
            System.out.println("Some error occurred in adding nodes " + this.nodes.get(i+1).get(0).toString());
            return false;
        }

        // System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+" after adding multiple nodes all");

        System.out.print("Adding edge bw stations " + this.stationList.get(i) +"->" + this.stationList.get(i+1)+" ");
        System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        Scheduler.getRuntimeMemory();

        for(int j=0;j<this.nodes.get(i).size();j++) {
            // System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS").format(new Date())+" loop 1st :"+ j);
            nodeStart = this.nodes.get(i).get(j);

            int countLoopMax = maxDelayBwStations;
            if(countLoopMax>this.nodes.get(i+1).size() || nodeStart.getStationId().equalsIgnoreCase("source")){
                countLoopMax = this.nodes.get(i+1).size();
            }

            for(int countLoop= 0;countLoop<countLoopMax; countLoop++) {
                // System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS").format(new Date())+" loop 2nd :"+ countLoop);
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
                    boolean validEdge = false;
                    TrainTime tempTrainTime = (isSingleDay?new TrainTime(nodeStart.getTime().day,23,55):
                            new TrainTime(6,23,54));

                    do{
                        int codeValidEdge = isValidEdge(delayBwStation, waitTimeStationEnd, nodeStart, nodeEnd,
                                maxDelayBwStations, isSingleDay, minDelayBwTrains, totalUpPlatform, totalDownPlatform,
                                totalDualPlatform,totalUpTrack,totalDownTrack,totalDualTrack, isDirectLineAvailable);
                        if(codeValidEdge>0){
                            validEdge = true;
                            break;
                        }
                        else if(addedFirstEdge){
                            break;
                        }
                        else if(codeValidEdge==-1 ||codeValidEdge==-3 ||codeValidEdge==-4 || codeValidEdge==-5 ){
                            break;
                        }
                        else if(nodeStart.getTime().compareTo(nodeEnd.getTime())>=0){
                            break;
                        }
                        else if(nodeStart.getTime().compareTo(tempTrainTime)>=0){
                            System.out.println("No place on source station platform");
                            return false;
                        }
                        nodeStart.getTime().addMinutes(1);
                    } while(!validEdge);

                    if(validEdge) {
                        addedFirstEdge = true;
                        int edgeCost = 0;
                        if(!aroundSourceTime){
                            edgeCost = nodeEnd.getTime().compareTo(nodeStart.getTime());
                            if(edgeCost<0){
                                edgeCost+= (isSingleDay?1440:10080);
                            }
                        }
                        if(this.graphKBestPath.addEdge(new Edge(nodeStart, nodeEnd,edgeCost))){
                            this.edgeCount++;
                        }
                        else{
                            System.err.println("Some error occurred in adding edge bw " + nodeStart.toString() +
                                    " and " + nodeEnd.toString()+  " cost 0");
                        }
                    }
                }
                else{
                    int codeValidEdge = isValidEdge(delayBwStation, waitTimeStationEnd, nodeStart,
                            nodeEnd, maxDelayBwStations, isSingleDay, minDelayBwTrains,totalUpPlatform, totalDownPlatform,
                            totalDualPlatform,totalUpTrack,totalDownTrack,totalDualTrack, isDirectLineAvailable);
                    System.out.print(codeValidEdge+" ");
                    if(codeValidEdge==-5){
                        break;
                    }
                    else if(codeValidEdge>0){
                        int edgeCost = nodeEnd.getTime().compareTo(nodeStart.getTime());
                        if(edgeCost<0){
                            edgeCost+= (isSingleDay ? 1440 : 10080);
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
                               boolean usePreviousComputation, List<Double> maxCostList, boolean aroundSourceTime){
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
                    // System.out.println("distance Bw station : " + distanceBwStation + " delayBwStation Actual : " +
                    //         delayBwStationActual + " delayBwStation : " + delayBwStation +
                    //         " delaySecondsAdded : " + delaySecondsAdded);
                    if (!addEdgeBwStations(i, maxDelayBwStations, delayBwStation, waitTimeStationEnd,
                            isSingleDay, minDelayBwTrains, aroundSourceTime)) {
                        System.out.println("Some error occurred in adding edges.");
                        return Collections.emptyList();
                    }
                    // System.out.println("Edge size: " + this.edgeCount);
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

            if(!this.graphKBestPath.disconnect()){
                System.out.println("Some error occurred with graph.");
            }
            // System.out.println(this.graphKBestPath.toString());
            this.graphKBestPath = null;
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
                                          String pathOldUpTrainSchedule, String pathOldDownTrainSchedule,
                                          String pathOldSSTrainSchedule, int trainDay,
                                          int startDay, int startHrs, int startMinutes, int endDay, int endHrs,
                                          int endMinutes, int maxDelayBwStations, boolean isSingleDay,
                                          boolean usePreviousComputation, double ratio, boolean aroundSourceTime) {
        TrainTime.updateIsSingleDay(isSingleDay);
        if(ratio<1){
            System.out.println("Ratio must be greater than 1.0");
            return Collections.emptyList();
        }
        long milli = new Date().getTime();
        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        System.out.println("Avg speed: " + avgSpeed);
        System.out.println("Mode: "+(isSingleDay?"Single Day":"Week wise"));
        System.out.println("Train Day: "+ trainDay);
        System.out.println("Max ratio: "+ ratio);
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
        if(!addTrainFromFolder(pathOldSSTrainSchedule, trainDay, isSingleDay, false)){
            System.out.println("Some error occurred in adding old train info");
            return Collections.emptyList();
        }
        // System.out.println(this.route.toString());
        // System.out.println("***********************************************************************************");
        // System.out.println(this.upTrainMap.values().toString());
        // System.out.println("***********************************************************************************");
        // System.out.println(this.downTrainMap.values().toString());
        System.out.println("***********************************************************************************");
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
        double startDistance = stationDistanceList.get(0);
        double sumStopTimes = 0;
        for(int i=0;i<stationDistanceList.size();i++){
            sumStopTimes +=stopTime.get(i);
            maxCostList.add((Math.ceil(((stationDistanceList.get(i)-startDistance)/avgSpeed)*60) + sumStopTimes)*ratio);
        }
        maxCostList.add(maxCostList.get(maxCostList.size()-1));
        if(!aroundSourceTime){
            for(int i=0;i<maxCostList.size();i++){
                maxCostList.set(i,maxCostList.get(i)+maxDelayBwStations);
            }
        }
        System.out.println(maxCostList.toString());
        List<Path> paths;
        paths= scheduleKBestPathOptimized(pathTemp, noOfPaths, sourceTime, null, maxDelayBwStations,
                minDelayBwTrains, stopTime,avgSpeed, startDay, startHrs,startMinutes,
                endDay, endHrs,endMinutes, isSingleDay, usePreviousComputation, maxCostList, aroundSourceTime);
        milli = new Date().getTime() - milli;
        System.out.println("Duration: " + milli + " ms");
        System.out.print(paths.size());
        for(Path path: paths) {
            System.out.print("\t"+path.pathCost());
        }
        System.out.println();
        System.out.println("---------------------------------------------------------------------------------------------------------");
        return paths;
    }
}
