import org.jfree.ui.RefineryUtilities;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Scheduler {

    private List<String> stationId;
    private List<String> stationName;
    private List<Double> stationDistance;

    private static boolean isNetAvailable() {
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

    public static int ceilOfDecimal(double number) {
        return (int) Math.ceil(number);
    }

    private static final long MEGABYTE = 1024L * 1024L;

    private static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
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

    private boolean addRouteFromFile(String pathRoute){
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


    private boolean addRoute(List<String> stationIdList, List<String> stationNameList, List<Double> stationDistanceList){
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


    private void writeToFilePaths(List<Path<String>> paths, String pathBestRouteFile, ArrayList<Double> stopTime, double avgSpeed){
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

    private static boolean createFolder(String path){
        File file = new File(path);
        if(!file.exists() && !file.mkdirs()){
                System.out.println("Unable to create folder " + path);
                return false;
        }
        return true;
    }

    private static boolean createParentFolder(String path){
        File file = new File(path);
        return  createFolder(file.getParentFile().getPath());
    }


    public static void main(String[] args) {
        String pathTrainList = "data" + File.separator +"train_list.txt";
        String pathRoute = "data"+File.separator+"route"+File.separator+"route.txt";
        String pathPlotFile = "data"+File.separator+"plot"+File.separator+"plot1.pdf";

        String pathTemp = "data"+File.separator+"temp";
        String pathLog = "data"+File.separator+"logs";
        String pathFinal = "data"+File.separator+"final";
        String pathBestRoute = "data"+File.separator+"bestRoute";
        String pathOldTrainSchedule = "data"+File.separator+"final" + File.separator + "dayall";

        if(!createParentFolder(pathTrainList) && !createParentFolder(pathRoute) && !createParentFolder(pathPlotFile)
                && createFolder(pathTemp) && createFolder(pathLog) && createFolder(pathFinal)
                && createFolder(pathBestRoute) && createFolder(pathOldTrainSchedule)){
            System.exit(1);
        }

        if(!isNetAvailable()){
            System.out.println("No internet Connection.. Try again");
            System.exit(0);
        }

        try {
            // Creating a File object that represents the disk file.
            PrintStream o = new PrintStream(new File(pathLog + File.separator+"err.log"));
            PrintStream o1 = new PrintStream(new File(pathLog + File.separator+"output.log"));
           // Store current System.out before assigning a new value
           // PrintStream console = System.err;
           // PrintStream console1 = System.out;
           //
           // Assign o to output stream
            System.setErr(o);
            System.setOut(o1);
            Scheduler scheduler = new Scheduler();


           // new TrainList().getTrainList(pathTrainList);
           // new TrainStoppageList().getTrainStoppageFromFile(pathTrainList,pathTemp,pathFinal);

            if(!scheduler.addRouteFromFile(pathRoute)){
                System.out.println("Unable to load route file");
                System.exit(0);
            }

            ArrayList<String> stationIdComplete= new ArrayList<>(scheduler.stationId);
            ArrayList<String> stationNameComplete= new ArrayList<>(scheduler.stationName);
            ArrayList<Double> stationDistanceComplete= new ArrayList<>(scheduler.stationDistance);
            ArrayList<Double> stopTime = new ArrayList<>();
            for(int i=0;i<scheduler.stationId.size();i++) {
                stopTime.add(0.0);
            }
            // stopTime.set(5, 2.0);
            // stopTime.set(12, 4.0);
            // stopTime.set(22, 4.0);

            Double avgSpeed = 80.0;
            int minDelayBwTrains = 3;
            LocalTime sourceTime = null;
            LocalTime destTime = LocalTime.of(17,15);
            String pathBestRouteFile;
            int noOfPaths = 1;
            int hrs1=-1;
            int minutes1=-1;

            for(int i=0;i<stationIdComplete.size();i+=9){
                pathBestRouteFile = pathBestRoute + File.separator + "Type 4 AvgSpeed "+avgSpeed +" part " + i/10;
                Scheduler scheduler1 = new Scheduler();
                int last = ((i+10)>stationIdComplete.size())?stationIdComplete.size():(i+10);
                scheduler1.addRoute(new ArrayList<>(stationIdComplete.subList(i,last)),new ArrayList<>(stationNameComplete.subList(i,last)),new ArrayList<>(stationDistanceComplete.subList(i,last)));
                long milli = new Date().getTime();
                System.out.println("*********************************************************");
                System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
                System.out.println("Avg speed: " + avgSpeed);
                if(hrs1>=0 && minutes1>=0) {
                    sourceTime = LocalTime.of(hrs1,minutes1);
                }

                List<Path<String>> paths= new KBestSchedule().getScheduleNewTrain(scheduler1.stationId, scheduler1.stationName, scheduler1.stationDistance, noOfPaths, sourceTime, null, minDelayBwTrains, avgSpeed, new ArrayList<>(stopTime.subList(i,last)), pathOldTrainSchedule);
                milli = new Date().getTime() - milli;
                System.out.println("Duration: " + milli + "ms");
                System.out.println(paths.size());
                for(Path<String> path: paths) {
                    System.out.println(path.toString() + " cost: " + path.pathCost());
                }
                String dataLastNode[] = paths.get(0).getNodeList().get(paths.get(0).getNodeList().size()-2).split(":");
                hrs1 = Integer.parseInt(dataLastNode[1]);
                minutes1 = Integer.parseInt(dataLastNode[2]);
                scheduler1.writeToFilePaths(paths,pathBestRouteFile,stopTime,avgSpeed);
            }

            // pathBestRouteFile = pathBestRoute + File.separator + "Type 1 AvgSpeed "+avgSpeed;
            // new Scheduler().getScheduleNewTrain(noOfPaths, pathBestRouteFile, sourceTime, destTime, minDelayBwTrains, avgSpeed, stopTime, pathRoute, pathBestRoute, pathOldTrainSchedule);
            // pathBestRouteFile = pathBestRoute + File.separator + "Type 2 AvgSpeed "+avgSpeed;
            // new Scheduler().getScheduleNewTrain(noOfPaths, pathBestRouteFile, sourceTime, null, minDelayBwTrains, avgSpeed, stopTime, pathRoute, pathBestRoute, pathOldTrainSchedule);
            // pathBestRouteFile = pathBestRoute + File.separator + "Type 3 AvgSpeed "+avgSpeed;
            // destTime = LocalTime.of(16,20);
            // new Scheduler().getScheduleNewTrain(noOfPaths, pathBestRouteFile, null, destTime, minDelayBwTrains, avgSpeed, stopTime, pathRoute, pathBestRoute, pathOldTrainSchedule);

            // String titlePlot = "Train Schedule";
            // int windowHeight = 600;
            // int windowWidth = 1000;
            // int newTrainNo = 9910;
            // int heightPlotFile = 600;
            // int widthPlotFile = 1000;
            //
            // String pathNewTrainFile = pathBestRoute+File.separator+"Type 4 AvgSpeed 80.0 path 1 cost 182.0 .path";
            // LinePlotTrains demo = new LinePlotTrains(titlePlot, windowHeight, windowWidth, newTrainNo, heightPlotFile, widthPlotFile, pathPlotFile, pathRoute, pathOldTrainSchedule, pathNewTrainFile);
            // demo.pack();
            // RefineryUtilities.centerFrameOnScreen(demo);
            // demo.setVisible(true);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
