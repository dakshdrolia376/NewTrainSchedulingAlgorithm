import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("unused")
public class TrainList {

    @SuppressWarnings("unused")
    TrainList(){

    }

    @SuppressWarnings("unused")
    public void getTrainList(String pathFile) {
        StringBuilder output = new StringBuilder("");
        for(int i=0;i<7;i++) {
            output.append(i);
            String url1 = "http://api.railwayapi.com/v2/between/source/pnbe/dest/mgs/date/"+(15+i) +"-01-2018/apikey/jg75kd7lc1/";

            // Connect to the URL using java's native library
            try {
                URL url = new URL(url1);
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.connect();

                // Convert to a JSON object to print data
                JsonParser jsonParser = new JsonParser(); //from gson.jar
                JsonElement root = jsonParser.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
                JsonObject rootObj = root.getAsJsonObject(); //May be an array, may be an object.
                int total = rootObj.get("total").getAsInt();
                JsonArray train_nos = rootObj.get("trains").getAsJsonArray();
                String train_no;
                for (int i1 = 0; i1 < total; i1++) {
                    train_no = train_nos.get(i1).getAsJsonObject().get("number").getAsString();
                    output.append('\t');
                    output.append(train_no);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            output.append('\n');
        }
        WriteToFile.write(pathFile, output.toString(), false);
    }
}
