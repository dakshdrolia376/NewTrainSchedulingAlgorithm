import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class GetWebsite {
    public boolean getWebsite(String url, String pathFile) {
        url = url.replaceAll(" ", "%20");
        System.out.println("***Crawling : " + url);
        try{
            Thread.sleep(4000);
            //without proper User-Agent, we will get 403 error
            Document doc = Jsoup.connect(url).maxBodySize(0)
                    .userAgent("Mozilla/17.0 Chrome/26.0.1410.64 Safari/537.31").timeout(30000).get();
            System.out.println("fetching done");
            //below will print HTML data, save it to a file.
            new WriteToFile().write(pathFile ,doc.html(),false);
            System.out.println("train website saved");
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
