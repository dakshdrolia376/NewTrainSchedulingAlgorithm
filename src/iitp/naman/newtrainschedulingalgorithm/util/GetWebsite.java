package iitp.naman.newtrainschedulingalgorithm.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Helper class to fetch a website and store in a file.
 */
public class GetWebsite {

    /**
     * @param url      url for website.
     * @param pathFile path where the html content needs to be stored.
     * @return true if successful.
     */
    public boolean getWebsite(String url, String pathFile) {
        url = url.replaceAll(" ", "%20");
        System.out.println("***Crawling : " + url);
        try {
            Thread.sleep(50);
            //without proper User-Agent, we will get 403 error
            Document doc = Jsoup.connect(url)
                    .maxBodySize(0)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/41.0.2228.0 Safari/537.36")
                    .timeout(30000)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .get();
            //below will print HTML data, save it to a file.
            new WriteToFile().write(pathFile, doc.html(), false);
            System.out.println("Website fetched.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
