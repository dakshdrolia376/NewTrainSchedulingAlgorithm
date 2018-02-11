import org.jfree.ui.RefineryUtilities;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class Scheduler {

    private List<String> stationId;
    private List<String> stationName;
    private List<Double> stationDistance;
    private List<Boolean> stationDirectLine;
    private List<Integer> stationNoOfUpPlatformList;
    private List<Integer> stationNoOfDownPlatformList;
    private List<Integer> stationNoOfDualPlatformList;
    private List<Integer> stationNoOfUpTrackList;
    private List<Integer> stationNoOfDownTrackList;
    private List<Integer> stationNoOfDualTrackList;

    public List<String> getStationIdList(){
        return this.stationId;
    }

    public List<String> getStationNameList(){
        return this.stationName;
    }

    public List<Double> getStationDistanceList(){
        return this.stationDistance;
    }

    public List<Boolean> getStationDirectLineList(){
        return this.stationDirectLine;
    }

    public List<Integer> getStationNoOfUpPlatformList(){
        return this.stationNoOfUpPlatformList;
    }

    public List<Integer> getStationNoOfDownPlatformList(){
        return this.stationNoOfDownPlatformList;
    }

    public List<Integer> getStationNoOfDualPlatformList(){
        return this.stationNoOfDualPlatformList;
    }

    public List<Integer> getStationNoOfUpTrackList(){
        return this.stationNoOfUpTrackList;
    }

    public List<Integer> getStationNoOfDownTrackList(){
        return this.stationNoOfDownTrackList;
    }

    public List<Integer> getStationNoOfDualTrackList(){
        return this.stationNoOfDualTrackList;
    }

    public static boolean isNetAvailable() {
        try {
            final URL url = new URL("http://www.iitp.ac.in");
            final URLConnection conn = url.openConnection();
            conn.connect();
            return true;
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            return false;
        }
    }

    private static final long MEGABYTE = 1024L * 1024L;

    private static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    @SuppressWarnings("unused")
    public static void updateRouteFile(String pathRouteFile, String pathStationDatabase){
        FetchStationDetails fetchStationDetails = new FetchStationDetails(pathStationDatabase);
        StringBuilder newRouteData = new StringBuilder("");
        Set<String> stationIdSet = new HashSet<>();
        try {
            FileReader fReader = new FileReader(pathRouteFile);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            while((line = bReader.readLine()) != null) {
                String data[] = line.split("\\s+");
                if(data.length<2){
                    System.out.println("Skipping station as incomplete data :" + line);
                    // newRouteData.append(line);
                    // newRouteData.append('\n');
                    continue;
                }
                String id = data[0].trim().replaceAll(".*-", "").toLowerCase();
                if(!stationIdSet.add(id)){
                    System.out.println("Duplicate station found in route : " + id);
                    System.out.println("This can cause problem in scheduling.");
                    continue;
                }
                int numOfPlatform = fetchStationDetails.getNumberOfPlatform(id);
                int numOfUpPlatform = numOfPlatform/2;
                int numOfTrack = fetchStationDetails.getNumberOfTracks(id);
                int numOfUpTrack = numOfTrack/2;
                newRouteData.append(data[0]);
                newRouteData.append(' ');
                newRouteData.append(data[1]);
                newRouteData.append(' ');
                newRouteData.append(1);
                newRouteData.append(' ');
                newRouteData.append(numOfUpPlatform);
                newRouteData.append(' ');
                newRouteData.append(numOfUpPlatform);
                newRouteData.append(' ');
                newRouteData.append((numOfPlatform - (2*numOfUpPlatform)));
                newRouteData.append(' ');
                newRouteData.append(numOfUpTrack);
                newRouteData.append(' ');
                newRouteData.append(numOfUpTrack);
                newRouteData.append(' ');
                newRouteData.append((numOfTrack - (2*numOfUpTrack)));
                newRouteData.append('\n');
            }
            bReader.close();
            fReader.close();
            new WriteToFile().write(pathRouteFile, newRouteData.toString(),false);
        }
        catch (Exception e) {
            System.out.println("Unable to update route file");
            e.printStackTrace();
        }
    }

    public boolean addRouteFromFile(String pathRouteFile){
        stationId = new ArrayList<>();
        stationName = new ArrayList<>();
        stationDistance = new ArrayList<>();
        stationDirectLine = new ArrayList<>();
        stationNoOfUpPlatformList = new ArrayList<>();
        stationNoOfDownPlatformList = new ArrayList<>();
        stationNoOfDualPlatformList = new ArrayList<>();
        stationNoOfUpTrackList = new ArrayList<>();
        stationNoOfDownTrackList = new ArrayList<>();
        stationNoOfDualTrackList = new ArrayList<>();

        try {
            FileReader fReader = new FileReader(pathRouteFile);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            while((line = bReader.readLine()) != null) {
                String data[] = line.split("\\s+");
                if(data.length<9){
                    System.out.println("Invalid station info. : " + line);
                    continue;
                }
                String station_code[] = data[0].split("-");
                String id = station_code[station_code.length-1];
                stationId.add(id);
                stationName.add(data[0]);
                stationDistance.add(Double.parseDouble(data[1]));
                stationDirectLine.add(Integer.parseInt(data[2])==1);
                stationNoOfUpPlatformList.add(Integer.parseInt(data[3]));
                stationNoOfDownPlatformList.add(Integer.parseInt(data[4]));
                stationNoOfDualPlatformList.add(Integer.parseInt(data[5]));
                stationNoOfUpTrackList.add(Integer.parseInt(data[6]));
                stationNoOfDownTrackList.add(Integer.parseInt(data[7]));
                stationNoOfDualTrackList.add(Integer.parseInt(data[8]));
                // if(id.equalsIgnoreCase("pws")){
                //     break;
                // }
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

    public boolean addRoute(List<String> stationIdList, List<String> stationNameList, List<Double> stationDistanceList,
                            List<Boolean> isDirectLineAvailableList,
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
        int sizeStation = stationIdList.size();
        if(stationNameList.size() != sizeStation || stationDistanceList.size() != sizeStation ||
                isDirectLineAvailableList.size() != sizeStation || noOfUpPlatformList.size() != sizeStation ||
                noOfDownPlatformList.size() != sizeStation || noOfDualPlatformList.size() != sizeStation ||
                noOfUpTrackList.size() != sizeStation || noOfDownTrackList.size() != sizeStation ||
                noOfDualTrackList.size() != sizeStation){
            throw new IllegalArgumentException("Invalid arguments for route");
        }
        this.stationId = stationIdList;
        this.stationName = stationNameList;
        this.stationDistance = stationDistanceList;
        this.stationDirectLine = isDirectLineAvailableList;
        this.stationNoOfUpPlatformList= noOfUpPlatformList;
        this.stationNoOfDownPlatformList = noOfDownPlatformList;
        this.stationNoOfDualPlatformList = noOfDualPlatformList;
        this.stationNoOfUpTrackList = noOfUpTrackList;
        this.stationNoOfDownTrackList = noOfDownTrackList;
        this.stationNoOfDualTrackList = noOfDualTrackList;
        return true;
    }


    public static void writePathsToFile(Path path, int countPath, String pathBestRouteFile, List<Integer> stopTime,
                                 double avgSpeed,List<String> stationName, List<Double> stationDistance){
        try {
            // System.out.println("In writePaths" +path.toString());
            BufferedWriter bWriter;
            FileWriter fWriter;
            List<Node> nodePathBestRoute = path.getNodeList();
            String arrivalTimeStation;
            double distancePrevStation = 0.0;
            int delayBwStation;
            double delaySecondsAdded=0;
            double delayBwStationActual;
            double distanceBwStation;

            TrainTime timePrevStation = null;
            fWriter = new FileWriter(pathBestRouteFile + " path " + countPath +
                    " cost " + path.pathCost() + " .path");
            bWriter = new BufferedWriter(fWriter);

            for (int i=1;i<nodePathBestRoute.size()-1;i++) {
                // System.out.println("In writePaths before loop : " +i + " " +path.toString());
                Node bestRouteNode = nodePathBestRoute.get(i);
                double nodeDistance = stationDistance.get(i-1);
                if (timePrevStation != null) {
                    distanceBwStation = nodeDistance - distancePrevStation;
                    delayBwStationActual =((distanceBwStation)/avgSpeed )*60;
                    delayBwStation = (int) Math.ceil(delayBwStationActual - delaySecondsAdded);
                    if(stopTime.get(i-1)==0) {
                        delaySecondsAdded = delayBwStation - (delayBwStationActual - delaySecondsAdded);
                    }
                    else{
                        delaySecondsAdded = 0;
                    }
                    timePrevStation.addMinutes(delayBwStation);
                    arrivalTimeStation = timePrevStation.getTimeString();
                }
                else {
                    TrainTime timePrevStationTemp = new TrainTime(bestRouteNode.getTime());
                    timePrevStationTemp.subMinutes(2);
                    arrivalTimeStation = timePrevStationTemp.getTimeString();
                }
                bWriter.write(stationName.get(i-1) + "\t" + arrivalTimeStation + "\t" +
                        bestRouteNode.getTime().getTimeString() + "\t" + nodeDistance);
                bWriter.write("\n");
                distancePrevStation = nodeDistance;
                timePrevStation = new TrainTime(bestRouteNode.getTime());
                // System.out.println("In writePaths after loop : " +i + " " +path.toString());
            }
            bWriter.close();
            fWriter.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        // System.out.println("After writePaths" +path.toString());
    }

    public static boolean createFolder(String path){
        requireNonNull(path, "path is null");
        File file = new File(path);
        return file.exists() || file.mkdirs();
    }

    public static boolean createParentFolder(String path){
        requireNonNull(path, "path is null");
        File file = new File(path);
        return  createFolder(file.getParentFile().getPath());
    }

    @SuppressWarnings("unused")
    public static void showPlot(String pathNewTrainFile, int newTrainNo, String pathPlotFile, String pathRoute,
                                String pathOldTrainSchedule, boolean newTrainFolder, boolean isSingleDay, int trainDay){
        String titlePlot = "Train Schedule";
        int windowHeight = 600;
        int windowWidth = 1000;
        int heightPlotFile = 600;
        int widthPlotFile = 1000;
        LinePlotTrains demo = new LinePlotTrains(titlePlot, windowHeight, windowWidth, newTrainNo, heightPlotFile,
                widthPlotFile, pathPlotFile, pathRoute, pathOldTrainSchedule, pathNewTrainFile, newTrainFolder,
                isSingleDay, trainDay);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }

    @SuppressWarnings("unused")
    public static void getRuntimeMemory(){
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        // runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Start Used memory is bytes: " + memory);
        System.out.println("Start Used memory is megabytes: " + bytesToMegabytes(memory));
    }

    @SuppressWarnings("unused")
    public static void getTrainList(String pathRoute, String pathUpTrainList, String pathDownTrainList){
        new FetchTrainList().getTrainList(pathRoute, pathUpTrainList, pathDownTrainList);
    }

    @SuppressWarnings("unused")
    public static void fetchStationInfo(String pathStationDatabase){
        if(!new FetchStationDetails(pathStationDatabase).fetchAll()){
            System.out.println("unable to fetch Station Info..");
        }
    }

    @SuppressWarnings("unused")
    public static void fetchTrainInfo(String pathTrainDatabase){
        if(!new FetchTrainDetails(pathTrainDatabase).fetchAll()){
            System.out.println("unable to fetch Train Info..");
        }
    }

    @SuppressWarnings("unused")
    public static void putTrainIntoDatabase(String pathTrainDatabase){
        new FetchTrainDetails(pathTrainDatabase).putAllTrainsInDatabase();
    }

    @SuppressWarnings("unused")
    public static void putStoppagesIntoDatabase(String pathTrainDatabase){
        new FetchTrainDetails(pathTrainDatabase).putAllStoppagesInDatabase();
    }

    @SuppressWarnings("unused")
    public static void createTrainList(String pathRoute, String pathUpTrainList, String pathDownTrainList,
                                       String pathTrainDatabase){
        FileReader fReader;
        BufferedReader bReader;
        String line;
        String[] data;
        String stationId;
        DatabaseConnector databaseConnector = new DatabaseConnector();
        List<String> stationIds = new ArrayList<>();
        Map<String, Integer> stationMap =new HashMap<>();
        int count=0;
        try {
            fReader = new FileReader(pathRoute);
            bReader = new BufferedReader(fReader);
            while((line = bReader.readLine()) != null) {
                data = line.split("\\s+");
                stationId = data[0].trim().replaceAll(".*-", "");
                stationIds.add(stationId);
                stationMap.put(stationId, stationIds.size());
            }
            bReader.close();
            fReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        List<Integer> trainNos = databaseConnector.getTrainNosForStation(stationIds);
        String pathFile;
        FetchTrainDetails fetchTrainDetails = new FetchTrainDetails(pathTrainDatabase);
        List<Integer> upDirectionTrain = new ArrayList<>();
        List<Integer> downDirectionTrain = new ArrayList<>();
        //checking the direction of trains as the list contains both up and down direction;
        int indexStation1;
        int tempIndex;

        for(int trainNo: trainNos){
            indexStation1=-1;
            int trainIndex = fetchTrainDetails.getTrainIndexNo(trainNo);
            if(trainIndex==-1){
                System.out.println("Some error occurred in getting train " + trainNo);
                continue;
            }
            pathFile = pathTrainDatabase + File.separator + trainIndex + ".txt";
            int direction = 0;
            try {
                fReader = new FileReader(pathFile);
                bReader = new BufferedReader(fReader);
                while((line = bReader.readLine()) != null) {
                    data = line.split("\\s+");
                    stationId = data[0].trim().replaceAll(".*-", "");
                    tempIndex = stationMap.getOrDefault(stationId, -1);
                    if(tempIndex==-1){
                        continue;
                    }
                    if(indexStation1==-1){
                        indexStation1 = tempIndex;
                        continue;
                    }

                    if(direction==0) {
                        if (indexStation1 < tempIndex) {
                            direction = 1;
                        }
                        else if (indexStation1 > tempIndex){
                            direction = 2;
                        }
                        else{
                            break;
                        }
                    }
                    else if(direction==1){
                        if(indexStation1 >=tempIndex){
                            direction =-1;
                            break;
                        }
                        else{
                            indexStation1 = tempIndex;
                        }
                    }
                    else if(direction==2){
                        if(indexStation1 <=tempIndex){
                            direction =-2;
                            break;
                        }
                        else{
                            indexStation1 = tempIndex;
                        }
                    }
                    else{
                        System.out.println("Some error occurred.");
                    }
                }
                if(direction==0){
                    System.out.println(trainNo+" stops only at one station in route. Skipping it.");
                    // upDirectionTrain.add(trainNo);
                }
                else if (direction==1) {
                    upDirectionTrain.add(trainNo);
                }
                else if (direction==2){
                    downDirectionTrain.add(trainNo);
                }
                else{
                    System.out.println("Rejecting train " + trainNo + " Direction : " + direction);
                }
                bReader.close();
                fReader.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<List<Integer>> upTrains = new ArrayList<>();
        List<List<Integer>> downTrains = new ArrayList<>();

        for(int i=0;i<7;i++){
            upTrains.add(databaseConnector.getTrainNosForDay(i));
            downTrains.add(new ArrayList<>(upTrains.get(i)));
            upTrains.get(i).retainAll(upDirectionTrain);
            downTrains.get(i).retainAll(downDirectionTrain);
        }

        StringBuilder stringBuilder = new StringBuilder("");
        for(int i=0;i<7;i++){
            stringBuilder.append(i);
            for(int trainNo: upTrains.get(i)){
                stringBuilder.append('\t');
                stringBuilder.append(trainNo);
            }
            stringBuilder.append('\n');
        }
        new WriteToFile().write(pathUpTrainList, stringBuilder.toString(), false);
        stringBuilder = new StringBuilder("");
        for(int i=0;i<7;i++){
            stringBuilder.append(i);
            for(int trainNo: downTrains.get(i)){
                stringBuilder.append('\t');
                stringBuilder.append(trainNo);
            }
            stringBuilder.append('\n');
        }
        new WriteToFile().write(pathDownTrainList, stringBuilder.toString(), false);
    }

    @SuppressWarnings("unused")
    public static void putStationIntoDatabase(String pathStationDatabase){
        new FetchStationDetails(pathStationDatabase).putAllStationsInDatabase();
    }

    @SuppressWarnings("unused")
    public static void fetchTrainSchedule(String pathTrainList, String pathTemp, String pathTrainBase, String pathTrainDatabase){
        new FetchTrainDetails(pathTrainDatabase).getTrainStoppageFromFile(pathTrainList,pathTemp,pathTrainBase);
    }

    @SuppressWarnings("unused")
    public static void test(String pathTemp, String pathRoute, String pathBestRoute, String pathOldUpTrainSchedule,
                            String pathOldDownTrainSchedule, boolean isSingleDay, int trainDay,
                            boolean usePreviousComputation, double ratio){
        Scheduler scheduler = new Scheduler();
        if(!scheduler.addRouteFromFile(pathRoute)){
            System.out.println("Unable to load route file");
            return;
        }
        ArrayList<Integer> stopTime = new ArrayList<>();
        for(int i=0;i<scheduler.stationId.size();i++) {
            stopTime.add(0);
        }
        // stopTime.set(5, 2.0);
        // stopTime.set(12, 4.0);
        // stopTime.set(22, 4.0);

        Double avgSpeed = 80.0;
        int minDelayBwTrains = 3;
        String pathBestRouteFile;
        int noOfPaths = 20;
        List<Path> paths;
        TrainTime sourceTime;

        int startDay;
        int endDay;
        int startHrs = 0;
        int startMinutes = 0;
        int endHrs = 23;
        int endMinutes=59;
        int maxDelayBwStations = 60;
        if(isSingleDay){
            startDay = trainDay;
            endDay = trainDay;
            sourceTime  = new TrainTime(trainDay,12,12);
        }
        else{
            startDay = 0;
            endDay = 6;
            sourceTime = new TrainTime(3,12,12);
        }
        int count;

        // pathBestRouteFile = pathBestRoute + File.separator + "Type 1 AvgSpeed "+avgSpeed;
        // paths= new KBestSchedule().getScheduleNewTrain(pathTemp, scheduler.getStationIdList(), scheduler.getStationNameList(),
        //         scheduler.getStationDistanceList(), scheduler.getStationDirectLineList(),
        //         scheduler.getStationNoOfUpPlatformList(), scheduler.getStationNoOfDownPlatformList(),
        //         scheduler.getStationNoOfDualPlatformList(), scheduler.getStationNoOfUpTrackList(),
        //         scheduler.getStationNoOfDownTrackList(), scheduler.getStationNoOfDualTrackList(), noOfPaths, sourceTime,
        //         minDelayBwTrains, avgSpeed, stopTime, pathOldUpTrainSchedule,pathOldDownTrainSchedule, trainDay,
        //         startDay,startHrs, startMinutes, endDay, endHrs, endMinutes, maxDelayBwStations, isSingleDay,
        //         usePreviousComputation, ratio);
        // System.out.println(paths.size());
        // count=0;
        // for(Path path: paths) {
        //     System.out.println(path.toString() + " cost: " + path.pathCost());
        //     writePathsToFile(path,++count,pathBestRouteFile,stopTime,avgSpeed, scheduler.getStationNameList(),
        //             scheduler.getStationDistanceList());
        // }

        pathBestRouteFile = pathBestRoute + File.separator + "Type 2 AvgSpeed "+avgSpeed;
        paths= new KBestSchedule().getScheduleNewTrain(pathTemp, scheduler.getStationIdList(), scheduler.getStationNameList(),
                scheduler.getStationDistanceList(), scheduler.getStationDirectLineList(),
                scheduler.getStationNoOfUpPlatformList(), scheduler.getStationNoOfDownPlatformList(),
                scheduler.getStationNoOfDualPlatformList(), scheduler.getStationNoOfUpTrackList(),
                scheduler.getStationNoOfDownTrackList(), scheduler.getStationNoOfDualTrackList(), noOfPaths, null,
                minDelayBwTrains, avgSpeed, stopTime, pathOldUpTrainSchedule, pathOldDownTrainSchedule, trainDay,
                startDay,startHrs, startMinutes, endDay,endHrs, endMinutes,maxDelayBwStations, isSingleDay,
                usePreviousComputation, ratio);
        System.out.println(paths.size());
        System.out.println("In test :" +paths.toString());
        count=0;
        for(Path path: paths) {
            System.out.println(path.toString() + " cost: " + path.pathCost());
            writePathsToFile(path,++count, pathBestRouteFile,stopTime, avgSpeed, scheduler.getStationNameList(),
                    scheduler.getStationDistanceList());
        }
    }
}
