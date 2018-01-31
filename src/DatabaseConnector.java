import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                        "jdbc:mysql://localhost:7888/trainscheduler", "root", "bittu420");
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
        if(!getConnection()){
            return false;
        }
        try {
            // the mysql insert statement
            System.out.println("Initializing database");
            con.setAutoCommit(false);
            Statement stmt = con.createStatement();
            String query = "truncate edges";
            stmt.executeUpdate(query);
            query = "delete from edges";
            stmt.executeUpdate(query);
            query = "delete from nodes";
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
        if(!nodeId.isValid()){
            System.out.println("Node is invalid");
            return false;
        }
        if(!getConnection()){
            return false;
        }
        try {
            // the mysql insert statement
            con.setAutoCommit(false);
            String query = "insert into nodes (Name) values (?)";
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
            // System.out.println(nodeIds.toString());
            // the mysql insert statement
            String query = "insert into nodes (Name) values (?)";
            // create the mysql insert prepared statement
            con.setAutoCommit(false);
            PreparedStatement preparedStmt = con.prepareStatement(query);
            final int batchSize = 1000;
            int count = 0;

            for (Node nodeId : nodeIds) {
                if (!nodeId.isValid()) {
                    // System.out.println("Node is invalid");
                    continue;
                }
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

    @SuppressWarnings("unused")
    public boolean insertIntoEdge(Edge edge){
        if(!getConnection()){
            return false;
        }
        try {
            if(edge.getWeight()<0){
                System.out.println("Edge weight cant be negative");
                return false;
            }
            con.setAutoCommit(false);
            // the mysql insert statement
            String query = "insert into edges (FromNode, Weight, ToNode) values ((select ID from nodes where Name = ?)," +
                    "?,(select ID from nodes where Name = ?))";
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
        if(!getConnection()){
            return false;
        }
        try {
            // the mysql insert statement
            String query = "insert into edges (FromNode, Weight, ToNode) values ((select ID from nodes where Name = ?)" +
                    ",?,(select ID from nodes where Name = ?))";
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
        if(!getConnection()){
            return Collections.emptyList();
        }
        try {
            // the mysql insert statement
            String query = "select * from nodes";
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
            System.out.println(e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Edge> getEdges(Node nodeIdFrom){
        if(!getConnection()){
            return Collections.emptyList();
        }
        try {
            if(!nodeIdFrom.isValid()){
                System.out.println("Invalid node");
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

    public double getEdgeWeight(Node nodeIdFrom, Node nodeIdTo){
        if(!getConnection()){
            return -1;
        }
        try {
            if(!nodeIdFrom.isValid() || !nodeIdTo.isValid()){
                return Double.MAX_VALUE;
            }
            // the mysql insert statement
            String query = "select * from edges where FromNode = (select ID from nodes where Name = ?) " +
                    "and ToNode = (select ID from nodes where Name = ?)";
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
