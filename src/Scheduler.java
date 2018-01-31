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

    public static int ceilOfDecimal(double number) {
        return (int) Math.ceil(number);
    }

    public static double roundDecimal(double number) {
        return (double) Math.round(number);
    }

    private static final long MEGABYTE = 1024L * 1024L;

    private static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    public boolean addRouteFromFile(String pathRoute){
        stationId = new ArrayList<>();
        stationName = new ArrayList<>();
        stationDistance = new ArrayList<>();
        stationDirectLine = new ArrayList<>();
        stationNoOfUpPlatform = new ArrayList<>();
        stationNoOfDownPlatform = new ArrayList<>();
        stationNoOfDualPlatform = new ArrayList<>();

        try {
            FileReader fReader = new FileReader(pathRoute);
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
                                 double avgSpeed, List<Double> stationDistance){
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
                    int delay = ceilOfDecimal(((nodeDistance - distancePrevStation) / avgSpeed) * 60);
                    timePrevStation.addMinutes(delay);
                    arrivalTimeStation = timePrevStation.getTimeString();
                } else {
                    TrainTime timePrevStationTemp = new TrainTime(bestRouteNode.getTime());
                    timePrevStationTemp.subMinutes(2);
                    arrivalTimeStation = timePrevStationTemp.getTimeString();
                }
                bWriter.write(bestRouteNode.getStationId() + "\t" + arrivalTimeStation + "\t" +
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
                                String pathOldTrainSchedule, boolean newTrainFolder){
        String titlePlot = "Train Schedule";
        int windowHeight = 600;
        int windowWidth = 1000;
        int heightPlotFile = 600;
        int widthPlotFile = 1000;
        LinePlotTrains demo = new LinePlotTrains(titlePlot, windowHeight, windowWidth, newTrainNo, heightPlotFile,
                widthPlotFile, pathPlotFile, pathRoute, pathOldTrainSchedule, pathNewTrainFile, newTrainFolder);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }

    @SuppressWarnings("unused")
    public static void getRuntimeMemory(){
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Start Used memory is bytes: " + memory);
        System.out.println("Start Used memory is megabytes: " + bytesToMegabytes(memory));
    }

    @SuppressWarnings("unused")
    public static void getTrainList(String pathTrainList){
        new TrainList().getTrainList(pathTrainList);
    }

    @SuppressWarnings("unused")
    public static void fetchTrainSchedule(String pathTrainList, String pathTemp, String pathFinal){
        new TrainStoppageList().getTrainStoppageFromFile(pathTrainList,pathTemp,pathFinal);
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
        TrainTime sourceTime = new TrainTime(0,12,12);
        String pathBestRouteFile;
        int noOfPaths = 10;
        List<Path> paths;


        int startDay;
        int startHrs = 0;
        int startMinutes = 0;
        int endDay;
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
        //     writePathsToFile(path,++count,pathBestRouteFile,avgSpeed, scheduler.getStationDistanceList());
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
            writePathsToFile(path,++count, pathBestRouteFile,avgSpeed, scheduler.getStationDistanceList());
        }

    }
}
