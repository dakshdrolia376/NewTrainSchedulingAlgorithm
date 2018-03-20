import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class KBestSchedule {

    private Route route;
    private final Map<String, Train> trainMap;
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

    private boolean addTrain(int trainDay, int trainNo, String trainName){
        requireNonNull(trainName, "Train name is null.");
        this.trainMap.put(trainDay+":"+trainNo, new Train(trainNo, trainName));
        return true;
    }

    private boolean addStoppageTrain(int trainDay, int trainNo, String stationId, TrainTime arrival,
                                       TrainTime departure){
        Train train = this.trainMap.getOrDefault(trainDay+":" +trainNo, null);
        if(train==null){
            System.out.println("Train not found "+ trainNo +" originating day "+ trainDay);
            return false;
        }
        Station station = this.route.getStation(stationId);
        //station==null represents that station is not in the route.
        return station==null || train.addStoppage(station, arrival, departure);
    }

    private boolean addTrainFromFile(int trainNo, String trainName, String pathTrainSchedule, int trainDay){
        int stoppageDay = trainDay;
        try {
            if(!addTrain(trainDay, trainNo, trainName)){
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
                if(departure!=null && arrival.compareTo(departure)<0){
                    arrival.addDay(1);
                    stoppageDay = arrival.day;
                }
                data1 =data[2].split(":");
                departure = new TrainTime(stoppageDay,Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure.compareTo(arrival)<0){
                    departure.addDay(1);
                    stoppageDay = departure.day;
                }
                if(!addStoppageTrain(trainDay, trainNo,stationId,arrival,departure)){
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

    private boolean addTrainFromFolderSingleDay(String pathOldTrainScheduleFolder, int trainDay){
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
                if(!addTrainFromFile(trainNo,file.getName(),file.getPath(), trainDay)){
                    return false;
                }
            }
        }

        return true;
    }

    private boolean addTrainFromFolder(String pathOldTrainScheduleFolder){

        return addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                "day0", 0) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day1", 1) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day2", 2) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day3", 3) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day4", 4) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day5", 5) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day6", 6);
    }

    private void getNodesFreeSlot(){

        this.nodes = this.route.getFreeSlots(new TrainTime(0, 0, 0),
                new TrainTime(6, 23, 59));
        List<Node> nodeSrcList = new ArrayList<>();
        nodeSrcList.add(new Node(null, "source"));
        this.nodes.add(0,nodeSrcList);
        List<Node> nodeDestList = new ArrayList<>();
        nodeDestList.add(new Node(null, "dest"));
        this.nodes.add(nodeDestList);
    }

    private void getStationList(){
        this.stationList = this.route.getStationList();
        this.stationList.add(0,"source");
        this.stationList.add("dest");
    }

    private int isValidEdge(int delayBwStation, int waitTimeStationEnd, Node nodeStart, Node nodeEnd,
                            int maxDelayBwStations, int minDelayBwTrains,
                            int totalUpPlatform ,int totalDownPlatform,int totalDualPlatform ,int totalUpTrack,
                            int totalDownTrack,int totalDualTrack, boolean isDirectLineAvailable){
        // System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS").format(new Date())+" Is valid edge start "+
        //         nodeStart.toString()+" > " +nodeEnd.toString());
        requireNonNull(nodeStart, "start node is null");
        requireNonNull(nodeEnd, "end node is null");
        totalUpPlatform--;
        totalUpTrack--;

        if(nodeStart.getTime()==null || nodeEnd.getTime()==null){
            return -1;
        }

        boolean addedNodeEnd= false;
        int timeNodeStart = nodeStart.getTime().getValue();
        int timeNodeEnd = nodeEnd.getTime().getValue();
        if(timeNodeEnd<timeNodeStart){
            timeNodeEnd += 10080;
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
                for (Train train : this.trainMap.values()) {
                    TrainAtStation s1 = train.getStationInfo(nodeStart.getStationId(),0);
                    TrainAtStation s2 = train.getStationInfo(nodeEnd.getStationId(),0);
                    if(s2==null){
                        continue;
                    }
                    //check platform requirement
                    int count=0;
                    do{
                        boolean addedOldTrain = false;
                        TrainTime oldTrainArrStation2 = s2.getArr();
                        TrainTime oldTrainDeptStation2 = s2.getDept();
                        if(oldTrainArrStation2==null || oldTrainDeptStation2==null){
                            // System.out.println("Error in storing stoppage info info :"+ train.getTrainNo()+ " "+ nodeEnd.getStationId());
                            s2= train.getStationInfo(nodeEnd.getStationId(), ++count);
                            continue;
                        }

                        int timeOldTrainArrStation2 = oldTrainArrStation2.getValue();
                        int timeOldTrainDeptStation2 = oldTrainDeptStation2.getValue();

                        if(oldTrainDeptStation2.day==0 && oldTrainArrStation2.day==6){
                            addedOldTrain = true;
                        }

                        if(!( isDirectLineAvailable && timeOldTrainArrStation2==timeOldTrainDeptStation2)){
                            //need to check availability of platform
                            if(addedNodeEnd && addedOldTrain) {
                                totalUpPlatform--;
                                // System.out.println("Platform : "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                //         oldTrainArrStation2.toString()+ " "+ oldTrainDeptStation2.toString()+ " "+train.getTrainNo());
                            }
                            else if(addedOldTrain){
                                if((timeNodeEnd>=timeOldTrainArrStation2 )|| ( timeEarliestToReach<=timeOldTrainDeptStation2)){
                                    totalUpPlatform--;
                                    // System.out.println("Platform : "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                    //         oldTrainArrStation2.toString()+ " "+ oldTrainDeptStation2.toString()+ " "+train.getTrainNo());
                                }
                            }
                            else if(addedNodeEnd){
                                if(timeEarliestToReach>=10080){
                                    if(!((timeNodeEnd-10080)<=timeOldTrainArrStation2 || (timeEarliestToReach-10080) >=timeOldTrainDeptStation2)){
                                        totalUpPlatform--;
                                        // System.out.println("Platform : "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                        //         oldTrainArrStation2.toString()+ " "+ oldTrainDeptStation2.toString()+ " "+train.getTrainNo());
                                    }
                                }
                                else {
                                    if (((timeNodeEnd - 10080) >= timeOldTrainArrStation2) || (timeEarliestToReach <= timeOldTrainDeptStation2)) {
                                        totalUpPlatform--;
                                        // System.out.println("Platform : "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                        //         oldTrainArrStation2.toString()+ " "+ oldTrainDeptStation2.toString()+ " "+train.getTrainNo());
                                    }
                                }
                            }
                            else if(!(timeNodeEnd<=timeOldTrainArrStation2 || timeEarliestToReach >=timeOldTrainDeptStation2)){
                                totalUpPlatform--;
                                // System.out.println("Platform : "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                //         oldTrainArrStation2.toString()+ " "+ oldTrainDeptStation2.toString()+ " "+train.getTrainNo());
                            }
                        }
                        s2= train.getStationInfo(nodeEnd.getStationId(), ++count);
                    }
                    while(s2!=null);

                    if(s1!=null){
                        //check track requirement, some train can be both direction for a particular segment.

                        s1 = train.getStationInfo(nodeStart.getStationId(), nodeEnd.getStationId(),true);
                        s2 = train.getStationInfo(nodeStart.getStationId(), nodeEnd.getStationId(), false);

                        if(s1!=null && s2 !=null){
                            //same direction train
                            TrainTime oldTrainDeptStation1 = s1.getDept();
                            TrainTime oldTrainArrStation2 = s2.getArr();

                            int timeOldTrainDeptStation1 = oldTrainDeptStation1.getValue();
                            int timeOldTrainArrStation2 = oldTrainArrStation2.getValue();
                            boolean addedOldTrain = false;
                            if(timeOldTrainArrStation2<timeOldTrainDeptStation1){
                                timeOldTrainArrStation2+=10080;
                                addedOldTrain=true;
                            }
                            if((timeOldTrainDeptStation1+minDelayBwTrains)>= timeNodeStart &&
                                    (timeOldTrainDeptStation1-minDelayBwTrains)<= timeNodeStart){
                                totalUpTrack--;
                                // System.out.println("Track Min Delay Same1: "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                //         oldTrainDeptStation1.toString()+ " "+ oldTrainArrStation2.toString()+ " "+train.getTrainNo());
                                continue;
                            }

                            if((timeOldTrainArrStation2+minDelayBwTrains)>= timeEarliestToReach &&
                                    (timeOldTrainArrStation2-minDelayBwTrains)<= timeEarliestToReach){
                                totalUpTrack--;
                                // System.out.println("Track Min Delay Same2: "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                //         oldTrainDeptStation1.toString()+ " "+ oldTrainArrStation2.toString()+ " "+train.getTrainNo());
                                continue;
                            }

                            if(timeEarliestToReach>=10080 && addedOldTrain){
                                if((timeNodeStart<timeOldTrainDeptStation1) && (timeEarliestToReach > timeOldTrainArrStation2) ||
                                        ((timeNodeStart > timeOldTrainDeptStation1) && (timeEarliestToReach < timeOldTrainArrStation2))){
                                    totalUpTrack--;
                                    // System.out.println("Track Same: "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                    //         oldTrainDeptStation1.toString()+ " "+ oldTrainArrStation2.toString()+ " "+train.getTrainNo());
                                }
                            }
                            else if(addedOldTrain){
                                timeOldTrainArrStation2-=10080;
                                if((timeEarliestToReach< timeOldTrainArrStation2) || (timeNodeStart > timeOldTrainDeptStation1)){
                                    totalUpTrack --;
                                    // System.out.println("Track Same: "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                    //         oldTrainDeptStation1.toString()+ " "+ oldTrainArrStation2.toString()+ " "+train.getTrainNo());
                                }
                            }
                            else if(timeEarliestToReach>=10080){
                                if ((timeOldTrainArrStation2 < (timeEarliestToReach-10080)) || (timeOldTrainDeptStation1 > timeNodeStart)) {
                                    totalUpTrack--;
                                    // System.out.println("Track Same: "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                    //         oldTrainDeptStation1.toString()+ " "+ oldTrainArrStation2.toString()+ " "+train.getTrainNo());
                                }
                            }
                            else{
                                if((timeNodeStart<timeOldTrainDeptStation1) && (timeEarliestToReach > timeOldTrainArrStation2) ||
                                        ((timeNodeStart > timeOldTrainDeptStation1) && (timeEarliestToReach < timeOldTrainArrStation2))){
                                    totalUpTrack--;
                                    // System.out.println("Track Same: "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                    //         oldTrainDeptStation1.toString()+ " "+ oldTrainArrStation2.toString()+ " "+train.getTrainNo());
                                }
                            }
                        }

                        s2 = train.getStationInfo(nodeEnd.getStationId(), nodeStart.getStationId(),true);
                        s1 = train.getStationInfo(nodeEnd.getStationId(), nodeStart.getStationId(), false);


                        if(s1!=null && s2 !=null){
                            //opposite direction train
                            TrainTime oldTrainArrStation1 = s1.getArr();
                            TrainTime oldTrainDeptStation2 = s2.getDept();

                            int timeOldTrainArrStation1 = oldTrainArrStation1.getValue();
                            int timeOldTrainDeptStation2 = oldTrainDeptStation2.getValue();

                            boolean addedOldTrain = false;
                            if(timeOldTrainArrStation1<timeOldTrainDeptStation2){
                                addedOldTrain=true;
                                timeOldTrainArrStation1 += 10080;
                            }

                            if((timeOldTrainArrStation1+minDelayBwTrains)>= timeNodeStart &&
                                    (timeOldTrainArrStation1-minDelayBwTrains)<= timeNodeStart){
                                totalUpTrack--;
                                // System.out.println("Track Min delay Opposite1: "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                //         oldTrainDeptStation2.toString()+ " "+ oldTrainArrStation1.toString()+ " "+train.getTrainNo());
                                continue;
                            }

                            if((timeOldTrainDeptStation2+minDelayBwTrains)>= timeEarliestToReach &&
                                    (timeOldTrainDeptStation2-minDelayBwTrains)<= timeEarliestToReach){
                                totalUpTrack--;
                                // System.out.println("Track Min delay Opposite2: "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                //         oldTrainDeptStation2.toString()+ " "+ oldTrainArrStation1.toString()+ " "+train.getTrainNo());
                                continue;
                            }

                            if(timeEarliestToReach>=10080 && addedOldTrain){
                                if((timeNodeStart<timeOldTrainArrStation1) && (timeEarliestToReach > timeOldTrainDeptStation2)){
                                    totalUpTrack--;
                                    // System.out.println("Track Opposite: "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                    //         oldTrainDeptStation2.toString()+ " "+ oldTrainArrStation1.toString()+ " "+train.getTrainNo());
                                }
                            }
                            else if(addedOldTrain){
                                timeOldTrainArrStation1 -=10080;
                                if((timeEarliestToReach> timeOldTrainDeptStation2) || (timeNodeStart < timeOldTrainArrStation1)){
                                    totalUpTrack --;
                                    // System.out.println("Track Opposite: "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                    //         oldTrainDeptStation2.toString()+ " "+ oldTrainArrStation1.toString()+ " "+train.getTrainNo());
                                }
                            }
                            else if(timeEarliestToReach>=10080){
                                if(((timeEarliestToReach-10080)> timeOldTrainDeptStation2) || (timeNodeStart < timeOldTrainArrStation1)){
                                    totalUpTrack --;
                                    // System.out.println("Track Opposite: "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                    //         oldTrainDeptStation2.toString()+ " "+ oldTrainArrStation1.toString()+ " "+train.getTrainNo());
                                }
                            }
                            else{
                                if((timeNodeStart<timeOldTrainArrStation1) && (timeEarliestToReach > timeOldTrainDeptStation2)){
                                    totalUpTrack--;
                                    // System.out.println("Track Opposite: "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                    //         oldTrainDeptStation2.toString()+ " "+ oldTrainArrStation1.toString()+ " "+train.getTrainNo());
                                }
                            }
                        }
                    }
                }
            }
            else if(nodeStart.getStationId().equalsIgnoreCase("source")){
                for (Train train : this.trainMap.values()) {
                    TrainAtStation s2 = train.getStationInfo(nodeEnd.getStationId(), 0);
                    if (s2 == null) {
                        continue;
                    }
                    //check platform requirement
                    int count = 0;
                    do{
                        boolean addedOldTrain = false;
                        TrainTime oldTrainArrStation2 = s2.getArr();
                        TrainTime oldTrainDeptStation2 = s2.getDept();
                        if(oldTrainArrStation2==null || oldTrainDeptStation2==null){
                            // System.out.println("Error in storing stoppage info info :"+ train.getTrainNo()+ " "+ nodeEnd.getStationId());
                            s2= train.getStationInfo(nodeEnd.getStationId(), ++count);
                            continue;
                        }

                        int timeOldTrainArrStation2 = oldTrainArrStation2.getValue();
                        int timeOldTrainDeptStation2 = oldTrainDeptStation2.getValue();

                        if(oldTrainDeptStation2.day==0 && oldTrainArrStation2.day==6){
                            addedOldTrain = true;
                        }

                        if(!( isDirectLineAvailable && timeOldTrainArrStation2==timeOldTrainDeptStation2)){
                            //need to check availability of platform
                            if(addedNodeEnd && addedOldTrain) {
                                totalUpPlatform--;
                                // System.out.println("Platform : "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                //         oldTrainArrStation2.toString()+ " "+ oldTrainDeptStation2.toString()+ " "+train.getTrainNo());
                            }
                            else if(addedOldTrain){
                                if((timeNodeEnd>=timeOldTrainArrStation2 )|| ( timeEarliestToReach<=timeOldTrainDeptStation2)){
                                    totalUpPlatform--;
                                    // System.out.println("Platform : "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                    //         oldTrainArrStation2.toString()+ " "+ oldTrainDeptStation2.toString()+ " "+train.getTrainNo());
                                }
                            }
                            else if(addedNodeEnd){
                                if(timeEarliestToReach>=10080){
                                    if(!((timeNodeEnd-10080)<=timeOldTrainArrStation2 || (timeEarliestToReach-10080) >=timeOldTrainDeptStation2)){
                                        totalUpPlatform--;
                                        // System.out.println("Platform : "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                        //         oldTrainArrStation2.toString()+ " "+ oldTrainDeptStation2.toString()+ " "+train.getTrainNo());
                                    }
                                }
                                else {
                                    if (((timeNodeEnd - 10080) >= timeOldTrainArrStation2) || (timeEarliestToReach <= timeOldTrainDeptStation2)) {
                                        totalUpPlatform--;
                                        // System.out.println("Platform : "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                        //         oldTrainArrStation2.toString()+ " "+ oldTrainDeptStation2.toString()+ " "+train.getTrainNo());
                                    }
                                }
                            }
                            else if(!(timeNodeEnd<=timeOldTrainArrStation2 || timeEarliestToReach >=timeOldTrainDeptStation2)){
                                totalUpPlatform--;
                                // System.out.println("Platform : "+nodeStart.toString()+ " "+ delayBwStation+" "+ nodeEnd.toString()+ " "+
                                //         oldTrainArrStation2.toString()+ " "+ oldTrainDeptStation2.toString()+ " "+train.getTrainNo());
                            }
                        }
                        s2= train.getStationInfo(nodeEnd.getStationId(), ++count);
                    }
                    while(s2!=null);
                }
            }
            else if(nodeEnd.getStationId().equalsIgnoreCase("dest")){
                return 1;
            }

            if((totalUpTrack+ totalDualTrack+ totalDownTrack)<0){
                // System.out.println("Track Constraint: "+ bakTotalUpT+ " "+nodeStart.toString()+ " "+ nodeEnd.toString()+ " "+
                //         obstructingTrains1.toString()+" > "+ obstructingTrains2.toString()+" >> "+
                //         obstructingTrains3.toString()+" >>> "+ obstructingTrains4.toString());
                // System.out.print("Track Constraint: "+nodeStart.toString()+ " "+ nodeEnd.toString()+ " ");
                return -2;
            }

            if((totalUpPlatform+totalDualPlatform+ totalDownPlatform)<0) {
                // System.out.println("Platform Constraint: "+ bakTotalUpP+ " "+nodeStart.toString()+ " "+ nodeEnd.toString()+ " "+
                //         obstructingTrains1.toString()+" > "+ obstructingTrains2.toString()+" >> "+
                //         obstructingTrains3.toString()+" >>> "+ obstructingTrains4.toString());
                // System.out.print("Platform Constraint: "+nodeStart.toString()+ " "+ nodeEnd.toString()+ " ");
                return -3;
            }
            else{
                return 2;
            }
        }
        else if(timeEarliestToDepart>timeNodeEnd){
            // System.out.print("Rejected as earliest to depart is more than node time :" + nodeStart.toString() +
            //         " -> " + nodeEnd.toString()+" "+ delayBwStation+" ");
            return -4;
        }
        else{
            return -5;
        }
    }

    private boolean addEdgeBwStations(int i, int delayBwStation, int waitTimeStationEnd,
                                      boolean isSingleDay, int minDelayBwTrains, boolean aroundSourceTime, int trainDay,
                                      List<Integer> maxDelayList, TrainTime sourceTime){
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

        // System.out.println("Max delay :"+ maxDelayList.get(i)+ " for i: "+ i);

        for(int j=0;j<this.nodes.get(i).size();j++) {
            // System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS").format(new Date())+" loop 1st :"+ j);
            nodeStart = this.nodes.get(i).get(j);
            if(nodeStart.getInEdgeCount()<=0 && !nodeStart.getStationId().equalsIgnoreCase("source")){
                // System.out.println("No incoming edge: "+ nodeStart.toString());
                continue;
            }

            int countLoopMax = maxDelayList.get(i)+1;
            if(countLoopMax>this.nodes.get(i+1).size() || nodeStart.getStationId().equalsIgnoreCase("source")){
                countLoopMax = this.nodes.get(i+1).size();
            }

            for(int countLoop= 0;countLoop<countLoopMax; countLoop++) {
                // System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS").format(new Date())+" loop 2nd :"+ countLoop);
                int k = Math.floorMod(j+countLoop,this.nodes.get(i+1).size());
                nodeEnd = this.nodes.get(i+1).get(k);
                if(nodeStart.getTime()==null || nodeEnd.getTime()==null){
                    if(isSingleDay && nodeEnd.getTime()!=null && nodeEnd.getTime().day!=trainDay){
                        continue;
                    }
                    if(sourceTime!=null && nodeStart.getStationId().equalsIgnoreCase("source")&&
                            nodeEnd.getTime()!=null && (nodeEnd.getTime().getValue()<sourceTime.getValue() ||
                            nodeEnd.getTime().getValue()>(sourceTime.getValue()+240))){
                        continue;
                    }
                    if(this.graphKBestPath.addEdge(new Edge(nodeStart, nodeEnd,0))){
                        this.edgeCount++;
                        nodeStart.incrementOutEdgeCount();
                        nodeEnd.incrementInEdgeCount();
                    }
                    else{
                        System.err.println("Some error occurred in adding edge bw " + nodeStart.toString() +
                                " and " + nodeEnd.toString() + " cost 0");
                    }
                }
                else if(nodeStart.getStationId().equalsIgnoreCase("source")){
                    boolean validEdge = false;
                    TrainTime tempTrainTime = new TrainTime(6,23,54);

                    do{
                        int codeValidEdge = isValidEdge(delayBwStation, waitTimeStationEnd, nodeStart, nodeEnd,
                                maxDelayList.get(i), minDelayBwTrains, totalUpPlatform, totalDownPlatform,
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
                                edgeCost+= 10080;
                            }
                        }
                        if(this.graphKBestPath.addEdge(new Edge(nodeStart, nodeEnd,edgeCost))){
                            this.edgeCount++;
                            nodeStart.incrementOutEdgeCount();
                            nodeEnd.incrementInEdgeCount();
                        }
                        else{
                            System.err.println("Some error occurred in adding edge bw " + nodeStart.toString() +
                                    " and " + nodeEnd.toString()+  " cost 0");
                        }
                    }
                }
                else{
                    int codeValidEdge = isValidEdge(delayBwStation, waitTimeStationEnd, nodeStart,
                            nodeEnd, maxDelayList.get(i), minDelayBwTrains,totalUpPlatform, totalDownPlatform,
                            totalDualPlatform,totalUpTrack,totalDownTrack,totalDualTrack, isDirectLineAvailable);
                    // System.out.println(codeValidEdge+" ");
                    if(codeValidEdge==-5){
                        break;
                    }
                    else if(codeValidEdge>0){
                        int edgeCost = nodeEnd.getTime().compareTo(nodeStart.getTime());
                        if(edgeCost<0){
                            edgeCost+= 10080;
                        }
                        if(this.graphKBestPath.addEdge(new Edge(nodeStart, nodeEnd,edgeCost))){
                            this.edgeCount++;
                            nodeStart.incrementOutEdgeCount();
                            nodeEnd.incrementInEdgeCount();
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
    scheduleKBestPathOptimized(String pathTemp, int noOfPaths, TrainTime sourceTime,
                               int minDelayBwTrains, List<Integer> stopTime,
                               double avgSpeed, boolean isSingleDay,
                               boolean usePreviousComputation, List<Double> maxCostList, boolean aroundSourceTime, int trainDay,
                               List<Integer> maxDelayList){
        try{
            this.graphKBestPath = new GraphKBestPath(usePreviousComputation, pathTemp);
            if(!usePreviousComputation) {
                getStationList();
                getNodesFreeSlot();
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
                    if (!addEdgeBwStations(i, delayBwStation, waitTimeStationEnd,
                            isSingleDay, minDelayBwTrains, aroundSourceTime, trainDay, maxDelayList, sourceTime)) {
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
                                          String pathOldTrainSchedule, int trainDay, boolean isSingleDay,
                                          boolean usePreviousComputation, double ratio, boolean aroundSourceTime) {
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
        if(!addTrainFromFolder(pathOldTrainSchedule)){
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
        List<Integer> maxDelayList = new ArrayList<>();
        if(stationDistanceList.size()!=stopTime.size()){
            System.out.println("Station distance and stop time size does not match");
            return Collections.emptyList();
        }
        if(aroundSourceTime){
            maxCostList.add(0.0);
            maxDelayList.add(0);
        }
        else{
            maxCostList.add(60.0);
            maxDelayList.add(60);
        }

        double startDistance = stationDistanceList.get(0);
        double sumStopTimes = 0;
        double previousDistance = 0;
        double initialDelay;
        initialDelay = maxDelayList.get(0);
        for(int i=0;i<stationDistanceList.size();i++){
            sumStopTimes +=stopTime.get(i);
            int temp = (int)Math.ceil(((stationDistanceList.get(i)-startDistance)/avgSpeed)*60);
            int temp1 = (int)Math.ceil(((stationDistanceList.get(i)-previousDistance)/avgSpeed)*60);
            if(i>0) {
                maxDelayList.add(temp1*5);
            }
            maxCostList.add((temp) * ratio+ sumStopTimes+ initialDelay);
            previousDistance = stationDistanceList.get(i);
        }
        maxCostList.add(maxCostList.get(maxCostList.size()-1));
        maxDelayList.add(0);
        System.out.println(maxCostList.toString());
        System.out.println(maxDelayList.toString());
        List<Path> paths;
        paths= scheduleKBestPathOptimized(pathTemp, noOfPaths, sourceTime,
                minDelayBwTrains, stopTime,avgSpeed, isSingleDay, usePreviousComputation, maxCostList, aroundSourceTime, trainDay,
                maxDelayList);
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
