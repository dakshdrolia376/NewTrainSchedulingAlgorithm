import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class GraphFile extends GraphParent{
    private final String pathTemp;

    private List<String> latestAccessedQueue;
    private List<Map<String, Map<String, Edge>>> latestAccessedMapList;
    private Map<String, Integer> latestAccessedFileMap;
    private int maxStationsToStore;

    private Map<String, Map<String, Edge>> tempMap = new HashMap<>();
    private String tempFile = "";

    public GraphFile(boolean usePreviousComputation, String pathTemp){
        super();
        requireNonNull(pathTemp, "Path temp cant be null");
        int K = 8;
        this.maxStationsToStore = K;
        this.latestAccessedQueue = new ArrayList<>(K);
        this.latestAccessedMapList = new ArrayList<>(K);
        for(int i=0;i<K;i++){
            latestAccessedMapList.add(new HashMap<>());
        }
        this.latestAccessedFileMap = new HashMap<>(K);
        this.pathTemp = pathTemp + File.separator + "database";
        if(!Scheduler.createFolder(pathTemp)){
            throw new RuntimeException("Unable to initialize local storage");
        }
        else{
            if(!usePreviousComputation) {
                File file = new File(this.pathTemp);
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
        Map<String, Map<String, Edge>> tempMapStation = new HashMap<>();
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<Map<String, Map<String, Edge>>>(){}.getType();
            File file = new File(pathNodeMap);
            if(file.exists()) {
                tempMapStation = gson.fromJson(new FileReader(file), listType);
            }
            this.latestAccessedMapList.set(index,tempMapStation);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private int removeEarliestUsed(){
        System.out.println("**** " +this.latestAccessedQueue.toString());
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
            // for(int i=0;i<this.latestAccessedQueue.size();i++){
            //     if(this.latestAccessedQueue.get(i).equalsIgnoreCase(filename)){
            //         this.latestAccessedQueue.remove(i);
            //         this.latestAccessedQueue.add(filename);
            //         break;
            //     }
            // }
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

    @Override
    public boolean disconnect(){
        return true;
    }

    @Override
    public boolean flushData(){
        String pathNodeMap = this.pathTemp + File.separator + this.tempFile;
        Map<String, Map<String, Edge>> tempMapStation = new HashMap<>();
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<Map<String, Map<String, Edge>>>(){}.getType();
            File file = new File(pathNodeMap);
            if(file.exists()) {
                tempMapStation = gson.fromJson(new FileReader(file), listType);
            }
            for(String key:this.tempMap.keySet()){
                tempMapStation.putIfAbsent(key, new HashMap<>());
                Map<String, Edge> tempNodeMap = tempMapStation.get(key);
                tempNodeMap.putAll(this.tempMap.get(key));
            }
            FileWriter fileWriter = new FileWriter(pathNodeMap);
            gson.toJson(tempMapStation,listType,fileWriter);
            fileWriter.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addNode(Node node){
        requireNonNull(node, "The node is null.");
        return true;
    }

    @Override
    public boolean addMultipleNode(List<Node> nodes){
        requireNonNull(nodes, "The node list is null.");
        return true;
    }

    @Override
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
            if(!flushData()){
                System.out.println("Some error occurred in storing data to disk.");
                return false;
            }
            else{
                tempFile = fileName;
                tempMap = new HashMap<>();
            }
        }

        try {
            this.tempMap.putIfAbsent(edge.getFrom().toString(), new HashMap<>());
            Map<String , Edge> previousData = this.tempMap.get(edge.getFrom().toString());
            // only single edge between two nodes.
            if(previousData.containsKey(edge.getTo().toString())) {
                System.out.println("Duplicate edge " + edge.toString());
                return false;
            }
            previousData.put(edge.getTo().toString(), edge);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addMultipleEdge(List<Edge> edges) {
        requireNonNull(edges, "The edge list is null.");
        boolean ans = true;
        for(Edge edge: edges){
            ans = addEdge(edge);
        }
        return ans;
    }

    @Override
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

    @Override
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

    @Override
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
