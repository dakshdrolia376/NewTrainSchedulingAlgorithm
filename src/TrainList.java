import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TrainList {

    public void getTrainList(String pathFile) {
        StringBuilder output = new StringBuilder("");
        for(int i=0;i<7;i++) {
            output.append(i);
            String url = "http://api.railwayapi.com/v2/between/source/pnbe/dest/mgs/date/"
                    +(15+i) +"-01-2018/apikey/jg75kd7lc1/";

            // Connect to the URL using java's native library
            try {
                HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
                request.connect();

                // Convert to a JSON object to print data
                JsonParser jsonParser = new JsonParser(); //from gson.jar
                JsonObject rootObj = jsonParser.parse(new InputStreamReader((InputStream) request.getContent()))
                        .getAsJsonObject(); //May be an array, may be an object.
                int total = rootObj.get("total").getAsInt();
                JsonArray trainNos = rootObj.get("trains").getAsJsonArray();
                for (int i1 = 0; i1 < total; i1++) {
                    output.append('\t');
                    output.append(trainNos.get(i1).getAsJsonObject().get("number").getAsString());
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            output.append('\n');
        }
        new WriteToFile().write(pathFile, output.toString(), false);
    }
}
