import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class GraphKBestPath {
    private final String pathTemp;

    private List<String> latestAccessedQueue;
    private List<Map<String, Map<String, Edge>>> latestAccessedMapList;
    private Map<String, Integer> latestAccessedFileMap;
    private int maxStationsToStore;

    private Map<String, Map<String, Double>> tempMap = new HashMap<>();
    private String tempFile = "";

    public GraphKBestPath(boolean usePreviousComputation, String pathTemp){
        requireNonNull(pathTemp, "Path temp cant be null");
        int K = 2;
        this.maxStationsToStore = K;
        this.latestAccessedQueue = new ArrayList<>(K);
        this.latestAccessedMapList = new ArrayList<>(K);
        for(int i=0;i<K;i++){
            latestAccessedMapList.add(new HashMap<>());
        }
        this.latestAccessedFileMap = new HashMap<>(K);
        this.pathTemp = pathTemp + File.separator + "database";
        File file = new File(this.pathTemp);
        if(!file.exists() && !file.mkdirs()){
            throw new RuntimeException("Unable to initialize local storage");
        }
        else{
            if(!usePreviousComputation) {
                File[] listOfFiles = file.listFiles();
                if (listOfFiles == null) {
                    throw new RuntimeException("Unable to initialize local storage");
                }
                for (File file1 : listOfFiles) {
                    if (file1.isFile() && !file1.delete()) {
                        throw new RuntimeException("Unable to initialize local storage");
                    }
                }
            }
        }
    }

    private boolean loadStation(String fileName, int index){
        Scheduler.getRuntimeMemory();
        System.out.println("Loading station ..." + fileName);
        String pathNodeMap = this.pathTemp + File.separator + fileName;
        JsonObject a = new JsonObject();
        try {
            JsonParser parser = new JsonParser();
            File file = new File(pathNodeMap);
            if(file.exists()){
                a = (JsonObject) parser.parse(new FileReader(file));
            }
            Scheduler.getRuntimeMemory();
            for(String nodeFromId: a.keySet()){
                // Scheduler.getRuntimeMemory();
                JsonObject nodeFrom;
                nodeFrom = a.getAsJsonObject(nodeFromId);
                Map<String, Edge> tempNodeFromMap = new HashMap<>();
                for(Map.Entry<String, JsonElement> entry: nodeFrom.entrySet()){
                    tempNodeFromMap.put(entry.getKey(), new Edge(new Node(nodeFromId),
                            new Node(entry.getKey()), entry.getValue().getAsDouble()));
                }
                this.latestAccessedMapList.get(index).put(nodeFromId, tempNodeFromMap);
            }
            Scheduler.getRuntimeMemory();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private int removeEarliestUsed(){
        if(this.latestAccessedQueue.size()>=this.maxStationsToStore){
            String filename = this.latestAccessedQueue.remove(0);
            System.out.println("Removing station... " + filename );
            int index = this.latestAccessedFileMap.get(filename);
            this.latestAccessedMapList.set(index, new HashMap<>());
            this.latestAccessedFileMap.remove(filename);
            return index;
        }
        else{
            return this.latestAccessedQueue.size();
        }
    }

    private int getIndex(String filename){
        if(this.latestAccessedFileMap.containsKey(filename)){
            for(int i=0;i<this.latestAccessedQueue.size();i++){
                if(this.latestAccessedQueue.get(i).equalsIgnoreCase(filename)){
                    this.latestAccessedQueue.remove(i);
                    this.latestAccessedQueue.add(filename);
                    break;
                }
            }
            return this.latestAccessedFileMap.get(filename);
        }
        else{
            int index = removeEarliestUsed();
            if(!loadStation(filename,index)){
                System.out.println("Some error occurred in loading data");
                return -1;
            }
            System.out.println("After Load Station ");
            Scheduler.getRuntimeMemory();
            this.latestAccessedQueue.add(filename);
            this.latestAccessedFileMap.put(filename, index);
            return index;
        }
    }

    public boolean disconnect(){
        return true;
    }

    @SuppressWarnings("unused")
    public boolean addNode(Node node){
        requireNonNull(node, "The node is null.");
        return true;
    }

    public boolean addMultipleNode(List<Node> nodes){
        requireNonNull(nodes, "The node list is null.");
        return true;
    }

    private boolean flushDataToFile(){
        String pathNodeMap = this.pathTemp + File.separator + this.tempFile;
        JsonObject a = new JsonObject();
        try {
            JsonParser parser = new JsonParser();
            File file = new File(pathNodeMap);
            if(file.exists()){
                a = (JsonObject) parser.parse(new FileReader(file));
            }
            for(String keyNodeFrom: this.tempMap.keySet()){
                JsonObject nodeFrom = new JsonObject();
                if(a.has(keyNodeFrom)) {
                    nodeFrom = a.getAsJsonObject(keyNodeFrom);
                }
                for(Map<String, Double> mapNodeTo: this.tempMap.values()){
                    for(String keyNodeTo: mapNodeTo.keySet()) {
                        nodeFrom.addProperty(keyNodeTo, mapNodeTo.get(keyNodeTo));
                    }
                }
                a.add(keyNodeFrom, nodeFrom);
            }

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathNodeMap));
            bufferedWriter.write(a.toString());
            bufferedWriter.close();
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean addEdge(Edge edge) {
        requireNonNull(edge, "The edge is null.");
        if(!edge.getFrom().isValid() || !edge.getTo().isValid()){
            System.out.println("Invalid edge.");
            return false;
        }
        String fileName = edge.getFrom().toString().split(":")[0];
        if(tempFile.equalsIgnoreCase("")){
            tempMap = new HashMap<>();
            tempFile = fileName;
        }
        if(!tempFile.equalsIgnoreCase(fileName)){
            System.out.println("********Max memory used");
            Scheduler.getRuntimeMemory();
            System.out.println("**********");
            if(!flushDataToFile()){
                System.out.println("Some error occurred");
                return false;
            }
            else{
                tempFile = fileName;
                tempMap = new HashMap<>();
            }
        }

        try {
            Map<String , Double> previousData = this.tempMap.getOrDefault(edge.getFrom().toString(), new HashMap<>());
            previousData.put(edge.getTo().toString(), edge.getWeight());
            this.tempMap.put(edge.getFrom().toString(), previousData);
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean flushEdgeList(){
        return flushDataToFile();
    }

    public boolean addMultipleEdge(List<Edge> edges) {
        requireNonNull(edges, "The edge list is null.");
        boolean ans = true;
        for(Edge edge: edges){
           ans = ans && addEdge(edge);
        }
        return ans;
    }

    public Edge get(Node from, Node to) {
        try {
            String fileName = from.toString().split(":")[0];
            int index = getIndex(fileName);
            return this.latestAccessedMapList.get(index).get(from.toString()).get(to.toString());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public Collection<Edge> get(Node from) {
        String fileName = from.toString().split(":")[0];
        try {
            int index = getIndex(fileName);
            if(this.latestAccessedMapList.get(index)==null){
                System.out.println("Some error occurred in loading station ");
                return Collections.emptyList();
            }
            return this.latestAccessedMapList.get(index).getOrDefault(from.toString(), Collections.emptyMap()).values();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public String toString(){
        try {
            File[] fileList = new File(this.pathTemp).listFiles();
            StringBuilder stringBuilder = new StringBuilder("");
            if(fileList==null){
                System.out.println("Some error occurred");
                return "";
            }
            for (File file: fileList){
                if(!file.isFile()){
                    continue;
                }
                JsonObject a;
                try {
                    JsonParser parser = new JsonParser();
                    a = (JsonObject) parser.parse(new FileReader(file));
                    for(Map.Entry<String , JsonElement> entry: a.entrySet()){
                        stringBuilder.append(entry.getKey());
                        stringBuilder.append(" ->> ");
                        stringBuilder.append(entry.getValue().getAsString());
                        stringBuilder.append('\n');
                    }
                }
                catch (Exception e){
                    System.out.println("Invalid file in database.");
                    e.printStackTrace();
                }
            }
            return stringBuilder.toString();
        }
        catch (Exception e){
            System.out.println("error in toString graph");
            e.printStackTrace();
            return "";
        }
    }
}
