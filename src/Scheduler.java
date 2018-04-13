import org.jfree.ui.RefineryUtilities;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
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

    @SuppressWarnings("SpellCheckingInspection")
    public static Map<String, String> uniqueStationIdMap = Map.ofEntries(Map.entry("-k", "unique-k"),
            Map.entry("-a", "unique-a"),
            Map.entry("-t", "unique-t"),Map.entry("-b", "unique-b"),Map.entry("-x", "unique-x"),
            Map.entry("haldibari-chilahati-zero-point-0ph-c", "unique-phc"),
            Map.entry("radhikapur-birol-zero-point-0pr-b", "unique-prb"),
            Map.entry("0-point-of-mssn-latu-line-0pm-l", "unique-pml"),
            Map.entry("petrapole-benapole-zero-point-0pp-b", "unique-ppb"),
            Map.entry("gede-darshana-zero-point-0pg-d", "unique-pgd"),
            Map.entry("singabad-rohanpur-zero-point-0ps-r", "unique-psr"),
            Map.entry("-am","unique-am"),Map.entry("-er","unique-er"),Map.entry("-bq","unique-bq"),
            Map.entry("-cr","unique-cr"),Map.entry("-yd","unique-yd"),Map.entry("-cy","unique-cy"),
            Map.entry("-ka","unique-ka"),Map.entry("-ne","unique-ne"),
            Map.entry("lalmonirhat-junction-br-lmh","br-lmh"),
            Map.entry("rohri-junction-pr-roh","pr-roh"),
            Map.entry("mominpur-b-mmpr","b-mmpr"));

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

    public static String getStationIdFromName(String name){
        name = name.toLowerCase();
        if(uniqueStationIdMap.containsKey(name)){
            return uniqueStationIdMap.get(name);
        }
        if(name.contains("-sl-")){
            return name.trim().replaceAll(".*-(?=-sl-.)", "");
        }
        else if(name.contains("-xx-")){
            return name.trim().replaceAll(".*-(?=-xx-.)", "");
        }
        else if(name.contains("-yy-")){
            return name.trim().replaceAll(".*-(?=-yy-.)", "");
        }
        else if(name.endsWith("-dls")){
            return name.trim().replaceAll(".*-(?=.+?-dls)", "");
        }
        else if(name.endsWith("-els")){
            return name.trim().replaceAll(".*-(?=.+?-els)", "");
        }
        else if(name.endsWith("-")){
            return name.trim().replaceAll(".*-(?=.+?-.)", "");
        }
        return name.trim().replaceAll(".*-", "");
    }

    public static String getTrainNoFromName(String name){
        name = name.toLowerCase();
        if(name.endsWith("-slip")){
            name = name.substring(0,name.length()-5);
            return "9"+name.trim().replaceAll(".*-(?=.)", "");
        }
        return name.trim().replaceAll(".*-(?=.)", "");
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

    @SuppressWarnings("unused,SpellCheckingInspection")
    public void updateTrainTypeFile(String pathTrainTypeFile){
        List<List<String>> trainNames = new DatabaseConnector().getAllTrainNames();
        Map<String,List<String>> mapTrainTypes = new HashMap<>();
        for(List<String> trainType: trainNames){
            mapTrainTypes.putIfAbsent(trainType.get(2).replaceAll("\\s+","-"),new ArrayList<>());
            mapTrainTypes.get(trainType.get(2).replaceAll("\\s+","-")).add(trainType.get(0));
        }

        StringBuilder stringBuilder = new StringBuilder("");

        for(String type:mapTrainTypes.keySet()){
            stringBuilder.append(type);
            List<String> traiNos = mapTrainTypes.get(type);
            for(String trainNo: traiNos){
                stringBuilder.append('\t');
                stringBuilder.append(trainNo);
            }
            stringBuilder.append('\n');
        }
        new WriteToFile().write(pathTrainTypeFile,stringBuilder.toString(),false);
    }

    public static List<Double> loadNewTrainTimeData(String pathRouteTimeFile, String newTrainType){
        List<Double> timeNewTrain = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(pathRouteTimeFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            int count=-1;
            String data[] = line.split("\\s+");
            for(int i=0;i<data.length;i++){
                if(data[i].equalsIgnoreCase(newTrainType)){
                    count = i;
                    break;
                }
            }
            while(count>0 && (line=bufferedReader.readLine())!=null){
                String avgTime = line.split("\\s+")[count];
                double avgTimeDouble = 0;
                try{
                    if(!avgTime.equalsIgnoreCase("NA")) {
                        avgTimeDouble = Double.parseDouble(avgTime);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                timeNewTrain.add(avgTimeDouble);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return timeNewTrain;
    }

    @SuppressWarnings("unused")
    public void updateRouteFile(String pathTrainTypeFile, String pathRouteFile, String pathRouteTimeMinFile,
                                String pathRouteTimeAvgFile, String pathStationDatabase){
        FetchStationDetails fetchStationDetails = new FetchStationDetails(pathStationDatabase);
        int lengthMaxName = 18;
        StringBuilder newRouteData = new StringBuilder("");
        StringBuilder routeTimeAvgData = new StringBuilder("");
        StringBuilder routeTimeMinData = new StringBuilder("");
        DatabaseConnector databaseConnector = new DatabaseConnector();
        List<List<String>> trainTypesAndNumbers = new ArrayList<>();
        try {
            FileReader fReader = new FileReader(pathTrainTypeFile);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            while((line = bReader.readLine()) != null) {
                String[] tempTrainNoArray = line.split("\\s+");
                if(tempTrainNoArray.length==0){
                    continue;
                }
                List<String> tempTrainNo = new ArrayList<>(Arrays.asList(tempTrainNoArray));
                trainTypesAndNumbers.add(tempTrainNo);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        double distanceToSubtract = -1;
        double prevDistance;
        double currDistance = 0;
        try {
            FileReader fReader = new FileReader(pathRouteFile);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            Map<String, String> departure1;
            Map<String, String> arrival2;
            Map<String, String> departure2=null;

            while((line = bReader.readLine()) != null) {
                String data[] = line.split("\\s+");
                if(data.length<2){
                    System.out.println("Skipping station as incomplete data :" + line);
                    continue;
                }
                String id = Scheduler.getStationIdFromName(data[0]);
                if(distanceToSubtract==-1){
                    distanceToSubtract = Double.parseDouble(data[1]);
                    List<Map<String, String>> stationStoppages = databaseConnector.getScheduleForStation(id);
                    departure2 = stationStoppages.get(1);
                    routeTimeAvgData.append("Station");
                    routeTimeMinData.append("Station");
                    for(int lengthString = "Station".length();lengthString<(lengthMaxName+20);lengthString++){
                        routeTimeAvgData.append(' ');
                        routeTimeMinData.append(' ');
                    }
                    for(List<String> trainNos: trainTypesAndNumbers) {
                        routeTimeAvgData.append('\t');
                        routeTimeMinData.append('\t');
                        routeTimeAvgData.append(trainNos.get(0));
                        routeTimeMinData.append(trainNos.get(0));
                        for(int lengthString = trainNos.get(0).length();lengthString<lengthMaxName;lengthString++){
                            routeTimeAvgData.append(' ');
                            routeTimeMinData.append(' ');
                        }
                    }
                    routeTimeAvgData.append('\n');
                    routeTimeMinData.append('\n');
                    routeTimeAvgData.append(data[0]);
                    routeTimeMinData.append(data[0]);
                    for(int lengthString = data[0].length();lengthString<(lengthMaxName+20);lengthString++){
                        routeTimeAvgData.append(' ');
                        routeTimeMinData.append(' ');
                    }
                    for(List<String> trainNos: trainTypesAndNumbers) {
                        routeTimeAvgData.append('\t');
                        routeTimeMinData.append('\t');
                        routeTimeAvgData.append("NA");
                        routeTimeMinData.append("NA");
                        for(int lengthString = "NA".length();lengthString<lengthMaxName;lengthString++){
                            routeTimeAvgData.append(' ');
                            routeTimeMinData.append(' ');
                        }
                    }
                    routeTimeAvgData.append('\n');
                    routeTimeMinData.append('\n');
                    currDistance = distanceToSubtract;
                }
                else{
                    prevDistance = currDistance;
                    currDistance = Double.parseDouble(data[1]);
                    departure1 = departure2;
                    List<Map<String, String>> stationStoppages = databaseConnector.getScheduleForStation(id);
                    arrival2 = stationStoppages.get(0);
                    departure2 = stationStoppages.get(1);
                    List<String> trainNosOriginal;
                    routeTimeAvgData.append(data[0]);
                    routeTimeMinData.append(data[0]);
                    for(int lengthString = data[0].length();lengthString<(lengthMaxName+20);lengthString++){
                        routeTimeAvgData.append(' ');
                        routeTimeMinData.append(' ');
                    }
                    for(List<String> trainNos: trainTypesAndNumbers) {
                        trainNosOriginal = new ArrayList<>(arrival2.keySet());
                        trainNosOriginal.retainAll(trainNos);
                        double timeTrainSum = 0;
                        double timeTrainCount = 0;
                        double timeTrainMin = Double.MAX_VALUE;
                        double tempTimeTrain;
                        for(String trainNo:trainNosOriginal){
                            String dept = departure1.getOrDefault(trainNo,null);
                            String arr = arrival2.getOrDefault(trainNo,null);
                            if(dept==null||arr==null){
                                continue;
                            }
                            int deptTime = new TrainTime("0:"+dept).getValue();
                            int arrTime = new TrainTime("0:"+arr).getValue();
                            if(arrTime<deptTime){
                                arrTime+=1440;
                            }
                            if(currDistance!=prevDistance){
                                tempTimeTrain = arrTime - deptTime;
                                if(tempTimeTrain>600){
                                    continue;
                                }
                                timeTrainSum +=tempTimeTrain;
                                if(tempTimeTrain<timeTrainMin){
                                    timeTrainMin = tempTimeTrain;
                                }
                                timeTrainCount++;
                            }
                        }
                        if(timeTrainCount>0) {
                            timeTrainSum = timeTrainSum / timeTrainCount;
                            routeTimeAvgData.append('\t');
                            routeTimeMinData.append('\t');
                            routeTimeAvgData.append(new DecimalFormat("#0.00").format(timeTrainSum));
                            routeTimeMinData.append(new DecimalFormat("#0.00").format(timeTrainMin));
                            for(int lengthString = new DecimalFormat("#0.00").format(timeTrainSum).length();lengthString<lengthMaxName;lengthString++){
                                routeTimeAvgData.append(' ');
                                routeTimeMinData.append(' ');
                            }
                        }
                        else{
                            routeTimeAvgData.append('\t');
                            routeTimeMinData.append('\t');
                            routeTimeAvgData.append("NA");
                            routeTimeMinData.append("NA");
                            for(int lengthString = "NA".length();lengthString<lengthMaxName;lengthString++){
                                routeTimeAvgData.append(' ');
                                routeTimeMinData.append(' ');
                            }
                        }
                    }
                    routeTimeAvgData.append('\n');
                    routeTimeMinData.append('\n');
                }
                int numOfPlatform = fetchStationDetails.getNumberOfPlatform(id);
                if(numOfPlatform<=0){
                    System.out.println("Unable to find Num of platforms in station : " + id+". Skipping it.");
                    continue;
                }
                int numOfUpPlatform = numOfPlatform/2;
                int numOfTrack = fetchStationDetails.getNumberOfTracks(id);
                int numOfUpTrack = numOfTrack/2;
                newRouteData.append(data[0]);
                newRouteData.append(' ');
                newRouteData.append(new DecimalFormat("#0.00").format((Double.parseDouble(data[1])) - distanceToSubtract));
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
            new WriteToFile().write(pathRouteTimeAvgFile, routeTimeAvgData.toString(),false);
            new WriteToFile().write(pathRouteTimeMinFile, routeTimeMinData.toString(),false);
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
                String id = Scheduler.getStationIdFromName(data[0]);
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

    @SuppressWarnings("unused")
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


    public void writePathsToFile(Path path, int countPath, String pathBestRouteFile, List<Integer> stopTime,
                                 String pathRouteTimeFile, String newTrainType,List<String> stationName, List<Double> stationDistance){
        try {
            List<Double> avgTimeNewTrain = loadNewTrainTimeData(pathRouteTimeFile,newTrainType);
            avgTimeNewTrain.add(0,0.0);
            avgTimeNewTrain.add(0.0);
            BufferedWriter bWriter;
            FileWriter fWriter;
            List<Node> nodePathBestRoute = path.getNodeList();
            String arrivalTimeStation;
            int delayBwStation;
            double delaySecondsAdded=0;
            double delayBwStationActual;

            TrainTime timePrevStation = null;
            fWriter = new FileWriter(pathBestRouteFile + " path " + countPath +
                    " cost " + (path.pathCost()-stopTime.get(0)-stopTime.get(stopTime.size()-1)) + " .txt");
            bWriter = new BufferedWriter(fWriter);

            for (int i=1;i<nodePathBestRoute.size()-1;i++) {
                Node bestRouteNode = nodePathBestRoute.get(i);
                double nodeDistance = stationDistance.get(i-1);
                if (timePrevStation != null) {
                    delayBwStationActual =avgTimeNewTrain.get(i);
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
                    timePrevStationTemp.subMinutes(stopTime.get(i-1));
                    arrivalTimeStation = timePrevStationTemp.getTimeString();
                }
                bWriter.write(stationName.get(i-1) + "\t" + arrivalTimeStation + "\t" +
                        bestRouteNode.getTime().getTimeString() + "\t" + nodeDistance);
                bWriter.write("\n");
                timePrevStation = new TrainTime(bestRouteNode.getTime());
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
    public void showPlot(String pathNewTrainFile, int newTrainNo, String pathPlotFile, String pathRoute,
                                String pathOldTrainSchedule, int trainDay, int requiredDay){
        String titlePlot = "Train Schedule";
        int windowHeight = 600;
        int windowWidth = 1000;
        int heightPlotFile = 600;
        int widthPlotFile = 1000;
        LinePlotTrains demo = new LinePlotTrains(titlePlot, windowHeight, windowWidth, newTrainNo, heightPlotFile,
                widthPlotFile, pathPlotFile, pathRoute, pathOldTrainSchedule, pathNewTrainFile, trainDay,requiredDay);
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
        // System.out.println("Start Used memory is bytes: " + memory);
        System.out.println("Start Used memory is megabytes: " + bytesToMegabytes(memory));
    }

    @SuppressWarnings("unused")
    public static void clearRuntimeMemory(){
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
    }

    @SuppressWarnings("unused")
    public void getTrainList(String pathRoute, String pathUpTrainList, String pathDownTrainList){
        new FetchTrainList().getTrainList(pathRoute, pathUpTrainList, pathDownTrainList);
    }

    @SuppressWarnings("unused")
    public void fetchStationInfo(String pathStationDatabase){
        if(!new FetchStationDetails(pathStationDatabase).fetchAll()){
            System.out.println("unable to fetch Station Info..");
        }
    }

    @SuppressWarnings("unused")
    public void fetchTrainInfo(String pathTrainDatabase){
        if(!new FetchTrainDetails(pathTrainDatabase).fetchAll()){
            System.out.println("unable to fetch Train Info..");
        }
    }

    @SuppressWarnings("unused")
    public void putTrainIntoDatabase(String pathTrainDatabase){
        new FetchTrainDetails(pathTrainDatabase).putAllTrainsInMap();
        new FetchTrainDetails(pathTrainDatabase).putTrainsMapInDatabase();
    }

    @SuppressWarnings("unused")
    public void putStationIntoDatabase(String pathStationDatabase){
        new FetchStationDetails(pathStationDatabase).putAllStationsInMap();
        new FetchStationDetails(pathStationDatabase).putStationMapInDatabase();
    }

    @SuppressWarnings("unused")
    public void fetchTrainSchedule(String pathTrainList, String pathTemp, String pathTrainBase, String pathTrainDatabase){
        deleteFolderContent(pathTrainBase);
        new FetchTrainDetails(pathTrainDatabase).getTrainStoppageFromFile(pathTrainList,pathTemp,pathTrainBase);
    }

    @SuppressWarnings("unused")
    public void putStoppagesIntoDatabase(String pathTrainDatabase){
        new FetchTrainDetails(pathTrainDatabase).putAllStoppagesInDatabase();
    }

    @SuppressWarnings("unused")
    public void createTrainList(String pathRoute, String pathTrainList){
        FileReader fReader;
        BufferedReader bReader;
        String line;
        String stationId;
        DatabaseConnector databaseConnector = new DatabaseConnector();
        List<String> stationIds = new ArrayList<>();
        try {
            fReader = new FileReader(pathRoute);
            bReader = new BufferedReader(fReader);
            while((line = bReader.readLine()) != null) {
                stationId =Scheduler.getStationIdFromName(line.split("\\s+")[0]);
                stationIds.add(stationId);
            }
            bReader.close();
            fReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        List<Integer> trainNos = databaseConnector.getTrainNosForStation(stationIds);
        List<List<Integer>> Trains = new ArrayList<>();
        for(int i=0;i<7;i++){
            Trains.add(databaseConnector.getTrainNosForDay(i));
            Trains.get(i).retainAll(trainNos);
        }

        StringBuilder stringBuilder = new StringBuilder("");
        for(int i=0;i<7;i++){
            stringBuilder.append(i);
            for(int trainNo: Trains.get(i)){
                stringBuilder.append('\t');
                stringBuilder.append(trainNo);
            }
            stringBuilder.append('\n');
        }
        new WriteToFile().write(pathTrainList, stringBuilder.toString(), false);
    }

    public static void deleteFolderContent(String folderPath){
        File file = new File(folderPath);
        if(!file.exists()){
            return;
        }

        for (File childFile : file.listFiles()) {

            if (childFile.isDirectory()) {
                deleteFolderContent(childFile.getPath());
            }
            else {
                if (!childFile.delete()) {
                    throw new RuntimeException("Unable to delete file: "+ childFile.getPath());
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public void initializeStopTimeFile(String pathRouteStopTime, String pathRouteFile){
        int maxLength = 40;
        StringBuilder stringBuilder = new StringBuilder("");
        try {
            FileReader fileReader = new FileReader(pathRouteFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while((line=bufferedReader.readLine())!=null){
                try{
                    String st = line.split("\\s+")[0];
                    stringBuilder.append(st);
                    for(int i=st.length();i<maxLength;i++){
                        stringBuilder.append(' ');
                    }
                    stringBuilder.append('\t');
                    stringBuilder.append('0');
                    stringBuilder.append('\n');
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        new WriteToFile().write(pathRouteStopTime,stringBuilder.toString(),false);
    }

    public static ArrayList<Integer> getStopTime(String pathRouteStopTime){
        ArrayList<Integer> stopTime = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(pathRouteStopTime);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while((line=bufferedReader.readLine())!=null){
                try{
                    stopTime.add(Integer.parseInt(line.split("\\s+")[1]));
                }
                catch (Exception e){
                    e.printStackTrace();
                    stopTime = new ArrayList<>();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return stopTime;
    }

    @SuppressWarnings("unused")
    public void test(String pathTemp, String pathRoute, String pathBestRoute, String pathOldTrainSchedule, boolean isSingleDay,
                     int trainDay, boolean usePreviousComputation, double ratio, String pathRouteTimeFile, String newTrainType,
                     String pathLog, TrainTime sourceTime, String pathRouteStopTime, int trainNotToLoad){
        if(sourceTime!=null){
            sourceTime = new TrainTime(sourceTime);
        }
        Scheduler scheduler = new Scheduler();
        if(!scheduler.addRouteFromFile(pathRoute)){
            System.out.println("Unable to load route file");
            return;
        }
        ArrayList<Integer> stopTime = getStopTime(pathRouteStopTime);

        int minDelayBwTrains = 3;
        int noOfPaths = 10;
        List<Path> paths;

        int count;
        try {
            PrintStream o1 = new PrintStream(new File(pathLog + File.separator + "Output Type Full Day "+trainDay+" TrainType "+newTrainType +
                    " maxRatio "+ratio +((sourceTime==null)?" unconditional.log":" conditional.log")));
            PrintStream console = System.out;
            System.setOut(o1);
            String pathBestRouteFile = pathBestRoute + File.separator +"Type Full Day "+trainDay+" TrainType "+newTrainType +
                    " maxRatio "+ratio +((sourceTime==null)?" unconditional ":" conditional ");
            paths= new KBestSchedule().getScheduleNewTrain(pathTemp, scheduler.getStationIdList(), scheduler.getStationNameList(),
                    scheduler.getStationDistanceList(), scheduler.getStationDirectLineList(),
                    scheduler.getStationNoOfUpPlatformList(), scheduler.getStationNoOfDownPlatformList(),
                    scheduler.getStationNoOfDualPlatformList(), scheduler.getStationNoOfUpTrackList(),
                    scheduler.getStationNoOfDownTrackList(), scheduler.getStationNoOfDualTrackList(), noOfPaths, sourceTime,
                    minDelayBwTrains, pathRouteTimeFile,newTrainType, stopTime, pathOldTrainSchedule,
                    trainDay, isSingleDay,
                    usePreviousComputation, ratio, (sourceTime!=null), trainNotToLoad);
            count=0;
            System.out.print(paths.size());
            for(Path path: paths) {
                System.out.print("\t"+(path.pathCost()-stopTime.get(0)-stopTime.get(stopTime.size()-1)));
            }
            System.out.println();

            for(Path path: paths) {
                System.out.println( "Cost: " + (path.pathCost()-stopTime.get(0)-stopTime.get(stopTime.size()-1))+" Unscheduled Stop: "+path.getUnScheduledStop()+" "+path.toString());
                writePathsToFile(path,++count,pathBestRouteFile,stopTime,pathRouteTimeFile,newTrainType, scheduler.getStationNameList(),
                        scheduler.getStationDistanceList());
            }
            System.setOut(console);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
