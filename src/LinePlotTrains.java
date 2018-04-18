import java.awt.*;
import java.awt.geom.Area;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.text.TextUtilities;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class LinePlotTrains extends ApplicationFrame {

    private static final long serialVersionUID = 1L;
    private Map<String, List<XYSeries>> schedule;
    private Map<String, Color> scheduleColor;
    private List<Color> seriesColor;
    private Map<String, Double> reverseTickLabels;
    private Map<Double, String> tickLabels;
    private FetchStationDetails fetchStationDetails;
    private String pathName;

    public LinePlotTrains(final String title, int windowHeight, int windowWidth, int newTrainNo, String pathPlotFile,
                          String pathRoute, String pathOldTrains, String pathNewTrainFile, int newTrainStartDay,
                          String pathStationDatabase, String pathName) {
        super(title);
        this.schedule = new HashMap<>();
        this.scheduleColor = new HashMap<>();
        this.seriesColor = new ArrayList<>();
        this.tickLabels = new HashMap<>();
        this.reverseTickLabels = new HashMap<>();
        this.fetchStationDetails = new FetchStationDetails(pathStationDatabase);
        this.pathName = pathName;
        FileReader fReader;
        BufferedReader bReader;
        try {
            fReader = new FileReader(pathRoute);
            bReader = new BufferedReader(fReader);
            String line;
            String data[];
            String st_id;
            double st_dist;
            while ((line = bReader.readLine()) != null) {
                data = line.split("\\s+");
                st_id = Scheduler.getStationIdFromName(data[0]);
                st_dist = Math.round(Double.parseDouble(data[1]));
                this.tickLabels.put(st_dist, st_id);
                this.reverseTickLabels.put(st_id, st_dist);
            }
            bReader.close();
            fReader.close();

            if (pathNewTrainFile != null) {
                if (!addTrainFromFile(newTrainNo, pathNewTrainFile, newTrainStartDay)) {
                    System.out.println("Error in adding train " + pathNewTrainFile);
                }
            }

            if (!addTrainFromFolder(pathOldTrains)) {
                throw new RuntimeException("Unable to read old train schedule");
            }

            final XYDataset dataset = createDataset();
            final JFreeChart chart = createChart(dataset);
            final ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(windowWidth, windowHeight));
            if (!pathPlotFile.endsWith(".pdf")) {
                pathPlotFile += ".pdf";
            }
            setContentPane(chartPanel);
            try {
                 saveChartToPDF(chart, pathPlotFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean addTrainFromFolder(String pathOldTrainScheduleFolder) {
        return addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                "day0", 0) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day1", 1) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day2", 2) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day3", 3) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day4", 4) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day5", 5) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day6", 6);
    }

    private boolean addTrainFromFolderSingleDay(String pathOldTrainScheduleFolder, int trainDay) {
        File[] listOfFiles = new File(pathOldTrainScheduleFolder).listFiles();
        if (listOfFiles == null) {
            System.out.println("No old trains found");
            return true;
        }

        for (File file : listOfFiles) {
            if (file.isFile()) {
                int trainNo;
                try {
                    trainNo = Integer.parseInt(file.getName().split("\\.")[0]);
                } catch (Exception e) {
                    System.out.println("File name should be train Number.");
                    System.out.println("Skipping file : " + file.getPath());
                    e.printStackTrace();
                    continue;
                }
                if (!addTrainFromFile(trainNo, file.getPath(), trainDay)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean addTrainFromFile(int trainNo, String filePath, int trainDay) {
        int stoppageDay = trainDay;
        try {
            String mapKey = trainNo+"";
            FileReader fReader = new FileReader(filePath);
            BufferedReader bReader = new BufferedReader(fReader);
            this.schedule.putIfAbsent(mapKey, new ArrayList<>());
            String line;
            TrainTime arrival, departure = null;
            String data[];
            String data1[];
            int countPart = this.schedule.get(mapKey).size();
            XYSeries stationTimingsSeries = new XYSeries(mapKey + "-" + (++countPart));
            Set<Double> stationAlreadySeen = new HashSet<>();
            double prevDist = -1;
            double lastDist = -1;
            boolean upOrDown;
            int station_count = 0;
            boolean atLeastOneStationInRoute = false;
            while ((line = bReader.readLine()) != null) {
                boolean addNullInLast = false;
                data = line.split("\\s+");
                String st_id = Scheduler.getStationIdFromName(data[0]);
                int numOfPlatform = fetchStationDetails.getNumberOfPlatform(st_id);
                if(numOfPlatform<=0){
                    continue;
                }
                double st_dist = this.reverseTickLabels.getOrDefault(st_id, -1.0);
                if (st_dist == -1.0) {
                    if(prevDist>=0) {
                        stationTimingsSeries.add(prevDist, null);
                    }
                    prevDist = -2;
                    continue;
                }
                else if (st_dist==prevDist){
                    continue;
                }

                atLeastOneStationInRoute = true;
                upOrDown = (st_dist >= prevDist);
                if (station_count == 1 && !upOrDown && prevDist>=0) {
                    XYDataItem tt;
                    List<Double> prev_tt = new ArrayList<>();
                    try {
                        tt = stationTimingsSeries.remove(prevDist);
                        while (tt != null) {
                            prev_tt.add(tt.getYValue());
                            prevDist -= 0.000001;
                            tt = stationTimingsSeries.remove(prevDist);
                        }
                    } catch (Exception e) {
                        prevDist += 0.000001;
                    }
                    for (double tt1 : prev_tt) {
                        stationTimingsSeries.add(prevDist, tt1);
                        prevDist += 0.000001;
                    }
                }
                station_count++;
                if (!stationAlreadySeen.add(st_dist)) {
                    // System.out.println(trainDay + " " + trainNo + " " + line);
                    this.schedule.get(mapKey).add(stationTimingsSeries);
                    stationTimingsSeries = new XYSeries(mapKey + "-" + (++countPart));
                    stationAlreadySeen = new HashSet<>();
                    if(prevDist>=0) {
                        stationTimingsSeries.add(prevDist, departure.getValue());
                        stationAlreadySeen.add(prevDist);
                    }
                    stationAlreadySeen.add(st_dist);
                }
                double temp_dist = st_dist;
                if(prevDist==-2 && lastDist>=0){
                    if(st_dist>=lastDist){
                        stationTimingsSeries.add(temp_dist, null);
                    }
                    else{
                        addNullInLast=true;
                    }
                }
                data1 = data[1].split(":");
                arrival = new TrainTime(stoppageDay, Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if (departure != null && arrival.compareTo(departure) < 0) {
                    arrival.addDay(1);
                    stoppageDay = arrival.day;
                    if (arrival.day == 0) {
                        if(prevDist>=0) {
                            double distanceNextDay = st_dist - prevDist;
                            double timeDiff1 = 10080 - departure.getValue();
                            double timeDiff2 = arrival.getValue();
                            distanceNextDay = (distanceNextDay) * timeDiff1 / (timeDiff1 + timeDiff2);
                            distanceNextDay += prevDist;
                            boolean stationReachedAtMidnight = distanceNextDay==st_dist;
                            stationTimingsSeries.add(distanceNextDay, new TrainTime(6, 23, 59).getValue());
                            if (upOrDown) {
                                distanceNextDay += 0.000001;
                            }
                            else {
                                distanceNextDay -= 0.000001;
                            }
                            stationTimingsSeries.add(distanceNextDay, null);
                            if (upOrDown) {
                                distanceNextDay += 0.000001;
                            }
                            else {
                                distanceNextDay -= 0.000001;
                            }
                            stationTimingsSeries.add(distanceNextDay, new TrainTime(0, 0, 0).getValue());
                            if (stationReachedAtMidnight) {
                                temp_dist = distanceNextDay;
                            }
                        }
                    }
                }
                if(arrival.compareTo(new TrainTime(0,0,0))==0){
                    System.out.println(trainNo+" arrival "+ line);
                }

                stationTimingsSeries.add(temp_dist, arrival.getValue());

                if (upOrDown) {
                    temp_dist += 0.000001;
                } else {
                    temp_dist -= 0.000001;
                }

                data1 = data[2].split(":");
                departure = new TrainTime(stoppageDay, Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if (departure.compareTo(arrival) < 0) {
                    departure.addDay(1);
                    stoppageDay = departure.day;
                    if (departure.day == 0) {
                        stationTimingsSeries.add(temp_dist, new TrainTime(6, 23, 59).getValue());
                        if (upOrDown) {
                            temp_dist += 0.000001;
                        } else {
                            temp_dist -= 0.000001;
                        }
                        stationTimingsSeries.add(temp_dist, null);
                        if (upOrDown) {
                            temp_dist += 0.000001;
                        } else {
                            temp_dist -= 0.000001;
                        }
                        stationTimingsSeries.add(temp_dist, new TrainTime(0, 0, 0).getValue());
                        if (upOrDown) {
                            temp_dist += 0.000001;
                        } else {
                            temp_dist -= 0.000001;
                        }
                    }
                }

                stationTimingsSeries.add(temp_dist, departure.getValue());
                if(addNullInLast){
                    stationTimingsSeries.add(temp_dist, null);
                }
                prevDist = temp_dist;
                lastDist = temp_dist;
            }
            this.schedule.get(mapKey).add(stationTimingsSeries);
            if(!atLeastOneStationInRoute){
                this.schedule.remove(mapKey);
            }
            bReader.close();
            fReader.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public XYDataset createDataset() {
        // create the dataset...
        final XYSeriesCollection dataset = new XYSeriesCollection();
        List<String> keyTrains = new ArrayList<>(this.schedule.keySet());
        int countDiffTrains = keyTrains.size();
        List<Color> keyColors = new RandomColors().getRandomColors(countDiffTrains);
        for (int i=0;i<countDiffTrains;i++) {
            this.scheduleColor.put(keyTrains.get(i),keyColors.get(i));
            for (XYSeries series1 : this.schedule.get(keyTrains.get(i))) {
                dataset.addSeries(series1);
                this.seriesColor.add(keyColors.get(i));
            }
        }
        return dataset;
    }

    public JFreeChart createChart(final XYDataset dataset) {
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
                "Train-tracking " + this.pathName,      // chart title
                "Station",                      // x axis label
                "Time",                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        final XYPlot plot = (XYPlot) chart.getPlot();
        if(plot.getSeriesCount()!=this.seriesColor.size()){
            System.out.println("Some error in dataset");
            return null;
        }
        XYItemRenderer dd = plot.getRenderer();
        // System.out.println(plot.getSeriesCount());

        for(int i=0;i<plot.getSeriesCount();i++){
            dd.setSeriesPaint(i,this.seriesColor.get(i));
            // System.out.println(dd.getLegendItem(0,i).getLabel()+ " "+ dd.getLegendItem(0,i).getLinePaint());
        }
        plot.setRenderer(dd);
        chart.getLegend().setVisible(false);

        // customise the range axis...
        NumberAxis rangeAxis = new NumberAxis(plot.getRangeAxis().getLabel()) {
            private static final long serialVersionUID = 1L;
            Area tickLabelArea = new Area();

            @SuppressWarnings("rawtypes")
            @Override
            public List<NumberTick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
                List allTicks = super.refreshTicks(g2, state, dataArea, edge);
                List<NumberTick> myTicks = new ArrayList<>();
                TrainTime trainTime;
                tickLabelArea = new Area();
                for (Object tick : allTicks) {
                    NumberTick numberTick = (NumberTick) tick;
                    String label;
                    double numTickValue = numberTick.getValue();
                    if (numTickValue >= 0 && numTickValue < 10080) {
                        trainTime = new TrainTime(0, 0, 0);
                        trainTime.addMinutes((int) Math.ceil(numberTick.getValue()));
                        label = trainTime.getFullString();
                    } else if (numTickValue < 0) {
                        label = "Prev week";
                    } else {
                        label = "Next week";
                    }

                    NumberTick numberTickTemp = new NumberTick(TickType.MINOR, numberTick.getValue(), label,
                            numberTick.getTextAnchor(), numberTick.getRotationAnchor(),
                            (2 * Math.PI * 0) / 360.0f);

                    Rectangle2D labelBounds = getTickBounds(numberTickTemp, g2);
                    double java2dValue = valueToJava2D(numberTick.getValue(), g2.getClipBounds(), edge);
                    labelBounds.setRect(labelBounds.getX(), java2dValue, labelBounds.getWidth(), labelBounds.getHeight());
                    if (!tickLabelIsOverlapping(tickLabelArea, labelBounds)) {
                        myTicks.add(numberTickTemp);
                        tickLabelArea.add(new Area(labelBounds));
                    }
                }
                return myTicks;
            }

            private boolean tickLabelIsOverlapping(Area area, Rectangle2D rectangle) {
                return area.intersects(rectangle);
            }

            private Rectangle2D getTickBounds(NumberTick numberTick, Graphics2D g2) {
                FontMetrics fm = g2.getFontMetrics(getTickLabelFont());
                return TextUtilities.getTextBounds(numberTick.getText(), g2, fm);
            }
        };

        rangeAxis.setAutoRange(true);
        // rangeAxis.setLowerBound(0);
        // rangeAxis.setUpperBound(1439);
        // rangeAxis.setLowerBound(((this.requiredDay == 7) ? 0 : requiredDay * 1440));
        // rangeAxis.setUpperBound(((this.requiredDay == 7) ? 10079 : (requiredDay + 1) * 1440));

        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRangeStickyZero(false);
        // rangeAxis.setTickUnit(new NumberTickUnit(10));
        plot.setRangeAxis(rangeAxis);

        NumberAxis domainAxis = new NumberAxis(plot.getDomainAxis().getLabel()) {
            private static final long serialVersionUID = 1L;

            Area tickLabelArea = new Area();

            @SuppressWarnings("rawtypes")
            @Override
            public List<NumberTick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
                List allTicks = super.refreshTicks(g2, state, dataArea, edge);
                List<NumberTick> myTicks = new ArrayList<>();
                tickLabelArea = new Area();
                for (Object tick : allTicks) {
                    NumberTick numberTick = (NumberTick) tick;
                    String label = "";
                    if (tickLabels.containsKey(numberTick.getValue())) {
                        label = tickLabels.get(numberTick.getValue());
                    }
                    NumberTick numberTickTemp = new NumberTick(TickType.MINOR, numberTick.getValue(), label,
                            numberTick.getTextAnchor(), numberTick.getRotationAnchor(),
                            (2 * Math.PI * 270) / 360.0f);

                    Rectangle2D labelBounds = getTickBounds(numberTickTemp, g2);
                    double java2dValue = valueToJava2D(numberTick.getValue(), g2.getClipBounds(), edge);
                    labelBounds.setRect(labelBounds.getX(), java2dValue, labelBounds.getWidth(), labelBounds.getHeight());
                    if (!tickLabelIsOverlapping(tickLabelArea, labelBounds)) {
                        myTicks.add(numberTickTemp);
                        tickLabelArea.add(new Area(labelBounds));
                    }
                }
                return myTicks;
            }

            private boolean tickLabelIsOverlapping(Area area, Rectangle2D rectangle) {
                return area.intersects(rectangle);
            }

            private Rectangle2D getTickBounds(NumberTick numberTick, Graphics2D g2) {
                FontMetrics fm = g2.getFontMetrics(getTickLabelFont());
                return TextUtilities.getTextBounds(numberTick.getText(), g2, fm);
            }
        };

        domainAxis.setAutoRange(true);
        // domainAxis.setLowerBound(-2);
        // domainAxis.setUpperBound(212);
        domainAxis.setAutoRangeIncludesZero(false);
        domainAxis.setAutoRangeStickyZero(false);
        // domainAxis.setTickUnit(new NumberTickUnit(0.5));
        plot.setDomainAxis(domainAxis);

        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        return chart;
    }

    public void saveChartToPDF(JFreeChart chart, String fileName) throws Exception {
        if (chart == null) {
            System.out.println("Invalid Data to save as pdf.");
            return;
        }
        BufferedOutputStream out = null;
        List<String> keyTrain = new ArrayList<>(this.scheduleColor.keySet());
        int cols =5;
        int widthPdf = 1024;
        int heightPdf = 1500;
        List<String> titles = Arrays.asList("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday");
        try {
            ValueAxis rangeAxis = ((XYPlot) chart.getPlot()).getRangeAxis();
            double initialLowerBound = rangeAxis.getLowerBound();
            double initialUpperBound = rangeAxis.getUpperBound();
            String initialTitle = chart.getTitle().getText();
            out = new BufferedOutputStream(new FileOutputStream(fileName));
            //convert chart to PDF with iText:
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.addAuthor("Naman");
            document.open();
            Rectangle one = new Rectangle(widthPdf,heightPdf);
            for(int i=0;i<10080;i+=1440){
                chart.setTitle(initialTitle+" Day: "+ titles.get(i/1440));
                rangeAxis.setLowerBound(i);
                rangeAxis.setUpperBound(i+heightPdf);
                document.setPageSize(one);
                document.setMargins(10, 10, 10, 10);
                document.newPage();
                PdfContentByte cb = writer.getDirectContent();
                PdfTemplate tp = cb.createTemplate(widthPdf, heightPdf);
                Graphics2D g2 = tp.createGraphics(widthPdf, heightPdf, new DefaultFontMapper());
                Rectangle2D r2D = new Rectangle2D.Double(0, 0, widthPdf, heightPdf);
                chart.draw(g2, r2D);
                g2.dispose();
                cb.addTemplate(tp, 0, 0);
            }

            int countLegendItem =0;
            int pageNumLegend =1;
            while(true) {
                document.setPageSize(one);
                document.setMargins(2, 2, 2, 2);
                document.newPage();
                PdfContentByte cb = writer.getDirectContent();
                PdfTemplate tp = cb.createTemplate(widthPdf, heightPdf);
                Graphics2D g2 = tp.createGraphics(widthPdf, heightPdf, new DefaultFontMapper());
                tp.setWidth(widthPdf);
                tp.setHeight(heightPdf);
                g2.setBackground(Color.white);
                g2.setPaint(Color.white);
                g2.fillRect(0, 0, widthPdf, heightPdf);
                g2.setPaint(Color.black);
                g2.setFont(new Font("TimesRoman",Font.BOLD,20));
                g2.drawString("Legend Page "+ pageNumLegend++, 450, 50);
                g2.setFont(new Font("TimesRoman",Font.PLAIN,14));
                int row_no =0;
                for (; countLegendItem < keyTrain.size(); countLegendItem++) {
                    if(countLegendItem%cols==0){
                        row_no++;
                    }
                    int xColor = (countLegendItem % cols) * 180 + 50;
                    int xText = xColor + 50;
                    int yColor = (row_no-1) * 50 + 100;
                    if(yColor>=heightPdf){
                        break;
                    }
                    int yText = yColor + 10;
                    g2.setPaint(this.scheduleColor.get(keyTrain.get(countLegendItem)));
                    g2.fillRect(xColor, yColor, 40, 10);
                    g2.setPaint(Color.black);
                    g2.drawString(keyTrain.get(countLegendItem), xText, yText);
                }
                g2.dispose();
                cb.addTemplate(tp, 0, 0);
                if(countLegendItem>=keyTrain.size()){
                    break;
                }
            }
            document.close();
            chart.setTitle(initialTitle);
            rangeAxis.setLowerBound(initialLowerBound);
            rangeAxis.setUpperBound(initialUpperBound);
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }
}