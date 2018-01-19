import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class KBestSchedule {

    private Route route;
    private final Map<Integer, Train> trainMap;

    KBestSchedule(){
        trainMap = new HashMap<>();
    }

     public static String getNodeLabel(String id, LocalTime time) {
        requireNonNull(id, "Station id is null.");
        if(time==null){
            return id.toLowerCase();
        }
        else{
            return (id+":"+time.toString()).toLowerCase();
        }
    }

    private static Pair<String, LocalTime> getNodeData(String label) {
        requireNonNull(label, "Node label is null.");
        String[] labelData = label.split(":");
        Pair<String, LocalTime> pair = new Pair<>();
        try {
            if(!pair.updateFirst(labelData[0])){
                throw new IllegalArgumentException("Illegal label");
            }
            if (labelData.length == 3 && !pair.updateSecond(LocalTime.of(Integer.parseInt(labelData[1]), Integer.parseInt(labelData[2])))){
                throw new IllegalArgumentException("Illegal label");
            }
        }
        catch (Exception e){
            System.out.println("Invalid time info for node");
        }
        return pair;
    }

    private static int getTimeDiff(LocalTime localTime1, LocalTime localTime2) {
        requireNonNull(localTime1, "Time1 is null.");
        requireNonNull(localTime2, "Time2 is null.");
        int startHrs = localTime2.getHour();
        int startMinutes = localTime2.getMinute();
        int endHrs = localTime1.getHour();
        int endMinutes = localTime1.getMinute();
        int diff;
        if(endHrs>startHrs || (endHrs==startHrs && endMinutes>=startMinutes)) {
            diff = (endHrs-startHrs)*60 + (endMinutes- startMinutes);
        }
        else {
            diff = (24 + endHrs -startHrs )* 60 + endMinutes-startMinutes;
        }
        return diff;
    }

    private boolean addRoute(List<String> stationIdList, List<String> stationNameList, List<Double> stationDistanceList){
        requireNonNull(stationIdList, "Station id list is null.");
        requireNonNull(stationNameList, "Station name list is null.");
        requireNonNull(stationDistanceList, "Station distance list is null.");
        this.route = new Route();
        if(stationIdList.size() != stationNameList.size() || stationNameList.size() != stationDistanceList.size()){
            throw new IllegalArgumentException("Invalid arguments for route");
        }
        for(int i=0;i<stationIdList.size();i++){
            if(!route.addStation(stationIdList.get(i), stationNameList.get(i),stationDistanceList.get(i))){
                throw new RuntimeException("Unable to add station to route");
            }
        }
        return true;
    }

    private boolean addTrain(int trainNo, String trainName){
        requireNonNull(trainName, "Train name is null.");
        trainMap.put(trainNo, new Train(trainNo, trainName));
        return true;
    }

    private boolean addStoppageTrain(int trainNo, String stationId, LocalTime arrival, LocalTime departure){
        Train train = this.trainMap.get(trainNo);
        Station station = this.route.getStation(stationId);
        //station==null represents that station is not in the route.
        return station==null || (train !=null && train.addStoppage(station,arrival,departure));
    }

    private boolean addTrainFromFile(int trainNo, String trainName, String pathTrainSchedule){
        try {
            if(!addTrain(trainNo, trainName)){
                return false;
            }
            FileReader fReader = new FileReader(pathTrainSchedule);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            LocalTime arrival, departure;
            String stationId;
            while((line = bReader.readLine()) != null) {
                String data[] = line.split("\\s+");
                String stationCode[] = data[0].split("-");
                stationId = stationCode[stationCode.length-1];
                String data1[] =data[1].split(":");
                arrival = LocalTime.of(Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                data1 =data[2].split(":");
                departure = LocalTime.of(Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
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

    private boolean addTrainFromFolder(String pathOldTrainScheduleFolder){
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
                if(!addTrainFromFile(trainNo,file.getName(),file.getPath())){
                    return false;
                }
            }
        }

        return true;
    }

    private List<List<String>> getNodesFreeSlot(int minDelayBwTrains, LocalTime sourceTime, LocalTime destTime,int startHrs, int startMinutes, int endHrs, int endMinutes){
        List<List<String>> nodes = this.route.getFreeSlots(minDelayBwTrains, startHrs,startMinutes,endHrs,endMinutes);
        if(nodes==null){
            return Collections.emptyList();
        }
        List<String> nodeSrcList = new ArrayList<>();
        nodeSrcList.add(getNodeLabel("source",sourceTime));
        nodes.add(0,nodeSrcList);
        List<String> nodeDestList = new ArrayList<>();
        nodeDestList.add(getNodeLabel("dest", destTime));
        nodes.add(nodeDestList);
        return nodes;
    }

    private List<String> getStationList(){
        List<String> stationList = new ArrayList<>(this.route.getStationList());
        stationList.add(0,"source");
        stationList.add("dest");
        return stationList;
    }

    private boolean isValidEdge(double distanceBwStation, double avgSpeed, double waitTimeStationEnd, LocalTime nodeStartTime, LocalTime nodeEndTime, int maxDelayBwStations, String nodeStartId, String nodeEndId){
        int delay = Scheduler.ceilOfDecimal((((distanceBwStation)/avgSpeed )*60));
        delay = (delay + (int)(waitTimeStationEnd/1));
        LocalTime earliestTimeToReach = Scheduler.addMinutes(nodeStartTime, delay);
        LocalTime maxTimeToReach = Scheduler.addMinutes(earliestTimeToReach, maxDelayBwStations);
        int compEarliestAndNode = earliestTimeToReach.compareTo(nodeEndTime);
        int compNodeAndMax = nodeEndTime.compareTo(maxTimeToReach);
        int caseId=0;
        if(maxTimeToReach.compareTo(earliestTimeToReach)<=0) {
            if(earliestTimeToReach.compareTo(nodeEndTime)<=0 && nodeEndTime.compareTo(maxTimeToReach)>=0) {
                compEarliestAndNode = -1;
                compNodeAndMax = -1;
                caseId = 1;
            }
            else if(nodeEndTime.compareTo(maxTimeToReach)<=0 && nodeEndTime.compareTo(earliestTimeToReach)<=0) {
                compEarliestAndNode = -1;
                compNodeAndMax = -1;
                caseId = 2;
            }
        }

        if(compEarliestAndNode <=0 && compNodeAndMax <=0){
            if(!nodeStartId.equalsIgnoreCase("source") && !nodeEndId.equalsIgnoreCase("dest") ) {
                for (Train train : this.trainMap.values()) {
                    LocalTime oldTrainDept = train.getDept(nodeStartId);
                    LocalTime oldTrainArr = train.getArr(nodeEndId);
                    int compOldTrainDeptAndNodeStart = oldTrainDept.compareTo(nodeStartTime);
                    int compOldTrainArrAndNodeEnd = oldTrainArr.compareTo(nodeEndTime);

                    if (oldTrainDept.compareTo(oldTrainArr) > 0) {
                        //need to take care of next day stoppage trains
                        if (caseId == 0 || caseId == 1) {
                            compOldTrainArrAndNodeEnd = 1;
                        }
                    } else {
                        if (caseId == 2) {
                            compOldTrainArrAndNodeEnd = -1;
                        }
                    }

                    if (compOldTrainDeptAndNodeStart != compOldTrainArrAndNodeEnd) {
                        //if depart at same time...  must arrive at same time at next station.
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private List<Path<String>> scheduleKBestPathOptimized(int noOfPaths, LocalTime sourceTime, LocalTime destTime, int maxDelayBwStations, int minDelayBwTrains, ArrayList<Double> stopTime, double avgSpeed, int startHrs, int startMinutes, int endHrs, int endMinutes){
        try{
            List<String> stationList = getStationList();
            List<List<String>> nodes = getNodesFreeSlot(minDelayBwTrains, sourceTime, destTime, startHrs, startMinutes, endHrs, endMinutes);
            if(nodes==null || stationList==null || nodes.isEmpty() || stationList.isEmpty()){
                System.out.println("Error in loading data");
                return Collections.emptyList();
            }
            if(nodes.size() != stationList.size()) {
                System.out.println("Invalid nodes in graph... exiting");
                return Collections.emptyList();
            }
            GraphKBestPath<String> graphKBestPath = new GraphKBestPath<>();
            long edgeCount=0;
            System.out.println("Nodes size: " +nodes.size());
            String nodeStartId;
            LocalTime nodeStartTime;
            String nodeStartLabel;
            String  nodeEndId;
            LocalTime nodeEndTime;
            String nodeEndLabel;
            Pair<String, LocalTime> pairNodeStartData;
            Pair<String, LocalTime> pairNodeEndData;

            double distanceStationStart;
            double distanceStationEnd = 0.0;
            double waitTimeStationEnd = 0.0;
            double distanceBwStation;

            for(int i=0;i<stationList.size()-1;i++) {
                distanceStationStart = distanceStationEnd;
                if(i < stationList.size()-2) {
                    distanceStationEnd = this.route.getStation(stationList.get(i+1)).getDistance();
                    waitTimeStationEnd = stopTime.get(i);
                }
                if(i==0){
                    distanceBwStation = 0;
                }
                else {
                    distanceBwStation = distanceStationEnd - distanceStationStart;
                }

                if(nodes.get(i).isEmpty()) {
                    System.out.println("No path found as no available slot for station "+ stationList.get(i));
                    return Collections.emptyList();
                }
                if(nodes.get(i+1).isEmpty()) {
                    System.out.println("No path found as no available slot for station "+ stationList.get(i+1));
                    return Collections.emptyList();
                }

                if(!nodes.get(i).get(0).split(":")[0].equalsIgnoreCase(stationList.get(i)) || !nodes.get(i+1).get(0).split(":")[0].equalsIgnoreCase(stationList.get(i+1))){
                    System.out.println("Invalid path Info.");
                    return Collections.emptyList();
                }

                for(int j=0;j<nodes.get(i).size();j++) {
                    nodeStartLabel = nodes.get(i).get(j);
                    pairNodeStartData = getNodeData(nodeStartLabel);
                    nodeStartId = pairNodeStartData.getFirst();
                    nodeStartTime = pairNodeStartData.getSecond();
                    for(int k=0;k<nodes.get(i+1).size();k++) {
                        nodeEndLabel = nodes.get(i+1).get(k);
                        pairNodeEndData = getNodeData(nodeEndLabel);
                        nodeEndId = pairNodeEndData.getFirst();
                        nodeEndTime = pairNodeEndData.getSecond();
                        if(nodeStartTime==null || nodeEndTime==null){
                            if(graphKBestPath.addEdge(new Edge<>(nodeStartLabel, nodeEndLabel,0))){
                                edgeCount++;
                            }
                        }
                        else{
                            if(isValidEdge(distanceBwStation, avgSpeed, waitTimeStationEnd, nodeStartTime, nodeEndTime, maxDelayBwStations, nodeStartId, nodeEndId)){
                                int edgeCost = getTimeDiff(nodeEndTime, nodeStartTime);
                                if(edgeCost >= 0 && graphKBestPath.addEdge(new Edge<>(nodeStartLabel, nodeEndLabel,edgeCost))){
                                    edgeCount++;
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("Edge size: " +edgeCount);
            // graphKBestPath.printInfo();
            List<Path<String>> paths;
            DefaultKShortestPathFinder<String> defaultKShortestPathFinder = new DefaultKShortestPathFinder<>();

            paths= defaultKShortestPathFinder.findShortestPaths(getNodeLabel("source",sourceTime), getNodeLabel("dest", destTime), graphKBestPath, noOfPaths);

            return paths;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public List<Path<String>> getScheduleNewTrain(List<String> stationIdList, List<String> stationNameList, List<Double> stationDistanceList, int noOfPaths, LocalTime sourceTime, LocalTime destTime, int minDelayBwTrains, double avgSpeed , ArrayList<Double> stopTime, String pathOldTrainSchedule) {
        if(!addRoute(stationIdList,stationNameList,stationDistanceList) || !addTrainFromFolder(pathOldTrainSchedule)){
            return Collections.emptyList();
        }
        // route.printInfo();
        int startHrs = 0;
        int startMinutes = 0;
        int endHrs = 23;
        int endMinutes=59;
        int maxDelayBwStations = 60;
        if(stopTime.size() != this.route.getNumberOfStation()){
            System.out.println("Please give stop time for every station in route. if it does not stop at any particular station, give stop time as 0.");
            return Collections.emptyList();
        }
        return scheduleKBestPathOptimized(noOfPaths, sourceTime, destTime, maxDelayBwStations, minDelayBwTrains, stopTime,avgSpeed,startHrs,startMinutes,endHrs,endMinutes);
    }
}
