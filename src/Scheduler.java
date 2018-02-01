import org.jfree.ui.RefineryUtilities;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Scheduler {

    private List<String> stationId;
    private List<String> stationName;
    private List<Double> stationDistance;
    private List<Boolean> stationDirectLine;
    private List<Integer> stationNoOfUpPlatform;
    private List<Integer> stationNoOfDownPlatform;
    private List<Integer> stationNoOfDualPlatform;

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

    public List<Integer> getStationNoOfUpPlatformLineList(){
        return this.stationNoOfUpPlatform;
    }

    public List<Integer> getStationNoOfDownPlatformLineList(){
        return this.stationNoOfDownPlatform;
    }

    public List<Integer> getStationNoOfDualPlatformLineList(){
        return this.stationNoOfDualPlatform;
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
        try {
            FileReader fReader = new FileReader(pathRouteFile);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            while((line = bReader.readLine()) != null) {
                String data[] = line.split("\\s+");
                if(data.length==6){
                    newRouteData.append(line);
                    newRouteData.append('\n');
                    continue;
                }
                String station_code[] = data[0].split("-");
                String id = station_code[station_code.length-1];
                int numOfPlatform = fetchStationDetails.fetchStationId(id);
                int numOfUpPlatform;
                int numOfDownPlatform;
                if(numOfPlatform==-1){
                    System.out.println("Unable to find the number of platforms for the station " + id +
                            ". Using default values.");
                    numOfUpPlatform = 1;
                    numOfDownPlatform =1;
                    numOfPlatform =2;
                }
                else if(numOfPlatform<2){
                    numOfUpPlatform = 0;
                    numOfDownPlatform =0;
                }
                else if(numOfPlatform<4){
                    numOfUpPlatform = 1;
                    numOfDownPlatform =1;
                }
                else{
                    numOfUpPlatform = 2;
                    numOfDownPlatform =2;
                }
                newRouteData.append(line);
                newRouteData.append(' ');
                newRouteData.append(1);
                newRouteData.append(' ');
                newRouteData.append(numOfUpPlatform);
                newRouteData.append(' ');
                newRouteData.append(numOfDownPlatform);
                newRouteData.append(' ');
                newRouteData.append((numOfPlatform - numOfUpPlatform -numOfDownPlatform));
                newRouteData.append('\n');
            }
            fReader.close();
            bReader.close();
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
        stationNoOfUpPlatform = new ArrayList<>();
        stationNoOfDownPlatform = new ArrayList<>();
        stationNoOfDualPlatform = new ArrayList<>();

        try {
            FileReader fReader = new FileReader(pathRouteFile);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            while((line = bReader.readLine()) != null) {
                String data[] = line.split("\\s+");
                String station_code[] = data[0].split("-");
                String id = station_code[station_code.length-1];
                stationId.add(id);
                stationName.add(data[0]);
                stationDistance.add(Double.parseDouble(data[1]));
                stationDirectLine.add(Integer.parseInt(data[2])==1);
                stationNoOfUpPlatform.add(Integer.parseInt(data[3]));
                stationNoOfDownPlatform.add(Integer.parseInt(data[4]));
                stationNoOfDualPlatform.add(Integer.parseInt(data[5]));
                // if(id.equalsIgnoreCase("pws")){
                //     break;
                // }
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

    public boolean addRoute(List<String> stationIdList, List<String> stationNameList, List<Double> stationDistanceList,
                            List<Boolean> isDirectLineAvailableList,
                            List<Integer> noOfUpPlatformList, List<Integer> noOfDownPlatformList,
                            List<Integer> noOfDualPlatformList){
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
                noOfDownPlatformList.size() != sizeStation || noOfDualPlatformList.size() != sizeStation){
            throw new IllegalArgumentException("Invalid arguments for route");
        }
        this.stationId = stationIdList;
        this.stationName = stationNameList;
        this.stationDistance = stationDistanceList;
        this.stationDirectLine = isDirectLineAvailableList;
        this.stationNoOfUpPlatform= noOfUpPlatformList;
        this.stationNoOfDownPlatform = noOfDownPlatformList;
        this.stationNoOfDualPlatform = noOfDualPlatformList;
        return true;
    }


    public static void writePathsToFile(Path path, int countPath, String pathBestRouteFile,
                                 double avgSpeed,List<String> stationName, List<Double> stationDistance){
        try {
            BufferedWriter bWriter;
            FileWriter fWriter;
            List<Node> nodePathBestRoute = path.getNodeList();
            String arrivalTimeStation;
            double distancePrevStation = 0.0;
            TrainTime timePrevStation = null;
            fWriter = new FileWriter(pathBestRouteFile + " path " + countPath +
                    " cost " + path.pathCost() + " .path");
            bWriter = new BufferedWriter(fWriter);

            for (int i=1;i<nodePathBestRoute.size()-1;i++) {
                Node bestRouteNode = nodePathBestRoute.get(i);
                double nodeDistance = stationDistance.get(i-1);
                if (timePrevStation != null) {
                    int delay = (int)Math.ceil(((nodeDistance - distancePrevStation) / avgSpeed) * 60);
                    timePrevStation.addMinutes(delay);
                    arrivalTimeStation = timePrevStation.getTimeString();
                } else {
                    TrainTime timePrevStationTemp = new TrainTime(bestRouteNode.getTime());
                    timePrevStationTemp.subMinutes(2);
                    arrivalTimeStation = timePrevStationTemp.getTimeString();
                }
                bWriter.write(stationName.get(i-1) + "\t" + arrivalTimeStation + "\t" +
                        bestRouteNode.getTime().getTimeString() + "\t" + nodeDistance);
                bWriter.write("\n");
                distancePrevStation = nodeDistance;
                timePrevStation = bestRouteNode.getTime();
            }
            bWriter.close();
            fWriter.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
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
    public static void getTrainList(String pathTrainList){
        new FetchTrainList().getTrainList(pathTrainList);

    }

    @SuppressWarnings("unused")
    public static void fetchStationInfo(String pathTemp){
        if(!new FetchStationDetails(pathTemp).fetchAll()){
            System.out.println("unable to fetch Station Info..");
        }
    }

    @SuppressWarnings("unused")
    public static void fetchTrainInfo(String pathTemp){
        if(!new FetchTrainDetails(pathTemp).fetchAll()){
            System.out.println("unable to fetch Train Info..");
        }
    }

    @SuppressWarnings("unused")
    public static void fetchTrainSchedule(String pathTrainList, String pathTemp, String pathFinal, String pathTrainDatabase){
        new FetchTrainDetails(pathTrainDatabase).getTrainStoppageFromFile(pathTrainList,pathTemp,pathFinal);
    }

    @SuppressWarnings("unused")
    public static void test(String pathTemp, String pathRoute, String pathBestRoute, String pathOldTrainSchedule,
                            boolean isSingleDay, int trainDay, boolean usePreviousComputation){
        Scheduler scheduler = new Scheduler();
        if(!scheduler.addRouteFromFile(pathRoute)){
            System.out.println("Unable to load route file");
            return;
        }
        ArrayList<Double> stopTime = new ArrayList<>();
        for(int i=0;i<scheduler.stationId.size();i++) {
            stopTime.add(0.0);
        }
        // stopTime.set(5, 2.0);
        // stopTime.set(12, 4.0);
        // stopTime.set(22, 4.0);

        Double avgSpeed = 80.0;
        int minDelayBwTrains = 3;
        TrainTime sourceTime = new TrainTime(0,17,12);
        String pathBestRouteFile;
        int noOfPaths = 20;
        List<Path> paths;


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
        }
        else{
            startDay = 0;
            endDay = 6;
        }
        int count;

        // pathBestRouteFile = pathBestRoute + File.separator + "Type 1 AvgSpeed "+avgSpeed;
        // paths= new KBestSchedule().getScheduleNewTrain(pathTemp, scheduler.getStationIdList(), scheduler.getStationNameList(),
        //         scheduler.getStationDistanceList(), scheduler.getStationDirectLineList(),
        //         scheduler.getStationNoOfUpPlatformLineList(), scheduler.getStationNoOfDownPlatformLineList(),
        //         scheduler.getStationNoOfDualPlatformLineList(), noOfPaths, sourceTime,
        //         minDelayBwTrains, avgSpeed, stopTime, pathOldTrainSchedule, trainDay,startDay,startHrs, startMinutes,
        //         endDay, endHrs, endMinutes, maxDelayBwStations, isSingleDay, usePreviousComputation);
        // System.out.println(paths.size());
        // count=0;
        // for(Path path: paths) {
        //     System.out.println(path.toString() + " cost: " + path.pathCost());
        //     writePathsToFile(path,++count,pathBestRouteFile,avgSpeed, scheduler.getStationNameList(),
        //             scheduler.getStationDistanceList());
        // }

        pathBestRouteFile = pathBestRoute + File.separator + "Type 2 AvgSpeed "+avgSpeed;
        paths= new KBestSchedule().getScheduleNewTrain(pathTemp, scheduler.getStationIdList(), scheduler.getStationNameList(),
                scheduler.getStationDistanceList(), scheduler.getStationDirectLineList(),
                scheduler.getStationNoOfUpPlatformLineList(), scheduler.getStationNoOfDownPlatformLineList(),
                scheduler.getStationNoOfDualPlatformLineList(), noOfPaths, null,
                minDelayBwTrains, avgSpeed, stopTime, pathOldTrainSchedule, trainDay,startDay,startHrs, startMinutes,
                endDay,endHrs, endMinutes,maxDelayBwStations, isSingleDay, usePreviousComputation);
        System.out.println(paths.size());
        count=0;
        for(Path path: paths) {
            System.out.println(path.toString() + " cost: " + path.pathCost());
            writePathsToFile(path,++count, pathBestRouteFile,avgSpeed, scheduler.getStationNameList(),
                    scheduler.getStationDistanceList());
        }
    }
}
