import org.jfree.ui.RefineryUtilities;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Scheduler {

    private List<String> stationId;
    private List<String> stationName;
    private List<Double> stationDistance;

    public List<String> getStationIdList(){
        return this.stationId;
    }

    public List<String> getStationNameList(){
        return this.stationName;
    }

    public List<Double> getStationDistanceList(){
        return this.stationDistance;
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

    private static LocalTime addHrs(LocalTime localTime, int hrs) {
        requireNonNull(localTime, "Time is null.");
        hrs += localTime.getHour();
        int minutes = localTime.getMinute();
        hrs = Math.floorMod(hrs, 24);
        return LocalTime.of(hrs, minutes);
    }

    public static LocalTime addMinutes(LocalTime localTime, int minutes) {
        requireNonNull(localTime, "Time is null.");
        minutes+= localTime.getMinute();
        localTime = addHrs(localTime, minutes/60);
        minutes = Math.floorMod(minutes, 60);
        int hrs = localTime.getHour();
        return LocalTime.of(hrs, minutes);
    }

    private static LocalTime subHrs(LocalTime localTime, int hrs) {
        requireNonNull(localTime, "Time is null.");
        hrs = localTime.getHour()-hrs;
        int minutes = localTime.getMinute();
        hrs = Math.floorMod(hrs, 24);
        return LocalTime.of(hrs, minutes);
    }

    public static LocalTime subMinutes(LocalTime localTime, int minutes) {
        requireNonNull(localTime, "Time is null.");
        minutes = localTime.getMinute() - minutes;
        if(minutes>=0){
            return LocalTime.of(localTime.getHour(), minutes);
        }
        int hrsToSub = (-minutes)/60+1;
        localTime = subHrs(localTime, hrsToSub);
        minutes = Math.floorMod(minutes, 60);
        return LocalTime.of(localTime.getHour(), minutes);
    }

    public static Pair<String, LocalTime> getNodeData(String label) {
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

    public static void getRuntimeMemory(){
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Start Used memory is bytes: " + memory);
        System.out.println("Start Used memory is megabytes: " + bytesToMegabytes(memory));
    }

    public boolean addRouteFromFile(String pathRoute){
        stationId = new ArrayList<>();
        stationName = new ArrayList<>();
        stationDistance = new ArrayList<>();
        try {
            FileReader fReader = new FileReader(pathRoute);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            while((line = bReader.readLine()) != null) {
                String data[] = line.split("\\s+");
                String station_code[] = data[0].split("-");
                String id = station_code[station_code.length-1];

                double stationDist = Double.parseDouble(data[1]);
                stationId.add(id);
                stationName.add(data[0]);
                stationDistance.add(stationDist);
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


    public boolean addRoute(List<String> stationIdList, List<String> stationNameList, List<Double> stationDistanceList){
        requireNonNull(stationIdList, "Station id list is null.");
        requireNonNull(stationNameList, "Station name list is null.");
        requireNonNull(stationDistanceList, "Station distance list is null.");
        if(stationIdList.size() != stationNameList.size() || stationNameList.size() != stationDistanceList.size()){
            throw new IllegalArgumentException("Invalid arguments for route");
        }
        this.stationId = stationIdList;
        this.stationName = stationNameList;
        this.stationDistance = stationDistanceList;
        return true;
    }


    public void writePathsToFile(List<Path<String>> paths, String pathBestRouteFile, ArrayList<Double> stopTime, double avgSpeed){
        try {
            BufferedWriter bWriter;
            FileWriter fWriter;
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
                    if (!stationId.get(i-1).equalsIgnoreCase(stationIdResult)) {
                        System.out.println("Invalid path found... ");
                        break;
                    }
                    nodePathBestRoute.add(new Node(timeResult, stationName.get(i-1), stationDistance.get(i-1), stopTime.get(i-1)));
                }
                // System.out.println(path.toString() + " cost: " + path.pathCost());
                String arrivalTimeStation;
                double distancePrevStation = 0.0;
                LocalTime timePrevStation = null;
                fWriter = new FileWriter(pathBestRouteFile + " path " + countPath + " cost " + path.pathCost() + " .path");
                bWriter = new BufferedWriter(fWriter);

                for (Node bestRouteNode : nodePathBestRoute) {
                    if (timePrevStation != null) {

                        int delay = ceilOfDecimal(((bestRouteNode.getDistance() - distancePrevStation) / avgSpeed) * 60);
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

    public static void showPlot(String pathNewTrainFile, int newTrainNo, String pathPlotFile, String pathRoute, String pathOldTrainSchedule){
        String titlePlot = "Train Schedule";
        int windowHeight = 600;
        int windowWidth = 1000;
        int heightPlotFile = 600;
        int widthPlotFile = 1000;
        LinePlotTrains demo = new LinePlotTrains(titlePlot, windowHeight, windowWidth, newTrainNo, heightPlotFile, widthPlotFile, pathPlotFile, pathRoute, pathOldTrainSchedule, pathNewTrainFile);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }


    public static void test(String pathRoute, String pathBestRoute, String pathOldTrainSchedule){
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
        LocalTime sourceTime = LocalTime.of(12,12);
        String pathBestRouteFile;
        int noOfPaths = 10;
        List<Path<String>> paths;

        pathBestRouteFile = pathBestRoute + File.separator + "Type 1 AvgSpeed "+avgSpeed;
        paths= new KBestSchedule().getScheduleNewTrain(scheduler.stationId, scheduler.stationName, scheduler.stationDistance, noOfPaths, sourceTime, minDelayBwTrains, avgSpeed, stopTime, pathOldTrainSchedule);
        System.out.println(paths.size());
        for(Path<String> path: paths) {
            System.out.println(path.toString() + " cost: " + path.pathCost());
        }
        scheduler.writePathsToFile(paths,pathBestRouteFile,stopTime,avgSpeed);


        pathBestRouteFile = pathBestRoute + File.separator + "Type 2 AvgSpeed "+avgSpeed;
        paths= new KBestSchedule().getScheduleNewTrain(scheduler.stationId, scheduler.stationName, scheduler.stationDistance, noOfPaths, null, minDelayBwTrains, avgSpeed, stopTime, pathOldTrainSchedule);
        System.out.println(paths.size());
        for(Path<String> path: paths) {
            System.out.println(path.toString() + " cost: " + path.pathCost());
        }
        scheduler.writePathsToFile(paths,pathBestRouteFile,stopTime,avgSpeed);
    }
}
