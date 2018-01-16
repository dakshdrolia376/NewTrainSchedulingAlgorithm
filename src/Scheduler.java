import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

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
                // if(stationId.equals("ara")){
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
    
    private void scheduleNewTrainUtility(LocalTime startTime, LocalTime destTime, int maxDelayBwStations, ArrayList<Double> waitTime, Double destDist, String pathBestRouteFile, Double avgSpeed, int startHrs, int startMinutes, int endHrs, int endMinutes){
        
        long milli = new Date().getTime();
        System.out.println("*********************************************************");
        System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        System.out.println("Avg speed: " + avgSpeed);
        System.out.println("Start: "+startHrs+":"+startMinutes);
        System.out.println("End: "+ endHrs+":"+startMinutes);
        
        try{
            ArrayList<ArrayList<Node>> nodes = this.route.findSchedule(waitTime,startHrs,startMinutes,endHrs,endMinutes);
            ArrayList<String> stationList = this.route.getStationList();
            stationList.add(0,"SOURCE");
            
            ArrayList<Node> nodeSrcList = new ArrayList<>();
            nodeSrcList.add(new Node(startTime, "SOURCE", 0.0, 0.0));
            nodes.add(0,nodeSrcList);

            stationList.add("DEST");
            ArrayList<Node> nodeDestList = new ArrayList<>();
            nodeDestList.add(new Node(destTime, "DEST", destDist, 0.0));
            nodes.add(nodeDestList);
            
            Graph graph = new Graph();
            long edgeCount=0;

            for(ArrayList<Node> nodeStation: nodes) {
                for(Node node: nodeStation) {
                    graph.addNode(node);
                }
            }
            System.out.println("Nodes size: " +nodes.size());
            if(nodes.size() != stationList.size()) {
                System.out.println("Invalid nodes in graph... exiting");
                return;
            }

            Node nodeStart;
            Node nodeEnd;

            for(int i=0;i<stationList.size()-1;i++) {
                // System.out.println("Nodes station " + (i+1) + "size: " + nodes.get(i).size());
                // System.out.println("Nodes station " + (i+2) + "size: " + nodes.get(i+1).size());
                if(nodes.get(i).isEmpty()) {
                    System.out.println("No path found as no available slot for station "+ stationList.get(i));
                    return;
                }
                if(nodes.get(i+1).isEmpty()) {
                    System.out.println("No path found as no available slot for station "+ stationList.get(i+1));
                    return;
                }
                if(!nodes.get(i).get(0).getStationId().equals(stationList.get(i)) || !nodes.get(i+1).get(0).getStationId().equals(stationList.get(i+1))) {
                    System.out.println("Invalid path Info.");
                    return;
                }

                for(int j=0;j<nodes.get(i).size();j++) {
                    nodeStart = nodes.get(i).get(j);
                    for(int k=0;k<nodes.get(i+1).size();k++) {
                        nodeEnd = nodes.get(i+1).get(k);
                        
                        if(nodeStart.getTime()==null || nodeEnd.getTime()==null){
                            if(graph.addEdge(nodeStart, nodeEnd,0)){
                                edgeCount++;
                                // System.err.println("Added " + nodeStart.getStationId() + " -> " + nodeEnd.getStationId());
                            }
                            // else{
                            //     System.out.println("Unable to add edge in graph " + nodeStart.getStationId() + ":"+ nodeStart.getTime() + " -> " + nodeEnd.getStationId() + ":"+ nodeEnd.getTime());
                            // }
                        }
                        else{
                            Double distancePrevStation = nodeEnd.getDistance() - nodeStart.getDistance();
                            int delay = (int)(nextDecimal(((distancePrevStation/avgSpeed )*60))/1);
                            delay = (delay + (int)(nodeEnd.getWaitTime()/1));
                            
                            LocalTime earliestTimeToReach = addMinutes(nodeStart.getTime(), delay);
                            LocalTime maxTimeToReach = addMinutes(earliestTimeToReach, maxDelayBwStations);

                            int compEarliestAndNode = earliestTimeToReach.compareTo(nodeEnd.getTime());
                            int compNodeAndMax = nodeEnd.getTime().compareTo(maxTimeToReach);
                            int caseId=0;
                            if(maxTimeToReach.compareTo(earliestTimeToReach)<=0) {
                                if(earliestTimeToReach.compareTo(nodeEnd.getTime())<=0 && nodeEnd.getTime().compareTo(maxTimeToReach)>=0) {
                                    compEarliestAndNode = -1;
                                    compNodeAndMax = -1;
                                    caseId = 1;
                                }
                                else if(nodeEnd.getTime().compareTo(maxTimeToReach)<=0 && nodeEnd.getTime().compareTo(earliestTimeToReach)<=0) {
                                    compEarliestAndNode = -1;
                                    compNodeAndMax = -1;
                                    caseId = 2;
                                }
                            }

                            if(compEarliestAndNode <=0 && compNodeAndMax <=0){
                                boolean crossAnotherTrain = false;
                                if(!nodeStart.getStationId().equalsIgnoreCase("SOURCE") && !nodeEnd.getStationId().equalsIgnoreCase("DEST") ) {
                                    for (Train train : TRAIN_LIST) {
                                        LocalTime oldTrainDept = train.getDept(nodeStart.getStationId());
                                        LocalTime oldTrainArr = train.getArr(nodeEnd.getStationId());
                                        int compOldTrainDeptAndNodeStart = oldTrainDept.compareTo(nodeStart.getTime());
                                        int compOldTrainArrAndNodeEnd = oldTrainArr.compareTo(nodeEnd.getTime());

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
                                            // System.out.println(train.getTrainNo() + " " + compOldTrainDeptAndNodeStart + " " + compOldTrainArrAndNodeEnd);
                                            crossAnotherTrain = true;
                                            break;
                                        }
                                    }
                                }

                                if(!crossAnotherTrain) {
                                    int startHrs1 = nodeStart.getTime().getHour();
                                    int startMinutes1 = nodeStart.getTime().getMinute();
                                    int endHrs1 = nodeEnd.getTime().getHour();
                                    int endMinutes1 = nodeEnd.getTime().getMinute();
                                    int edgeCost;
                                    if(endHrs1>startHrs1 || (endHrs1==startHrs1 && endMinutes1>startMinutes1)) {
                                        edgeCost = (endHrs1-startHrs1)*60 + (endMinutes1- startMinutes1);
                                    }
                                    else {
                                        edgeCost = (24 + endHrs1 -startHrs1 )* 60 + endMinutes1-startMinutes1;
                                    }
                                    
                                    if(edgeCost >= 0 && graph.addEdge(nodeStart, nodeEnd,edgeCost)){
                                        edgeCount++;
                                        // System.err.println("Added " + nodeStart.getStationId() +" " +nodeStart.getTime() + " -> " + nodeEnd.getStationId() +" " +nodeEnd.getTime());
                                    }
                                    // else{
                                    //     System.err.println("Unable to add edge in graph " + nodeStart.getStationId() + ":"+ nodeStart.getTime() + " -> " + nodeEnd.getStationId() + ":"+ nodeEnd.getTime());
                                    // }
                                }
                                // else {
                                //     System.err.println("Rejected to avoid collision : " + nodeStart.getStationId() +" " + nodeEnd.getTime() + " -> " + nodeEnd.getStationId() +" " + nodeEnd.getTime());
                                // }
                            }
                            // else if(compEarliestAndNode>0){
                            //     System.err.println("Rejected as too early to reach " + nodeStart.getStationId() +" " +nodeStart.getTime() + " -> " + nodeEnd.getStationId() +" " +nodeEnd.getTime());
                            // }
                            // else {
                            //     System.err.println("Rejected as too late to reach " + nodeStart.getStationId() +" " +nodeStart.getTime() + " -> " + nodeEnd.getStationId() +" " +nodeEnd.getTime());
                            // }
                        }
                    }
                }
            }

            BufferedWriter bWriter;
            FileWriter fWriter;
            fWriter = new FileWriter(pathBestRouteFile);
            bWriter = new BufferedWriter(fWriter);
            
            System.out.println("Edge size: " +edgeCount);
            // graph.printGraph();
            // route.printInfo();
            ArrayList<Node> bestRoute = graph.dijkstra();

            //removing SOURCE & DEST node
            if(bestRoute.size()>=2) {
                System.out.println("Best path found : ");
                bestRoute.remove(0);
                bestRoute.remove(bestRoute.size()-1);
                String arrivalTimeStation;
                Double distancePrevStation = 0.0;
                LocalTime timePrevStation= null;
                
                for(Node bestRouteNode: bestRoute) {
                    if(timePrevStation!=null) {
                        int delay = (int)(nextDecimal((bestRouteNode.getDistance() - distancePrevStation)/avgSpeed * 60)/1);
                        arrivalTimeStation = addMinutes(timePrevStation, delay) +"";
                    }
                    else {
                        arrivalTimeStation = subMinutes(bestRouteNode.getTime(),2) + "";
                    }
                    bWriter.write(bestRouteNode.getStationId() + "\t" + arrivalTimeStation + "\t" + bestRouteNode.getTime() + "\t" + bestRouteNode.getDistance());
                    bWriter.write("\n");
                    distancePrevStation = bestRouteNode.getDistance();
                    timePrevStation = bestRouteNode.getTime();
                }
            }
            else {
                System.out.println("No path found");
            }
            milli = new Date().getTime() - milli;
            System.out.println("Duration: " + milli + "ms");
            bWriter.close();
            fWriter.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

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

        int maxDelayBwStations = 120;
        LocalTime startTime = LocalTime.of(12,12);
        LocalTime destTime = LocalTime.of(18,15);
        String pathBestRouteFile;

        pathBestRouteFile = pathBestRoute + File.separator + "Type 1 AvgSpeed "+avgSpeed + " Start "+startHrs+"_"+startMinutes +" End " + endHrs +"_"+endMinutes+".txt";
        scheduleNewTrainUtility(startTime, destTime, maxDelayBwStations,waitTime,destDist,pathBestRouteFile,avgSpeed,startHrs,startMinutes,endHrs,endMinutes);

        pathBestRouteFile = pathBestRoute + File.separator + "Type 2 AvgSpeed "+avgSpeed + " Start "+startHrs+"_"+startMinutes +" End " + endHrs +"_"+endMinutes+".txt";
        scheduleNewTrainUtility(startTime, null, maxDelayBwStations,waitTime,destDist,pathBestRouteFile,avgSpeed,startHrs,startMinutes,endHrs,endMinutes);

        pathBestRouteFile = pathBestRoute + File.separator + "Type 3 AvgSpeed "+avgSpeed + " Start "+startHrs+"_"+startMinutes +" End " + endHrs +"_"+endMinutes+".txt";
        scheduleNewTrainUtility(null, destTime, maxDelayBwStations,waitTime,destDist,pathBestRouteFile,avgSpeed,startHrs,startMinutes,endHrs,endMinutes);

        pathBestRouteFile = pathBestRoute + File.separator + "Type 4 AvgSpeed "+avgSpeed + " Start "+startHrs+"_"+startMinutes +" End " + endHrs +"_"+endMinutes+".txt";
        scheduleNewTrainUtility(null, null, maxDelayBwStations,waitTime,destDist,pathBestRouteFile,avgSpeed,startHrs,startMinutes,endHrs,endMinutes);
    }
}
