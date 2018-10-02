### Software Required:
    * Intelllij Idea
    * mysql-8.0.12
        * Install Mysql
        * create a database with name railwaydetails.
        * create a user account with name: testrailway & password: testrailway
        * You can refer https://dev.mysql.com/doc/refman/8.0/en/windows-install-archive.html to install mysql in windows

### Jars Required:

    * gson-2.8.2.jar
    * hamcrest-core-1.3.jar
    * iText-5.0.4.jar
    * jcommon-1.0.23.jar
    * jfreechart-1.0.19.jar
    * jfreechart-1.0.19-experimental.jar
    * jfreechart-1.0.19-swt.jar
    * jfreesvg-2.0.jar
    * jsoup-1.10.3.jar
    * junit-4.11.jar
    * mysql-connector-java-8.0.12.jar
    * orsoncharts-1.4-eval-nofx.jar
    * orsonpdf-1.6-eval.jar
    * pdfbox-app-2.0.8.jar
    * servlet.jar
    * swtgraphics2d.jar

    Add jars to libraries..

#### Download data resources:
    * databaseTrainAll (contains html & parsed info): https://drive.google.com/open?id=1TR51McvGFVY4thPpK66QSB3sL-a7ZLS0
    * databaseTrainParsed (contains parsed info): https://drive.google.com/open?id=1uqZptpJZiI7xe4cBzBg3jtUzgRufY_l4

    * databaseStationAll (contains html & parsed info): https://drive.google.com/open?id=1K7Mi-TF_CrLFd3fyiJoHnLLiWMcA5int
    * databaseStationParsed (contains parsed info): https://drive.google.com/open?id=1SviCcXp1Gkvy1gUfVGSquULmp-JDLoiY

    * Keep the downloaded data in following structure
        * pathDatabaseStation = data/temp/databaseStation
        * pathDatabaseTrain =  data/temp/databaseTrain

        for station:
        * <pathDatabaseStation>/station_details_*.html (required)
        * <pathDatabaseStation>/*.txt (required)

        for train:
        * <pathDatabaseTrain>/train_details_*.html (required)
        * <pathDatabaseTrain>/*.txt (required)

### Put route for new train:
    * download routePnbeBta.txt: https://drive.google.com/open?id=1uOXaI-pHXyAoWbeoT1M5RcjbORcnwnt7
    * download routePnbeDnr.txt: https://drive.google.com/open?id=11DnflXV_0SqFUOb4PLygLPo77S0erz47
    * download routePnbeMgs.txt: https://drive.google.com/open?id=15KnNU59YC13Xq2ACTNh0u3WYcX11t8KO
    * download routePnbeNdls.txt: https://drive.google.com/open?id=1qaCmm6T_ieYpkFUYGH5ZyTU-5V04njnf

    * pathRoute = "data/route";
    * <pathRoute>/routePnbeMgs.txt

### Seq of Execution:
    * DataHelper.fetchStationInfo(pathStationDatabase);
    * DataHelper.fetchTrainInfo(pathTrainDatabase);
    * DataHelper.putStationIntoDatabase(pathStationDatabase);
    * DataHelper.putTrainIntoDatabase(pathTrainDatabase);
    * DataHelper.putStoppagesIntoDatabase(pathTrainDatabase);
    * TrainHelper.updateTrainTypeFile(pathTrainTypeFile);

    * RouteHelper.updateRouteFile(pathTrainTypeFile, pathRoute, pathRouteTimeMin,pathRouteTimeAvg, pathRouteTimeMed, pathStationDatabase);
    * RouteHelper.initializeStopTimeFile(pathRouteStopTime,pathRoute);
    * TrainHelper.createTrainList(pathRoute, pathTrainList);
    * DataHelper.fetchTrainSchedule(pathTrainList,pathTemp, pathTrainBase, pathTrainDatabase);

    Now all the required data is downloaded. Do a test run.

