package iitp.naman.newtrainschedulingalgorithm.datahelper;

import iitp.naman.newtrainschedulingalgorithm.util.DatabaseConnector;
import iitp.naman.newtrainschedulingalgorithm.util.TrainTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Helper class for storing station & train data in database.
 */
public class DatabaseHelper extends DatabaseConnector {

    public DatabaseHelper() {
        super();
        if (!createTable()) {
            throw new RuntimeException("Cannot create table in database");
        }
    }

    /**
     * Creates the required tables in database.
     *
     * @return true if successful.
     */
    private boolean createTable() {
        try {
            String sqlCreateTrain = "CREATE TABLE IF NOT EXISTS train" +
                    " (Name varchar(255) NOT NULL," +
                    " Num int NOT NULL," +
                    " Type varchar(255) NOT NULL," +
                    " trainIndex varchar(255) NOT NULL," +
                    " travelSun varchar(1) NOT NULL," +
                    " travelMon varchar(1) NOT NULL," +
                    " travelTue varchar(1) NOT NULL," +
                    " travelWed varchar(1) NOT NULL," +
                    " travelThu varchar(1) NOT NULL," +
                    " travelFri varchar(1) NOT NULL," +
                    " travelSat varchar(1) NOT NULL," +
                    " count int DEFAULT 1," +
                    " DuplicateIndexes varchar(10000) DEFAULT ''," +
                    " PRIMARY KEY(Num))";

            String sqlCreateStation = "CREATE TABLE IF NOT EXISTS station" +
                    " (Name varchar(255) NOT NULL," +
                    " ID varchar(255) NOT NULL," +
                    " StationIndex varchar(255) NOT NULL," +
                    " StationType varchar(255) NOT NULL," +
                    " Track varchar(255) NOT NULL," +
                    " OriginatingTrains int," +
                    " TerminatingTrains int," +
                    " HaltingTrains int," +
                    " Platforms int," +
                    " Elevation varchar(255)," +
                    " RailwayZone varchar(255)," +
                    " Address varchar(5000)," +
                    " count int DEFAULT 1," +
                    " DuplicateIndexes varchar(5000) DEFAULT ''," +
                    " PRIMARY KEY(ID))";

            String sqlCreateStoppage = "CREATE TABLE IF NOT EXISTS stoppage " +
                    " (stationId varchar(255) NOT NULL," +
                    " trainNum int NOT NULL," +
                    " arrival varchar(255) NOT NULL," +
                    " departure varchar(255) NOT NULL," +
                    " distance double(15,4) not null," +
                    " count int DEFAULT 1," +
                    " duplicateStoppages varchar(10000) DEFAULT ''," +
                    " PRIMARY KEY(trainNum, stationId, distance)," +
                    " FOREIGN KEY (stationId) REFERENCES station(ID) ON DELETE CASCADE ON UPDATE CASCADE," +
                    " FOREIGN KEY (trainNum) REFERENCES train(Num) ON DELETE CASCADE ON UPDATE CASCADE)";

            Statement stmt = connection.createStatement();
            stmt.execute(sqlCreateTrain);
            stmt.execute(sqlCreateStation);
            stmt.execute(sqlCreateStoppage);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Insert train details into the table.
     *
     * @param trainDetails list of train.
     * @return true if successful.
     */
    public boolean insertIntoTrainBatch(Map<Integer, List<String>> trainDetails) {
        try {
            if (!connect()) {
                return false;
            }
            String query = "INSERT INTO train (Name, Num, Type, trainIndex, travelSun, travelMon, travelTue, travelWed, " +
                    "travelThu, travelFri, travelSat) values (?,?,?,?,?,?,?,?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE count=count+1, " +
                    "DuplicateIndexes = CONCAT(DuplicateIndexes,\",\",?);";
            connection.setAutoCommit(false);
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            final int batchSize = 1000;
            int count = 0;

            for (int trainIndex : trainDetails.keySet()) {
                List<String> trainDetail = trainDetails.get(trainIndex);
                preparedStmt.setString(1, trainDetail.get(1));
                preparedStmt.setInt(2, Integer.parseInt(trainDetail.get(0)));
                preparedStmt.setString(3, trainDetail.get(3));
                preparedStmt.setString(4, (trainIndex + ""));
                preparedStmt.setString(5, trainDetail.get(2).charAt(0) == '1' ? "Y" : "N");
                preparedStmt.setString(6, trainDetail.get(2).charAt(1) == '1' ? "Y" : "N");
                preparedStmt.setString(7, trainDetail.get(2).charAt(2) == '1' ? "Y" : "N");
                preparedStmt.setString(8, trainDetail.get(2).charAt(3) == '1' ? "Y" : "N");
                preparedStmt.setString(9, trainDetail.get(2).charAt(4) == '1' ? "Y" : "N");
                preparedStmt.setString(10, trainDetail.get(2).charAt(5) == '1' ? "Y" : "N");
                preparedStmt.setString(11, trainDetail.get(2).charAt(6) == '1' ? "Y" : "N");
                preparedStmt.setString(12, (trainIndex + ""));
                preparedStmt.addBatch();
                if (++count % batchSize == 0) {
                    preparedStmt.executeBatch();
                    count = 0;
                }
            }
            preparedStmt.executeBatch();
            connection.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Insert station details into database.
     *
     * @param stationDetails list of station.
     * @return true if successful.
     */
    public boolean insertIntoStationBatch(Map<Integer, List<String>> stationDetails) {
        try {
            if (!connect()) {
                return false;
            }
            String query = "insert into station (Name, ID, StationIndex, StationType, Track,  OriginatingTrains, " +
                    "TerminatingTrains, HaltingTrains, Platforms, Elevation, RailwayZone, Address) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE count=count+1, " +
                    "DuplicateIndexes = CONCAT(DuplicateIndexes,\",\",?);";

            connection.setAutoCommit(false);
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            final int batchSize = 1000;
            int count = 0;

            for (int stationIndex : stationDetails.keySet()) {
                List<String> stationDetail = stationDetails.get(stationIndex);
                preparedStmt.setString(1, stationDetail.get(0));
                preparedStmt.setString(2, stationDetail.get(1));
                preparedStmt.setString(3, (stationIndex + ""));
                preparedStmt.setString(4, stationDetail.get(2));
                preparedStmt.setString(5, stationDetail.get(3));
                preparedStmt.setInt(6, Integer.parseInt(stationDetail.get(4)));
                preparedStmt.setInt(7, Integer.parseInt(stationDetail.get(5)));
                preparedStmt.setInt(8, Integer.parseInt(stationDetail.get(6)));
                preparedStmt.setInt(9, Integer.parseInt(stationDetail.get(7)));
                preparedStmt.setString(10, stationDetail.get(8));
                preparedStmt.setString(11, stationDetail.get(9));
                preparedStmt.setString(12, stationDetail.get(10));
                preparedStmt.setString(13, (stationIndex + ""));
                preparedStmt.addBatch();
                if (++count % batchSize == 0) {
                    preparedStmt.executeBatch();
                    count = 0;
                }
            }
            preparedStmt.executeBatch();
            connection.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Insert stoppage details into database.
     *
     * @param trainNos   list of train numbers.
     * @param stationIds list of stations ids.
     * @param arrivals   list of arrivals time for train.
     * @param departures list of departure time for train.
     * @param distances  list of distance for station.
     * @return true if successful.
     */
    public boolean insertIntoStoppageBatch(List<Integer> trainNos, List<String> stationIds, List<TrainTime> arrivals,
                                           List<TrainTime> departures, List<Double> distances) {
        try {
            if (!connect()) {
                return false;
            }
            String query = "insert into stoppage (stationId, trainNum, arrival, departure, distance) values (?,?,?,?,?)" +
                    " ON DUPLICATE KEY UPDATE count=count+1, " +
                    "duplicateStoppages = CONCAT(duplicateStoppages,\",\",?);";

            connection.setAutoCommit(false);
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            final int batchSize = 1000;
            int count = 0;

            for (int i = 0; i < stationIds.size(); i++) {
                preparedStmt.setString(1, stationIds.get(i));
                preparedStmt.setInt(2, trainNos.get(i));
                preparedStmt.setString(3, arrivals.get(i).getTimeString());
                preparedStmt.setString(4, departures.get(i).getTimeString());
                preparedStmt.setDouble(5, distances.get(i));
                preparedStmt.setString(6, arrivals.get(i).getTimeString() + ">" +
                        departures.get(i).getTimeString() + ">" + distances.get(i).toString());
                preparedStmt.addBatch();
                if (++count % batchSize == 0) {
                    preparedStmt.executeBatch();
                    count = 0;
                }
            }
            preparedStmt.executeBatch();
            connection.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param stationIds list of station ids.
     * @return list of train numbers stopping at least 1 station.
     */
    public List<Integer> getTrainNosForStation(List<String> stationIds) {
        try {
            if (!connect()) {
                return Collections.emptyList();
            }
            requireNonNull(stationIds, "stationId cant be null.");
            if (stationIds.size() <= 0) {
                System.out.println("empty station ids list");
                return Collections.emptyList();
            }
            StringBuilder query = new StringBuilder("");
            query.append("select distinct trainNum from stoppage where stationId in (");
            for (String stationId : stationIds) {
                query.append("'");
                query.append(stationId);
                query.append("'");
                query.append(",");
            }
            query.deleteCharAt(query.length() - 1);
            query.append(");");
            PreparedStatement preparedStmt = connection.prepareStatement(query.toString());
            ResultSet rs = preparedStmt.executeQuery();
            List<Integer> trainNos = new ArrayList<>();
            while (rs.next()) {
                trainNos.add(rs.getInt("trainNum"));
            }
            return trainNos;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * @param day day
     * @return list of train numbers origination from source station on given day.
     */
    public List<Integer> getTrainNosForDay(int day) {
        try {
            if (!connect()) {
                return Collections.emptyList();
            }
            String query = "select distinct Num from train where ";
            switch (day) {
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
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            ResultSet rs = preparedStmt.executeQuery();
            List<Integer> trainNos = new ArrayList<>();
            while (rs.next()) {
                trainNos.add(rs.getInt("Num"));
            }
            return trainNos;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * @param stationId station id.
     * @return list of trains, arrival & departure for a given station.
     */
    public List<Map<String, String>> getScheduleForStation(String stationId) {
        try {
            if (!connect()) {
                return Collections.emptyList();
            }
            requireNonNull(stationId, "stationId cant be null.");
            String query = "select stationId, trainNum, arrival, departure from stoppage where stationId='" + stationId + "';";
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            ResultSet rs = preparedStmt.executeQuery();
            List<Map<String, String>> trainSchedules = new ArrayList<>();
            Map<String, String> arrival = new HashMap<>();
            Map<String, String> departure = new HashMap<>();
            while (rs.next()) {
                arrival.put(rs.getString("trainNum"), rs.getString("arrival"));
                departure.put(rs.getString("trainNum"), rs.getString("departure"));
            }
            trainSchedules.add(arrival);
            trainSchedules.add(departure);
            return trainSchedules;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * @return list of trains details.
     */
    public List<List<String>> getAllTrainNames() {
        try {
            if (!connect()) {
                return Collections.emptyList();
            }
            String query = "select Name, Num, Type from train;";
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            ResultSet rs = preparedStmt.executeQuery();
            List<List<String>> ans = new ArrayList<>();
            while (rs.next()) {
                List<String> temp = new ArrayList<>();
                temp.add(rs.getString("Num"));
                temp.add(rs.getString("Name"));
                temp.add(rs.getString("Type"));
                ans.add(temp);
            }
            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}