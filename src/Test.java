import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Test {

    public static void main(String... s){
        String url = "http://api.railwayapi.com/v2/name-number/train/" + 12878 + "/apikey/o2w0ov08yw/";
        try {
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
            request.connect();

            // Convert to a JSON object to print data
            JsonParser jsonParser = new JsonParser(); //from gson.jar
            JsonObject rootObj = jsonParser.parse(new InputStreamReader((InputStream) request.getContent()))
                    .getAsJsonObject(); //May be an array, may be an object
            System.out.print(rootObj.toString());
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

}
