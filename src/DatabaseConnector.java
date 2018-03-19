import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class DatabaseConnector {
    private Connection con=null;
    public DatabaseConnector(){
        if(!getConnection()){
            throw new RuntimeException("Unable to connect to database.");
        }
    }

    private boolean getConnection(){
        try {
            if(con==null) {
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:7888/railwaydetails?autoReconnect=true&useSSL=false",
                        "root", "bittu420");
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            con=null;
            return false;
        }
    }

    public boolean closeConnection(){
        try {
            con.close();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAllNodes(){
        try {
            if(!getConnection()){
                return false;
            }
            // the mysql insert statement
            System.out.println("Initializing database");
            con.setAutoCommit(false);
            Statement stmt = con.createStatement();
            String query = "truncate edges;";
            stmt.executeUpdate(query);
            query = "delete from edges;";
            stmt.executeUpdate(query);
            query = "delete from nodes;";
            stmt.executeUpdate(query);
            query ="ALTER TABLE nodes AUTO_INCREMENT = 1;";
            stmt.executeUpdate(query);
            con.commit();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertIntoNode(Node nodeId){
        try {
            if(!getConnection()){
                return false;
            }
            // the mysql insert statement
            con.setAutoCommit(false);
            String query = "insert into nodes (Name) values (?);";
            // create the mysql insert prepared statement
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setString (1, nodeId.toString());
            // execute the prepared statement
            preparedStmt.executeUpdate();
            con.commit();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertIntoNodeBatch(List<Node> nodeIds){

        try {
            if (!getConnection()) {
                return false;
            }
            // the mysql insert statement
            String query = "insert into nodes (Name) values (?);";
            // create the mysql insert prepared statement
            con.setAutoCommit(false);
            PreparedStatement preparedStmt = con.prepareStatement(query);
            final int batchSize = 1000;
            int count = 0;

            for (Node nodeId : nodeIds) {
                preparedStmt.setString(1, nodeId.toString());
                preparedStmt.addBatch();
                if (++count % batchSize == 0) {
                    preparedStmt.executeBatch();
                    count=0;
                }
            }
            preparedStmt.executeBatch();
            con.commit();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertIntoTrainBatch(Map<Integer, List<String>> trainDetails){
        try {
            if (!getConnection()) {
                return false;
            }
            String query = "insert into train (Name, Num, trainIndex, travelSun, travelMon, travelTue, travelWed, " +
                    "travelThu, travelFri, travelSat) values (?,?,?,?,?,?,?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE count=count+1, " +
                    "DuplicateIndexes = CONCAT(DuplicateIndexes,\",\",?);";
            // create the mysql insert prepared statement
            con.setAutoCommit(false);
            PreparedStatement preparedStmt = con.prepareStatement(query);
            final int batchSize = 1000;
            int count = 0;

            for(int trainIndex:trainDetails.keySet()){
                List<String> trainDetail = trainDetails.get(trainIndex);
                preparedStmt.setString(1,trainDetail.get(1));
                preparedStmt.setInt(2,Integer.parseInt(trainDetail.get(0)));
                preparedStmt.setString(3,(trainIndex+""));
                preparedStmt.setString(4,trainDetail.get(2).charAt(0)=='1'?"Y":"N");
                preparedStmt.setString(5,trainDetail.get(2).charAt(1)=='1'?"Y":"N");
                preparedStmt.setString(6,trainDetail.get(2).charAt(2)=='1'?"Y":"N");
                preparedStmt.setString(7,trainDetail.get(2).charAt(3)=='1'?"Y":"N");
                preparedStmt.setString(8,trainDetail.get(2).charAt(4)=='1'?"Y":"N");
                preparedStmt.setString(9,trainDetail.get(2).charAt(5)=='1'?"Y":"N");
                preparedStmt.setString(10,trainDetail.get(2).charAt(6)=='1'?"Y":"N");
                preparedStmt.setString(11,(trainIndex+""));
                preparedStmt.addBatch();
                if (++count % batchSize == 0) {
                    preparedStmt.executeBatch();
                    count=0;
                }
            }
            preparedStmt.executeBatch();
            con.commit();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertIntoStationBatch(Map<Integer, List<String>> stationDetails){
        try {
            if (!getConnection()) {
                return false;
            }
            String query = "insert into station (Name, ID, StationIndex, StationType, Track,  OriginatingTrains, " +
                    "TerminatingTrains, HaltingTrains, Platforms, Elevation, RailwayZone, Address) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE count=count+1, " +
                    "DuplicateIndexes = CONCAT(DuplicateIndexes,\",\",?);";

            // create the mysql insert prepared statement
            con.setAutoCommit(false);
            PreparedStatement preparedStmt = con.prepareStatement(query);
            final int batchSize = 1000;
            int count = 0;

            for(int stationIndex: stationDetails.keySet()){
                List<String> stationDetail = stationDetails.get(stationIndex);
                preparedStmt.setString(1,stationDetail.get(0));
                preparedStmt.setString(2,stationDetail.get(1));
                preparedStmt.setString(3,(stationIndex+""));
                preparedStmt.setString(4,stationDetail.get(2));
                preparedStmt.setString(5,stationDetail.get(3));
                preparedStmt.setInt(6,Integer.parseInt(stationDetail.get(4)));
                preparedStmt.setInt(7,Integer.parseInt(stationDetail.get(5)));
                preparedStmt.setInt(8,Integer.parseInt(stationDetail.get(6)));
                preparedStmt.setInt(9,Integer.parseInt(stationDetail.get(7)));
                preparedStmt.setString(10,stationDetail.get(8));
                preparedStmt.setString(11,stationDetail.get(9));
                preparedStmt.setString(12,stationDetail.get(10));
                preparedStmt.setString(13,(stationIndex+""));
                preparedStmt.addBatch();
                if (++count % batchSize == 0) {
                    preparedStmt.executeBatch();
                    count=0;
                }
            }
            preparedStmt.executeBatch();
            con.commit();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertIntoStoppageBatch(List<Integer> trainNos, List<String> stationIds, List<TrainTime> arrivals,
                                           List<TrainTime> departures, List<Double> distances){
        try {
            if (!getConnection()) {
                return false;
            }
            String query = "insert into stoppage (stationId, trainNum, arrival, departure, distance) values (?,?,?,?,?)"+
                    " ON DUPLICATE KEY UPDATE count=count+1, " +
                    "duplicateStoppages = CONCAT(duplicateStoppages,\",\",?);";

            // create the mysql insert prepared statement
            con.setAutoCommit(false);
            PreparedStatement preparedStmt = con.prepareStatement(query);
            final int batchSize = 1000;
            int count = 0;

            for(int i=0;i<stationIds.size();i++){
                preparedStmt.setString(1,stationIds.get(i));
                preparedStmt.setInt(2,trainNos.get(i));
                preparedStmt.setString(3,arrivals.get(i).getTimeString());
                preparedStmt.setString(4,departures.get(i).getTimeString());
                preparedStmt.setDouble(5,distances.get(i));
                preparedStmt.setString(6,arrivals.get(i).getTimeString() + ">" +
                        departures.get(i).getTimeString() + ">" + distances.get(i).toString());
                preparedStmt.addBatch();
                if (++count % batchSize == 0) {
                    preparedStmt.executeBatch();
                    count=0;
                }
            }
            preparedStmt.executeBatch();
            con.commit();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unused")
    public boolean insertIntoEdge(Edge edge){
        try {
            if(!getConnection()){
                return false;
            }
            if(edge.getWeight()<0){
                System.out.println("Edge weight cant be negative");
                return false;
            }
            con.setAutoCommit(false);
            // the mysql insert statement
            String query = "insert into edges (FromNode, Weight, ToNode) values ((select ID from nodes where Name = ?)," +
                    "?,(select ID from nodes where Name = ?));";
            // create the mysql insert prepared statement
            PreparedStatement preparedStmt = con.prepareStatement(query);

            preparedStmt.setString(1, edge.getFrom().toString());
            preparedStmt.setDouble(2,edge.getWeight());
            preparedStmt.setString(3,edge.getTo().toString());

            // execute the prepared statement
            preparedStmt.executeUpdate();
            con.commit();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertIntoEdgeBatch(List<Edge> edges){
        try {
            if(!getConnection()){
                return false;
            }
            // the mysql insert statement
            String query = "insert into edges (FromNode, Weight, ToNode) values ((select ID from nodes where Name = ?)" +
                    ",?,(select ID from nodes where Name = ?));";
            // create the mysql insert prepared statement
            con.setAutoCommit(false);
            PreparedStatement preparedStmt = con.prepareStatement(query);
            final int batchSize = 1000;
            int count = 0;

            for(Edge edge:edges) {
                if (edge.getWeight() < 0) {
                    // System.out.println("Edge weight cant be negative");
                    continue;
                }
                preparedStmt.setString(1, edge.getFrom().toString());
                preparedStmt.setDouble(2, edge.getWeight());
                preparedStmt.setString(3, edge.getTo().toString());
                preparedStmt.addBatch();
                if (++count % batchSize == 0) {
                    preparedStmt.executeBatch();
                    count=0;
                }
            }
            preparedStmt.executeBatch();
            con.commit();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public List<Node> getNodes(){
        try {
            if(!getConnection()){
                return Collections.emptyList();
            }
            // the mysql insert statement
            String query = "select * from nodes;";
            // create the mysql insert prepared statement
            PreparedStatement preparedStmt = con.prepareStatement(query);
            // execute the prepared statement
            ResultSet rs=preparedStmt.executeQuery();
            List<Node> edgeInfo = new ArrayList<>();
            while (rs.next()){
                edgeInfo.add(new Node(rs.getString("Name")));
            }
            return edgeInfo;
        }
        catch (Exception e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Edge> getEdges(Node nodeIdFrom){
        try {
            if(!getConnection()){
                return Collections.emptyList();
            }
            // the mysql insert statement
            String query = "select nf.Name as NodeFrom, nt.Name as NodeTo, e.weight as Weight " +
                    "from edges e inner join nodes nf on nf.ID = e.FromNode " +
                    "inner join nodes nt on nt.ID = e.ToNode where nf.Name = ?;";
            // create the mysql insert prepared statement
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setString(1, nodeIdFrom.toString());
            // execute the prepared statement
            ResultSet rs=preparedStmt.executeQuery();
            List<Edge> edgeInfo = new ArrayList<>();
            while (rs.next()){
                edgeInfo.add(new Edge(new Node(rs.getString("NodeFrom")),
                        new Node(rs.getString("NodeTo")),rs.getDouble("Weight") ));
            }
            return edgeInfo;
        }
        catch (Exception e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Integer> getTrainNosForStation(List<String> stationIds){
        try {
            if(!getConnection()){
                return Collections.emptyList();
            }
            requireNonNull(stationIds, "stationId cant be null.");
            if(stationIds.size()<=0){
                System.out.println("empty station ids list");
                return Collections.emptyList();
            }
            // the mysql insert statement
            StringBuilder query = new StringBuilder("");
            query.append("select distinct trainNum from stoppage where stationId in (");
            for(String stationId:stationIds){
                query.append("'");
                query.append(stationId);
                query.append("'");
                query.append(",");
            }
            query.deleteCharAt(query.length()-1);
            query.append(");");
            // create the mysql insert prepared statement
            PreparedStatement preparedStmt = con.prepareStatement(query.toString());
            // Array array = con.createArrayOf("VARCHAR", stationId.toArray());
            // execute the prepared statement
            ResultSet rs=preparedStmt.executeQuery();
            List<Integer> trainNos = new ArrayList<>();
            while (rs.next()){
                trainNos.add(rs.getInt("trainNum"));
            }
            return trainNos;
        }
        catch (Exception e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Integer> getTrainNosForDay(int day){
        try {
            if(!getConnection()){
                return Collections.emptyList();
            }
            // the mysql insert statement
            String query = "select distinct Num from train where ";
            // create the mysql insert prepared statement
            switch (day){
                case 0:
                    query += "travelSun";
                    break;
                case 1:
                    query += "travelMon";
                    break;
                case 2:
                    query += "travelTue";
                    break;
                case 3:
                    query += "travelWed";
                    break;
                case 4:
                    query += "travelThu";
                    break;
                case 5:
                    query += "travelFri";
                    break;
                case 6:
                    query += "travelSat";
                    break;
                default:
                    System.out.print("Invalid day");
                    return Collections.emptyList();
            }
            query += "= 'Y';";
            PreparedStatement preparedStmt = con.prepareStatement(query);
            // execute the prepared statement
            ResultSet rs=preparedStmt.executeQuery();
            List<Integer> trainNos = new ArrayList<>();
            while (rs.next()){
                trainNos.add(rs.getInt("Num"));
            }
            return trainNos;
        }
        catch (Exception e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public double getEdgeWeight(Node nodeIdFrom, Node nodeIdTo){
        try {
            if(!getConnection()){
                return -1;
            }
            // the mysql insert statement
            String query = "select * from edges where FromNode = (select ID from nodes where Name = ?) " +
                    "and ToNode = (select ID from nodes where Name = ?);";
            // create the mysql insert prepared statement
            PreparedStatement preparedStmt = con.prepareStatement(query);

            preparedStmt.setString(1, nodeIdFrom.toString());
            preparedStmt.setString(2, nodeIdTo.toString());
            // execute the prepared statement
            ResultSet rs=preparedStmt.executeQuery();
            if(rs.next()){
                return rs.getDouble("Weight");
            }
            else{
                return -4;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return -5;
        }
    }
}
