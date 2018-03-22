import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FetchTrainList {

    public void getTrainList(String pathRoute, String pathUpTrainList, String pathDownTrainList) {
        StringBuilder output = new StringBuilder("");
        Set<String> stationIds = new HashSet<>();
        List<String> stationIdsList = new ArrayList<>();
        FileReader fReader;
        BufferedReader bReader;
        String line;
        try {
            fReader = new FileReader(pathRoute);
            bReader = new BufferedReader(fReader);
            while((line = bReader.readLine()) != null) {
                String stationId = Scheduler.getStationIdFromName(line.split("\\s+")[0]);
                if(stationIds.add(stationId)){
                    stationIdsList.add(stationId);
                }
            }
            bReader.close();
            fReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        for(int i=0;i<7;i++) {
            output.append(i);
            // String url = "http://api.railwayapi.com/v2/between/source/"+firstStationId+"/dest/"+secondStationId+
            //         "/date/" +(5+i) +"-02-2018/apikey/o2w0ov08yw/";
            Set<Integer> trainNosSet = new HashSet<>();

            for(int j=0;j<stationIdsList.size()-1;j++){
                for(int k=j+1;k<stationIdsList.size();k++){
                    String url = "http://indianrailapi.com/api/v1/trainsbetweenstations/apikey/" +
                            "0f27b1b20ed3eef73adaeb2a30c12ea4/date/201802"+(12+i)+"/source/"+
                            stationIdsList.get(j).toUpperCase()+"/destination/"+stationIdsList.get(k).toUpperCase()+"/";

                    // Connect to the URL using java's native library
                    try {
                        HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
                        request.connect();

                        // Convert to a JSON object to print data
                        JsonParser jsonParser = new JsonParser(); //from gson.jar
                        JsonObject rootObj = jsonParser.parse(new InputStreamReader((InputStream) request.getContent()))
                                .getAsJsonObject(); //May be an array, may be an object.
                        int total = rootObj.get("Total").getAsInt();
                        if(total==0){
                            continue;
                        }
                        JsonArray trainNos = rootObj.get("Trains").getAsJsonArray();
                        System.out.println(stationIdsList.get(j).toUpperCase() + " -> " + stationIdsList.get(k).toUpperCase() +
                        " "+ trainNos.toString());
                        for (int i1 = 0; i1 < total; i1++) {
                            trainNosSet.add(trainNos.get(i1).getAsJsonObject().get("Number").getAsInt());
                        }
                        request.disconnect();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            List<Integer> trainNosList = new ArrayList<>(trainNosSet);

            for(int trainNo : trainNosList){
                output.append('\t');
                output.append(trainNo);
            }
            output.append('\n');
        }
        System.out.println(output.toString());
        new WriteToFile().write(pathUpTrainList, output.toString(), false);

        output = new StringBuilder("");
        for(int i=0;i<7;i++) {
            output.append(i);
            // String url = "http://api.railwayapi.com/v2/between/source/"+firstStationId+"/dest/"+secondStationId+
            //         "/date/" +(5+i) +"-02-2018/apikey/o2w0ov08yw/";
            Set<Integer> trainNosSet = new HashSet<>();

            for(int j=stationIdsList.size()-1;j>0;j--){
                for(int k=j-1;k>=0;k--){
                    String url = "http://indianrailapi.com/api/v1/trainsbetweenstations/apikey/" +
                            "f3db6f2060474ec459616f48a1f8a003/date/201802"+(12+i)+"/source/"+
                            stationIdsList.get(j).toUpperCase()+"/destination/"+stationIdsList.get(k).toUpperCase()+"/";

                    // Connect to the URL using java's native library
                    try {
                        HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
                        request.connect();

                        // Convert to a JSON object to print data
                        JsonParser jsonParser = new JsonParser(); //from gson.jar
                        JsonObject rootObj = jsonParser.parse(new InputStreamReader((InputStream) request.getContent()))
                                .getAsJsonObject(); //May be an array, may be an object.
                        int total = rootObj.get("Total").getAsInt();
                        if(total==0){
                            continue;
                        }
                        JsonArray trainNos = rootObj.get("Trains").getAsJsonArray();
                        for (int i1 = 0; i1 < total; i1++) {
                            trainNosSet.add(trainNos.get(i1).getAsJsonObject().get("Number").getAsInt());
                        }
                        request.disconnect();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            List<Integer> trainNosList = new ArrayList<>(trainNosSet);

            for(int trainNo : trainNosList){
                output.append('\t');
                output.append(trainNo);
            }
            output.append('\n');
        }
        System.out.println(output.toString());
        new WriteToFile().write(pathDownTrainList, output.toString(), false);
    }
}
