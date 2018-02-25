import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        String pathUpTrainList = "data" + File.separator +"upTrainList.txt";
        String pathDownTrainList = "data" + File.separator +"downTrainList.txt";
        String pathSingleStoppageTrainList = "data" + File.separator +"ssTrainList.txt";

        String pathRoute = "data"+File.separator+"route"+File.separator+"routePnbeNdls.txt";

        String pathPlotFile = "data"+File.separator+"plot"+File.separator+"plot1.pdf";
        String pathTemp = "data"+File.separator+"temp";
        String pathLog = "data"+File.separator+"logs";
        String pathUpTrainBase = "data"+File.separator+"final" + File.separator+"upTrains";
        String pathDownTrainBase = "data"+File.separator+"final" +File.separator+"downTrains";
        String pathSSTrainBase = "data"+File.separator+"final" +File.separator+"ssTrains";

        String pathBestRoute = "data"+File.separator+"bestRoute";
        String pathStationDatabase = pathTemp+File.separator+"databaseStation";
        String pathTrainDatabase = pathTemp+File.separator+"databaseTrain";
        boolean usePreviousComputation = false;

        if(!Scheduler.createParentFolder(pathUpTrainList) || !Scheduler.createParentFolder(pathRoute)
                || !Scheduler.createParentFolder(pathPlotFile) || !Scheduler.createFolder(pathTemp)
                || !Scheduler.createFolder(pathLog) || !Scheduler.createFolder(pathUpTrainBase)
                || !Scheduler.createFolder(pathDownTrainBase) || !Scheduler.createFolder(pathBestRoute)
                || !Scheduler.createFolder(pathStationDatabase) || !Scheduler.createFolder(pathTrainDatabase)
                || !Scheduler.createFolder(pathSSTrainBase)){
            System.out.println("Unable to create directory");
            System.exit(1);
        }

        if(!Scheduler.isNetAvailable()){
            System.out.println("No internet Connection.. Some functionality may not work...");
        }

        try {
            // Creating a File object that represents the disk file.
            PrintStream o = new PrintStream(new File(pathLog + File.separator+"err.log"));
            PrintStream o1 = new PrintStream(new File(pathLog + File.separator+"output.log"));
            // Store current System.out before assigning a new value
            PrintStream console = System.err;
            PrintStream console1 = System.out;
            //
            // Assign o to output stream
            System.setErr(o);
            System.setOut(o1);

            Scheduler scheduler = new Scheduler();
            ScheduleByDivision scheduleByDivision = new ScheduleByDivision();
            int count = 0;
            double avgSpeed=80;
            double ratio = 1.3;
            int trainDay=7;
            boolean isSingleDay =false;

            // String pathOldUpTrainSchedule = pathUpTrainBase + "";
            // String pathOldDownTrainSchedule = pathDownTrainBase + "";
            // String pathOldSSTrainSchedule = pathSSTrainBase + "";
            // String pathBestRouteTemp = pathBestRoute + File.separator + count;
            // Scheduler.createFolder(pathBestRouteTemp);
            // System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
            //         " count "+count +" Type Full Day "+trainDay+" AvgSpeed "+avgSpeed +
            //         " maxRatio "+ratio +" conditional ");
            // scheduler.test(pathTemp, pathRoute, pathBestRouteTemp, pathOldUpTrainSchedule,
            //         pathOldDownTrainSchedule, pathOldSSTrainSchedule, isSingleDay, trainDay,
            //         usePreviousComputation, ratio, avgSpeed, pathLog, null);

            // for(avgSpeed=60;avgSpeed<120;avgSpeed+=5) {
            //         count++;
            //         isSingleDay = true;
            //         TrainTime sourceTime;
            //         String pathBestRouteTemp = pathBestRoute + File.separator + count;
            //         Scheduler.createFolder(pathBestRouteTemp);
            //         for (trainDay = 0; trainDay < 7; trainDay++) {
            //             sourceTime  = new TrainTime(trainDay,12,12);
            //             String pathOldUpTrainSchedule = pathUpTrainBase + File.separator + "day" + trainDay;
            //             String pathOldDownTrainSchedule = pathDownTrainBase + File.separator + "day" + trainDay;
            //             String pathOldSSTrainSchedule = pathSSTrainBase + File.separator + "day" + trainDay;
            //             System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
            //                     " count "+count +" Type Full Day "+trainDay+" AvgSpeed "+avgSpeed +
            //                     " maxRatio "+ratio +" conditional ");
            //             scheduler.test(pathTemp, pathRoute, pathBestRouteTemp, pathOldUpTrainSchedule,
            //                     pathOldDownTrainSchedule, pathOldSSTrainSchedule, isSingleDay, trainDay,
            //                     usePreviousComputation, ratio, avgSpeed, pathLog, sourceTime);
            //             System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
            //                     " count "+count +" Type Break Day "+trainDay+" AvgSpeed "+avgSpeed +
            //                     " maxRatio "+ratio +" conditional ");
            //             scheduleByDivision.scheduleByBreaking(pathTemp, pathRoute, pathBestRouteTemp, pathOldUpTrainSchedule,
            //                     pathOldDownTrainSchedule,pathOldSSTrainSchedule,isSingleDay, trainDay, ratio,
            //                     pathLog, avgSpeed, sourceTime);
            //
            //             System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
            //                     " count "+count +" Type Full Day "+trainDay+" AvgSpeed "+avgSpeed +
            //                     " maxRatio "+ratio +" unconditional ");
            //             scheduler.test(pathTemp, pathRoute, pathBestRouteTemp, pathOldUpTrainSchedule,
            //                     pathOldDownTrainSchedule, pathOldSSTrainSchedule, isSingleDay, trainDay,
            //                     usePreviousComputation, ratio, avgSpeed, pathLog, null);
            //             System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
            //                     " count "+count +" Type Break Day "+trainDay+" AvgSpeed "+avgSpeed +
            //                     " maxRatio "+ratio +" unconditional ");
            //             scheduleByDivision.scheduleByBreaking(pathTemp, pathRoute, pathBestRouteTemp, pathOldUpTrainSchedule,
            //                     pathOldDownTrainSchedule,pathOldSSTrainSchedule,isSingleDay, trainDay, ratio,
            //                     pathLog, avgSpeed, null);
            //         }
            //         isSingleDay = false;
            //         sourceTime = new TrainTime(3,12,12);
            //         trainDay = 7;
            //         String pathOldUpTrainSchedule = pathUpTrainBase + "";
            //         String pathOldDownTrainSchedule = pathDownTrainBase + "";
            //         String pathOldSSTrainSchedule = pathSSTrainBase + "";
            //         count++;
            //         System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
            //                 " count "+count +" Type Full Day "+trainDay+" AvgSpeed "+avgSpeed +
            //                 " maxRatio "+ratio +" conditional ");
            //         scheduler.test(pathTemp, pathRoute, pathBestRouteTemp, pathOldUpTrainSchedule, pathOldDownTrainSchedule,
            //                 pathOldSSTrainSchedule, isSingleDay, trainDay, usePreviousComputation, ratio, avgSpeed, pathLog, sourceTime);
            //         System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
            //                 " count "+count +" Type Break Day "+trainDay+" AvgSpeed "+avgSpeed +
            //                 " maxRatio "+ratio +" conditional ");
            //         scheduleByDivision.scheduleByBreaking(pathTemp, pathRoute, pathBestRouteTemp, pathOldUpTrainSchedule,
            //                 pathOldDownTrainSchedule,pathOldSSTrainSchedule,isSingleDay, trainDay, ratio, pathLog, avgSpeed, sourceTime);
            //         System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
            //                 " count "+count +" Type Full Day "+trainDay+" AvgSpeed "+avgSpeed +
            //                 " maxRatio "+ratio +" unconditional ");
            //         scheduler.test(pathTemp, pathRoute, pathBestRouteTemp, pathOldUpTrainSchedule, pathOldDownTrainSchedule,
            //                 pathOldSSTrainSchedule, isSingleDay, trainDay, usePreviousComputation, ratio, avgSpeed, pathLog, null);
            //         System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
            //                 " count "+count +" Type Break Day "+trainDay+" AvgSpeed "+avgSpeed +
            //                 " maxRatio "+ratio +" unconditional ");
            //         scheduleByDivision.scheduleByBreaking(pathTemp, pathRoute, pathBestRouteTemp, pathOldUpTrainSchedule,
            //                 pathOldDownTrainSchedule,pathOldSSTrainSchedule,isSingleDay, trainDay, ratio, pathLog, avgSpeed, null);
            // }

            count = 20;
            for(avgSpeed=60;avgSpeed<130;avgSpeed+=20) {
                count++;
                isSingleDay = true;
                TrainTime sourceTime;
                String pathBestRouteTemp = pathBestRoute + File.separator + count;
                Scheduler.createFolder(pathBestRouteTemp);
                for (trainDay = 0; trainDay < 7; trainDay++) {
                    sourceTime  = new TrainTime(trainDay,12,12);
                    String pathOldUpTrainSchedule = pathUpTrainBase + File.separator + "day" + trainDay;
                    String pathOldDownTrainSchedule = pathDownTrainBase + File.separator + "day" + trainDay;
                    String pathOldSSTrainSchedule = pathSSTrainBase + File.separator + "day" + trainDay;
                    System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
                            " count "+count +" Type Full Day "+trainDay+" AvgSpeed "+avgSpeed +
                            " maxRatio "+ratio +" conditional ");
                    scheduler.test(pathTemp, pathRoute, pathBestRouteTemp, pathOldUpTrainSchedule,
                            pathOldDownTrainSchedule, pathOldSSTrainSchedule, isSingleDay, trainDay,
                            usePreviousComputation, ratio, avgSpeed, pathLog, sourceTime);

                    System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
                            " count "+count +" Type Full Day "+trainDay+" AvgSpeed "+avgSpeed +
                            " maxRatio "+ratio +" unconditional ");
                    scheduler.test(pathTemp, pathRoute, pathBestRouteTemp, pathOldUpTrainSchedule,
                            pathOldDownTrainSchedule, pathOldSSTrainSchedule, isSingleDay, trainDay,
                            usePreviousComputation, ratio, avgSpeed, pathLog, null);
                }
                isSingleDay = false;
                sourceTime = new TrainTime(3,12,12);
                trainDay = 7;
                String pathOldUpTrainSchedule = pathUpTrainBase + "";
                String pathOldDownTrainSchedule = pathDownTrainBase + "";
                String pathOldSSTrainSchedule = pathSSTrainBase + "";
                System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
                        " count "+count +" Type Full Day "+trainDay+" AvgSpeed "+avgSpeed +
                        " maxRatio "+ratio +" conditional ");
                scheduler.test(pathTemp, pathRoute, pathBestRouteTemp, pathOldUpTrainSchedule, pathOldDownTrainSchedule,
                        pathOldSSTrainSchedule, isSingleDay, trainDay, usePreviousComputation, ratio, avgSpeed, pathLog, sourceTime);
                System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+
                        " count "+count +" Type Full Day "+trainDay+" AvgSpeed "+avgSpeed +
                        " maxRatio "+ratio +" unconditional ");
                scheduler.test(pathTemp, pathRoute, pathBestRouteTemp, pathOldUpTrainSchedule, pathOldDownTrainSchedule,
                        pathOldSSTrainSchedule, isSingleDay, trainDay, usePreviousComputation, ratio, avgSpeed, pathLog, null);
            }


            // int newTrainNo = 9910;
            // String pathNewTrainFile = pathBestRoute+File.separator+"Type 2 AvgSpeed 80.0 path 1 cost 24.0 .path";
            // scheduler.showPlot(null,newTrainNo,pathPlotFile,pathRoute,pathOldUpTrainSchedule,
            //         true, isSingleDay, trainDay);


            // scheduler.fetchStationInfo(pathStationDatabase);
            // scheduler.fetchTrainInfo(pathTrainDatabase);
            // scheduler.getTrainList(pathRoute, pathUpTrainList, pathDownTrainList);
            // scheduler.putStationIntoDatabase(pathStationDatabase);
            // scheduler.putTrainIntoDatabase(pathTrainDatabase);
            // scheduler.putStoppagesIntoDatabase(pathTrainDatabase);
            //
            // scheduler.updateRouteFile(pathRoute, pathStationDatabase);
            // scheduler.createTrainList(pathRoute, pathUpTrainList,pathDownTrainList,pathSingleStoppageTrainList, pathTrainDatabase);
            // scheduler.fetchTrainSchedule(pathUpTrainList,pathTemp, pathUpTrainBase, pathTrainDatabase);
            // scheduler.fetchTrainSchedule(pathDownTrainList,pathTemp, pathDownTrainBase, pathTrainDatabase);
            // scheduler.fetchTrainSchedule(pathSingleStoppageTrainList,pathTemp, pathSSTrainBase, pathTrainDatabase);

            System.setOut(console1);
            System.setErr(console);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
