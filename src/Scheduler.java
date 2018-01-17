import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Scheduler {

    private Route route;
    private ArrayList<Train> TRAIN_LIST = new ArrayList<>();
    private ArrayList<Station> PATH = new ArrayList<>();

    private static Double nextDecimal(Double number) {
        Double temp = number%1;
        if(temp>0) {
            number=number/1 + 1.0;
        }
        return number;
    }

    public static String getNodeLabel(String id, LocalTime time) {
        if(time==null){
            return id.toLowerCase();
        }
        else{
            return (id+":"+time.toString()).toLowerCase();
        }
    }

    private static Pair<String, LocalTime> getNodeData(String label) {
        String[] labelData = label.split(":");
        Pair<String, LocalTime> pair = new Pair<>();
        try {
            if(!pair.updateFirst(labelData[0])){
                return null;
            }
            if (labelData.length == 3) {
                if(!pair.updateSecond(LocalTime.of(Integer.parseInt(labelData[1]), Integer.parseInt(labelData[2])))){
                    return null;
                }
            }
        }
        catch (Exception e){
            System.out.println("Invalid time info for node");
        }
        return pair;
    }

    public static LocalTime addMinutes(LocalTime localTime, int minutes) {
        if(minutes>60) {
            localTime = addMinutes(localTime, (minutes- 60));
            minutes = 60;
        }
        int hrs = localTime.getHour();
        minutes += localTime.getMinute();

        if(minutes>=60) {
            hrs++;
            minutes = minutes-60;
            if(hrs>=24) {
                hrs=0;
            }
        }
        return LocalTime.of(hrs, minutes);
    }

    public static LocalTime subMinutes(LocalTime localTime, int minutes) {
        if(minutes>60) {
            localTime = subMinutes(localTime, (minutes- 60));
            minutes = 60;
        }
        int hrs = localTime.getHour();
        minutes = localTime.getMinute()-minutes;

        if(minutes<0) {
            hrs--;
            minutes = minutes+60;
            if(hrs<0) {
                hrs=23;
            }
        }
        return LocalTime.of(hrs, minutes);
    }

    private static Integer getTimeDiff(LocalTime localTime1, LocalTime localTime2) {

        int startHrs = localTime2.getHour();
        int startMinutes = localTime2.getMinute();
        int endHrs = localTime1.getHour();
        int endMinutes = localTime1.getMinute();
        int diff;
        if(endHrs>startHrs || (endHrs==startHrs && endMinutes>startMinutes)) {
            diff = (endHrs-startHrs)*60 + (endMinutes- startMinutes);
        }
        else {
            diff = (24 + endHrs -startHrs )* 60 + endMinutes-startMinutes;
        }
        return diff;
    }

    private Double analyzeRouteFile(String pathRoute){
        Double destDist=0.0;
        try {
            FileReader fReader;
            BufferedReader bReader;
            fReader = new FileReader(pathRoute);
            bReader = new BufferedReader(fReader);
            String line;
            while((line = bReader.readLine()) != null) {
                String data[] = line.split("\\s+");
                String station_code[] = data[0].split("-");
                String stationId = station_code[station_code.length-1];
                Double stationDist = Double.parseDouble(data[1]);
                this.PATH.add(new Station(stationId, data[0], stationDist));
                destDist = stationDist;
                // if(stationId.equalsIgnoreCase("ara")){
                //     break;
                // }
            }
            fReader.close();
            bReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        this.route = new Route(this.PATH);
        return destDist;
    }
    
    private Double analyzeOldTrainScheduleFile(String pathRoute, String pathOldTrainSchedule){
        Double destDist = analyzeRouteFile(pathRoute);
        
        File[] listOfFiles = new File(pathOldTrainSchedule).listFiles();
        int newTrainNo = 1;
        
        if(listOfFiles==null) {
            System.out.println("No old trains found");
            return destDist;
        }

        FileReader fReader;
        BufferedReader bReader;

        for (File file: listOfFiles) {
            if (file.isFile()) {
                String TrainNo = file.getName().split("\\.")[0];
                Train train;
                try {
                    train = new Train(Integer.parseInt(TrainNo), "--");
                }
                catch (NumberFormatException e) {
                    train = new Train(newTrainNo++, "--");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                try {
                    fReader = new FileReader(file);
                    bReader = new BufferedReader(fReader);
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
                        
                        Station station = route.getStation(stationId);
                        
                        if(station!=null) {
                            if(!train.addStoppage(station, arrival, departure)){
                                System.out.println("Error in adding station" + station.getName() +"to train" +train.getName() +" " + train.getTrainNo());
                            }
                        }
                        else{
                            System.out.println("Invalid station id: " + stationId);
                        }
                    }
                    fReader.close();
                    bReader.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                TRAIN_LIST.add(train);
            }
        }
        return destDist;
    }

    private void writeToFilePaths(List<Path<String>> paths, String pathBestRouteFile, ArrayList<Double> waitTime, Double avgSpeed){
        try {
            BufferedWriter bWriter;
            FileWriter fWriter;
            System.out.println(paths.size());
            ArrayList<Node> pseudoResultNodeList = new ArrayList<>();
            for (int i = 0; i < this.PATH.size(); i++) {
                pseudoResultNodeList.add(new Node(null, this.PATH.get(i).getId(), this.PATH.get(i).getDistance(), waitTime.get(i)));
            }
            int countPath = 0;
            for (Path<String> path : paths) {
                countPath++;
                ArrayList<Node> nodePathBestRoute = new ArrayList<>();
                List<String> nodeResultList = path.getNodeList();
                for (int i = 1; i < nodeResultList.size() - 1; i++) {
                    String nodeResult = nodeResultList.get(i);
                    String nodeResultData[] = nodeResult.split(":");
                    String stationIdResult = nodeResultData[0];
                    LocalTime timeResult = LocalTime.of(Integer.parseInt(nodeResultData[1]), Integer.parseInt(nodeResultData[2]));
                    Node tempPseudoResultNode = pseudoResultNodeList.get(i - 1);
                    if (!tempPseudoResultNode.getStationId().equalsIgnoreCase(stationIdResult)) {
                        System.out.println("Invalid path found... ");
                        break;
                    }
                    nodePathBestRoute.add(new Node(timeResult, stationIdResult, tempPseudoResultNode.getDistance(), tempPseudoResultNode.getWaitTime()));
                }
                System.out.println(path.toString() + " cost: " + path.pathCost());
                String arrivalTimeStation;
                Double distancePrevStation = 0.0;
                LocalTime timePrevStation = null;
                fWriter = new FileWriter(pathBestRouteFile + " path " + countPath + " cost " + path.pathCost());
                bWriter = new BufferedWriter(fWriter);

                for (Node bestRouteNode : nodePathBestRoute) {
                    if (timePrevStation != null) {
                        int delay = (int) (nextDecimal((bestRouteNode.getDistance() - distancePrevStation) / avgSpeed * 60) / 1);
                        arrivalTimeStation = addMinutes(timePrevStation, delay) + "";
                    } else {
                        arrivalTimeStation = subMinutes(bestRouteNode.getTime(), 2) + "";
                    }
                    bWriter.write(bestRouteNode.getStationId() + "\t" + arrivalTimeStation + "\t" + bestRouteNode.getTime() + "\t" + bestRouteNode.getDistance());
                    bWriter.write("\n");
                    distancePrevStation = bestRouteNode.getDistance();
                    timePrevStation = bestRouteNode.getTime();
                }
                bWriter.close();
                fWriter.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private ArrayList<ArrayList<String>> getNodesFreeSlot(LocalTime sourceTime, LocalTime destTime,int startHrs, int startMinutes, int endHrs, int endMinutes){
        ArrayList<ArrayList<String>> nodes = this.route.getFreeSlots(startHrs,startMinutes,endHrs,endMinutes);
        if(nodes==null){
            return null;
        }
        ArrayList<String> nodeSrcList = new ArrayList<>();
        nodeSrcList.add(getNodeLabel("source",sourceTime));
        nodes.add(0,nodeSrcList);
        ArrayList<String> nodeDestList = new ArrayList<>();
        nodeDestList.add(getNodeLabel("dest", destTime));
        nodes.add(nodeDestList);
        return nodes;
    }

    private ArrayList<String> getStationList(){
        ArrayList<String> stationList = this.route.getStationList();
        if(stationList==null){
            return null;
        }
        stationList.add(0,"source");
        stationList.add("dest");
        return stationList;
    }

    private int checkIfValidCase(Double distanceBwStation, Double avgSpeed, Double waitTimeStationEnd, LocalTime nodeStartTime, LocalTime nodeEndTime, int maxDelayBwStations){
        int delay = (int)(nextDecimal(((distanceBwStation/avgSpeed )*60))/1);
        delay = (delay + (int)(waitTimeStationEnd/1));
        LocalTime earliestTimeToReach = addMinutes(nodeStartTime, delay);
        LocalTime maxTimeToReach = addMinutes(earliestTimeToReach, maxDelayBwStations);

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
            return caseId;
        }
        return -1;
    }

    private boolean checkIfCrossingWithAnotherTrain(String nodeStartId, String nodeEndId, LocalTime nodeStartTime, LocalTime nodeEndTime, int caseId){
        boolean crossAnotherTrain = false;
        if(!nodeStartId.equalsIgnoreCase("source") && !nodeEndId.equalsIgnoreCase("dest") ) {
            for (Train train : TRAIN_LIST) {
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
                    crossAnotherTrain = true;
                    break;
                }
            }
        }
        return crossAnotherTrain;
    }

    @SuppressWarnings("unused")
    private void scheduleKBestPathOptimized(int noOfPaths, LocalTime sourceTime, LocalTime destTime, int maxDelayBwStations, ArrayList<Double> waitTime, Double destDist, String pathBestRouteFile, Double avgSpeed, int startHrs, int startMinutes, int endHrs, int endMinutes){
        long milli = new Date().getTime();
        System.out.println("*********************************************************");
        System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        System.out.println("Avg speed: " + avgSpeed);
        System.out.println("Start: "+startHrs+":"+startMinutes);
        System.out.println("End: "+ endHrs+":"+startMinutes);
        try{
            ArrayList<String> stationList = getStationList();
            ArrayList<ArrayList<String>> nodes = getNodesFreeSlot(sourceTime, destTime, startHrs, startMinutes, endHrs, endMinutes);
            if(nodes==null || stationList==null){
                System.out.println("Error in loading data");
                return;
            }
            if(nodes.size() != stationList.size()) {
                System.out.println("Invalid nodes in graph... exiting");
                return;
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

            Double distanceStationStart;
            Double distanceStationEnd = 0.0;
            Double waitTimeStationEnd = 0.0;
            Double distanceBwStation;

            for(int i=0;i<stationList.size()-1;i++) {
                distanceStationStart = distanceStationEnd;
                if(i<this.PATH.size()) {
                    distanceStationEnd = this.PATH.get(i).getDistance();
                    waitTimeStationEnd = waitTime.get(i);
                }
                distanceBwStation = distanceStationEnd - distanceStationStart;

                if(nodes.get(i).isEmpty()) {
                    System.out.println("No path found as no available slot for station "+ stationList.get(i));
                    return;
                }
                if(nodes.get(i+1).isEmpty()) {
                    System.out.println("No path found as no available slot for station "+ stationList.get(i+1));
                    return;
                }

                if(!nodes.get(i).get(0).split(":")[0].equalsIgnoreCase(stationList.get(i)) || !nodes.get(i+1).get(0).split(":")[0].equalsIgnoreCase(stationList.get(i+1))){
                    System.out.println("Invalid path Info.");
                    return;
                }

                for(int j=0;j<nodes.get(i).size();j++) {
                    nodeStartLabel = nodes.get(i).get(j);
                    pairNodeStartData = getNodeData(nodeStartLabel);
                    if(pairNodeStartData==null){
                        System.out.println("Some error occurred in pair update...");
                        return;
                    }
                    nodeStartId = pairNodeStartData.getFirst();
                    nodeStartTime = pairNodeStartData.getSecond();
                    for(int k=0;k<nodes.get(i+1).size();k++) {
                        nodeEndLabel = nodes.get(i+1).get(k);
                        pairNodeEndData = getNodeData(nodeEndLabel);
                        if(pairNodeEndData==null){
                            System.out.println("Some error occurred pair update...");
                            return;
                        }
                        nodeEndId = pairNodeEndData.getFirst();
                        nodeEndTime = pairNodeEndData.getSecond();

                        if(nodeStartTime==null || nodeEndTime==null){
                            if(graphKBestPath.addEdge(new Edge<>(nodeStartLabel, nodeEndLabel,0))){
                                edgeCount++;
                            }
                        }
                        else{
                            int caseId = checkIfValidCase(distanceBwStation,avgSpeed,waitTimeStationEnd,nodeStartTime,nodeEndTime,maxDelayBwStations);
                            if(caseId>=0){
                                if(!checkIfCrossingWithAnotherTrain(nodeStartId,nodeEndId,nodeStartTime,nodeEndTime,caseId)) {
                                    int edgeCost = getTimeDiff(nodeEndTime, nodeStartTime);
                                    if(edgeCost >= 0 && graphKBestPath.addEdge(new Edge<>(nodeStartLabel, nodeEndLabel,edgeCost))){
                                        edgeCount++;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("Edge size: " +edgeCount);
            List<Path<String>> paths;

            paths= new DefaultKShortestPathFinder<String>().findShortestPaths(getNodeLabel("source",sourceTime), getNodeLabel("dest", destTime), graphKBestPath, noOfPaths);

            writeToFilePaths(paths, pathBestRouteFile, waitTime, avgSpeed);
            milli = new Date().getTime() - milli;
            System.out.println("Duration: " + milli + "ms");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void scheduleNewTrain(String pathRoute, String pathBestRoute, String pathOldTrainSchedule) {
        Double destDist = analyzeOldTrainScheduleFile(pathRoute, pathOldTrainSchedule);
        if(destDist==null){
            System.out.println("Some error occurred in analyzing old data. Please check given input and path.");
            return;
        }
        Double avgSpeed = 80.0;
        int startHrs = 0;
        int startMinutes = 0;
        int endHrs = 23;
        int endMinutes=59;
        ArrayList<Double> waitTime = new ArrayList<>();
        for(int i=0;i<this.PATH.size();i++) {
            waitTime.add(0.0);
        }
//        waitTime.set(5, 2.0);
//        waitTime.set(12, 4.0);
//        waitTime.set(22, 4.0);


        int maxDelayBwStations = 60;
        LocalTime sourceTime = LocalTime.of(12,12);
        LocalTime destTime = LocalTime.of(17,15);
        String pathBestRouteFile;
        int noOfPaths = 10;

        pathBestRouteFile = pathBestRoute + File.separator + "Type 1 AvgSpeed "+avgSpeed + " Start "+startHrs+"_"+startMinutes +" End " + endHrs +"_"+endMinutes;
        scheduleKBestPathOptimized(noOfPaths, sourceTime, destTime, maxDelayBwStations,waitTime,destDist,pathBestRouteFile,avgSpeed,startHrs,startMinutes,endHrs,endMinutes);

        pathBestRouteFile = pathBestRoute + File.separator + "Type 2 AvgSpeed "+avgSpeed + " Start "+startHrs+"_"+startMinutes +" End " + endHrs +"_"+endMinutes;
        scheduleKBestPathOptimized(noOfPaths, sourceTime, null, maxDelayBwStations,waitTime,destDist,pathBestRouteFile,avgSpeed,startHrs,startMinutes,endHrs,endMinutes);

        pathBestRouteFile = pathBestRoute + File.separator + "Type 3 AvgSpeed "+avgSpeed + " Start "+startHrs+"_"+startMinutes +" End " + endHrs +"_"+endMinutes;
        scheduleKBestPathOptimized(noOfPaths, null, destTime, maxDelayBwStations,waitTime,destDist,pathBestRouteFile,avgSpeed,startHrs,startMinutes,endHrs,endMinutes);

        pathBestRouteFile = pathBestRoute + File.separator + "Type 4 AvgSpeed "+avgSpeed + " Start "+startHrs+"_"+startMinutes +" End " + endHrs +"_"+endMinutes;
        scheduleKBestPathOptimized(noOfPaths,null, null, maxDelayBwStations,waitTime,destDist,pathBestRouteFile,avgSpeed,startHrs,startMinutes,endHrs,endMinutes);
    }
}
