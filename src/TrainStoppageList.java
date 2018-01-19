import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@SuppressWarnings("unused")
public class TrainStoppageList {

    private void getWebsite(String url, String pathFile) {
        url = url.replaceAll(" ", "%20");
        System.out.println("***Crawling : " + url);
        try{
            Thread.sleep(4000);
            //without proper User-Agent, we will get 403 error
            Document doc = Jsoup.connect(url).maxBodySize(0).userAgent("Mozilla/17.0 Chrome/26.0.1410.64 Safari/537.31").timeout(30000).get();
            System.out.println("fetching done");
            //below will print HTML data, save it to a file.
            new WriteToFile().write(pathFile ,doc.html(),false);
            System.out.println("train website saved");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private String parseTrainScheduleWebsite(String filename, String pathTemp, String trainNo) {
        System.out.println("Parsing " + filename);

        BufferedWriter bWriter;
        FileWriter fWriter;
        FileReader fReader;
        BufferedReader bReader;

        Pattern pattern = Pattern.compile("href=\"/departures/.*");
        Pattern pattern_time = Pattern.compile(">\\d+:\\d+<");
        Pattern pattern_km = Pattern.compile(">\\d+[.]\\d+<");
        Matcher matcher;
        String pathTrainSchedule = null;
        try {
            fReader = new FileReader(filename);
            bReader = new BufferedReader(fReader);
            pathTrainSchedule = pathTemp+ File.separator+trainNo+"_schedule_all.txt";
            fWriter = new FileWriter(pathTrainSchedule);
            bWriter = new BufferedWriter(fWriter);
            boolean station_started = false;
            boolean second_line_station = false;
            String station_name = "";
            String arrival="";
            String departure="";
            String km = "";
            String line;
            int stoppage_no = 0;
            while((line = bReader.readLine()) != null) {
                if(!line.contains("href=\"/departures/") && !station_started) {
                    continue;
                }
                if(!station_started) {
                    matcher = pattern.matcher(line);
                    station_started = matcher.find();
                    continue;
                }
                else if(!second_line_station){
                    matcher = pattern.matcher(line);
                    if(matcher.find()) {
                        second_line_station = true;
                        String temp = matcher.group();
                        station_name = temp.split("/")[2];
                    }
                    else {
                        station_started = false;
                        second_line_station = false;
                    }
                    continue;
                }
                else {
                    if(stoppage_no==0) {
                        arrival = "start";
                    }
                    matcher = pattern_time.matcher(line);
                    if(matcher.find()) {
                        String temp_abc = matcher.group();
                        if(arrival.equals("")) {
                            arrival = temp_abc.substring(1,temp_abc.length()-1);
                        }
                        else {
                            departure = temp_abc.substring(1,temp_abc.length()-1);
                        }
                    }
                    else {
                        matcher = pattern_km.matcher(line);
                        if(matcher.find()) {
                            String temp_abc = matcher.group();
                            km = temp_abc.substring(1,temp_abc.length()-1);
                        }
                    }
                }

                if(!station_name.equals("")&&!((arrival.equals("")||arrival.equals("start")) && departure.equals(""))&&!km.equals("")) {
                    if(departure.equals("")) {
                        if(!arrival.equals("") && !arrival.equals("start")) {
                            String temp_arrival[] = arrival.split(":");
                            int hour = Integer.parseInt(temp_arrival[0]);
                            int minutes  = Integer.parseInt(temp_arrival[1]);
                            minutes = minutes + 20;
                            if(minutes>60) {
                                minutes = minutes - 60;
                                hour++;
                                if(hour>23) {
                                    hour = 0;
                                }
                            }
                            String hour1 = hour + "";
                            if(hour1.length()==1) {
                                hour1 = "0" +hour1;
                            }
                            String minutes1 = minutes + "";
                            if(minutes1.length()==1) {
                                minutes1 = "0" +minutes1;
                            }
                            departure = hour1 + ":" + minutes1;
                        }
                    }
                    else {
                        if(arrival.equalsIgnoreCase("start")) {
                            String temp_departure[] = departure.split(":");
                            int hour = Integer.parseInt(temp_departure[0]);
                            int minutes  = Integer.parseInt(temp_departure[1]);
                            minutes = minutes - 20;
                            if(minutes<0) {
                                minutes = minutes + 60;
                                hour--;
                                if(hour<0) {
                                    hour = 23;
                                }
                            }
                            String hour1 = hour + "";
                            if(hour1.length()==1) {
                                hour1 = "0" +hour1;
                            }
                            String minutes1 = minutes + "";
                            if(minutes1.length()==1) {
                                minutes1 = "0" +minutes1;
                            }
                            arrival = hour1 + ":" + minutes1;
                        }

                    }
                    //System.out.println(station_name + "\t" +arrival + "\t" + departure + "\t" + km + "\n");
                    bWriter.write(station_name + "\t" +arrival + "\t" + departure + "\t" + km + "\n");
                    station_name = "";
                    arrival = "";
                    departure = "";
                    km = "";
                    station_started = false;
                    second_line_station = false;
                    stoppage_no++;
                }
            }
            bWriter.close();
            fWriter.close();
            fReader.close();
            bReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return pathTrainSchedule;
    }

    private String getTrainStoppageAll(String trainNo, String pathTemp){
        FileReader fReader;
        BufferedReader bReader;
        FileReader fReader2;
        BufferedReader bReader2;
        Boolean parsable=false;
        Pattern pattern = Pattern.compile("<a href=\"/url\\?q=.*?</a>");
        Pattern patternIndiaRail = Pattern.compile("https://indiarailinfo.com/train/(timetable/)?.*?/\\d+");
        Pattern patternIndiaRailAll = Pattern.compile("<a href=\".*?\">Show ALL intermediate Stations</a>");
        Matcher matcher;
        Matcher matcher2;
        Matcher matcher3;
        String searchUrl = "https://www.google.com/search?q="+trainNo+"%20indiarailinfo%20timetable";
        String pathTrainGoogleSearchPage = pathTemp+File.separator+trainNo+"_google_search.txt";
        String pathTrainWebsite = pathTemp + File.separator+ trainNo + "_website.txt";
        String pathTrainScheduleWebsite = pathTemp + File.separator+ trainNo + "_schedule_website.txt";
        File file = new File(pathTrainGoogleSearchPage);
        if(!file.exists()) {
            getWebsite(searchUrl, pathTrainGoogleSearchPage);
        }
        try {
            fReader = new FileReader(pathTrainGoogleSearchPage);
            bReader = new BufferedReader(fReader);
            String temp_line;
            while ((temp_line = bReader.readLine()) != null) {
                matcher = pattern.matcher(temp_line);
                if (matcher.find()) {
                    matcher2 = patternIndiaRail.matcher(matcher.group());
                    if (matcher2.find()) {
                        file = new File(pathTrainWebsite);
                        if (!file.exists()) {
                            getWebsite(matcher2.group(), pathTrainWebsite);
                        }
                        fReader2 = new FileReader(pathTrainWebsite);
                        bReader2 = new BufferedReader(fReader2);
                        String line_all_stations;
                        while ((line_all_stations = bReader2.readLine()) != null) {
                            matcher3 = patternIndiaRailAll.matcher(line_all_stations);
                            if (matcher3.find()) {
                                String all_station = matcher3.group();
                                String link = "https://indiarailinfo.com" + all_station.split("\"")[1];
                                file = new File(pathTrainScheduleWebsite);
                                if (!file.exists()) {
                                    getWebsite(link, pathTrainScheduleWebsite);
                                }
                                parsable = true;
                                break;
                            }
                        }
                        fReader2.close();
                        bReader2.close();
                        break;
                    }
                }
            }
            fReader.close();
            bReader.close();
            if(parsable) {
                return parseTrainScheduleWebsite(pathTrainScheduleWebsite, pathTemp, trainNo);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void getTrainStoppagePNBEtoMGS(String pathFileToParse, String pathParsedFile) {
        BufferedWriter bWriter;
        FileWriter fWriter;
        FileReader fReader;
        BufferedReader bReader;
        Pattern patternPNBE = Pattern.compile("patna-junction-pnbe.*");
        Pattern patternMGS = Pattern.compile("mughal-sarai-junction-mgs.*");

        Matcher matcher;
        try {
            fReader = new FileReader(pathFileToParse);
            bReader = new BufferedReader(fReader);
            fWriter = new FileWriter(pathParsedFile);
            bWriter = new BufferedWriter(fWriter);
            String line;
            boolean startFound = false;
            while((line = bReader.readLine()) != null) {
                matcher = patternPNBE.matcher(line);
                if(matcher.find()) {
                    startFound = true;
                }
                if(startFound) {
                    bWriter.write(line+"\n");
                }
                matcher = patternMGS.matcher(line);
                if(matcher.find()) {
                    break;
                }
            }
            bWriter.close();
            fWriter.close();
            fReader.close();
            bReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void getTrainStoppageFromFile(String filename, String pathTemp, String pathFinal) {
        FileReader fReader;
        BufferedReader bReader;
        try {
            fReader = new FileReader(filename);
            bReader = new BufferedReader(fReader);
            String line;

            while((line = bReader.readLine())!=null) {
                String[] data = line.split("\\s+");
                String day = "day" +data[0];
                for(int i=1;i<data.length;i++) {
                    String pathTrainScheduleAll = getTrainStoppageAll(data[i],pathTemp);
                    String pathTrainSchedulePNBEtoMGS = pathFinal+File.separator+day+File.separator +data[i]+".txt";
                    File fileFinal = new File(pathTrainSchedulePNBEtoMGS);
                    if(!fileFinal.getParentFile().exists()){
                        if(!(fileFinal.getParentFile()).mkdirs()){
                            System.out.println("Unable to create folder");
                            return;
                        }
                    }
                    getTrainStoppagePNBEtoMGS(pathTrainScheduleAll, pathTrainSchedulePNBEtoMGS);
                }
            }
            fReader.close();
            bReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
