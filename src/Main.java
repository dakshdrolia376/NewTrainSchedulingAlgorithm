import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Main {
    private static boolean isNetAvailable() {
        try {
            final URL url = new URL("http://www.iitp.ac.in");
            final URLConnection conn = url.openConnection();
            conn.connect();
            return true;
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            return false;
        }
    }

    private static final long MEGABYTE = 1024L * 1024L;

    private static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }
    @SuppressWarnings("unused")
    public static void getRuntimeMemory(){
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Start Used memory is bytes: " + memory);
        System.out.println("Start Used memory is megabytes: " + bytesToMegabytes(memory));
    }



    public static void main(String[] args) {
        String pathTrainList = "data" + File.separator +"train_list.txt";
        String pathTemp = "data"+File.separator+"temp";
        String pathLog = "data"+File.separator+"logs";
        String pathFinal = "data"+File.separator+"final";
        String pathRoute = "data"+File.separator+"route"+File.separator+"route.txt";
        String pathBestRoute = "data"+File.separator+"bestRoute";
        String pathOldTrainSchedule = "data"+File.separator+"final" + File.separator + "dayall";

        File file = new File(pathTrainList);
        if(!file.getParentFile().exists()){
            if(!file.getParentFile().mkdirs()){
                System.out.println("Unable to create file " + pathTemp);
                System.exit(0);
            }
        }

        file = new File(pathTemp);
        if(!file.exists()){
            if(!file.mkdirs()){
                System.out.println("Unable to create file " + pathTemp);
                System.exit(0);
            }
        }

        file = new File(pathFinal);
        if(!file.exists()){
            if(!file.mkdirs()){
                System.out.println("Unable to create file " + pathFinal);
                System.exit(0);
            }
        }

        file = new File(pathLog);
        if(!file.exists()){
            if(!file.mkdirs()){
                System.out.println("Unable to create file " + pathLog);
                System.exit(0);
            }
        }

        file = new File(pathBestRoute);
        if(!file.exists()){
            if(!file.mkdirs()){
                System.out.println("Unable to create file " + pathBestRoute);
                System.exit(0);
            }
        }

        if(!isNetAvailable()){
            System.out.println("No internet Connection.. Try again");
            System.exit(0);
        }
        try {
            // Creating a File object that represents the disk file.
            PrintStream o = new PrintStream(new File(pathLog + File.separator+"err.log"));
            PrintStream o1 = new PrintStream(new File(pathLog + File.separator+"output.log"));
           // Store current System.out before assigning a new value
           // PrintStream console = System.err;
           // PrintStream console1 = System.out;
           //
           // Assign o to output stream
            System.setErr(o);
            System.setOut(o1);

           // new TrainList().getTrainList(pathTrainList);
           // new TrainStoppageList().getTrainStoppageFromFile(pathTrainList,pathTemp,pathFinal);

            new Scheduler().scheduleNewTrain(pathRoute, pathBestRoute, pathOldTrainSchedule);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
