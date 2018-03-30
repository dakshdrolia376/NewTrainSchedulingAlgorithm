import java.awt.*;
import java.awt.geom.Area;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
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

class LinePlotTrains extends ApplicationFrame {

    private static final long serialVersionUID = 1L;
    private List<String> trains;
    private List<List<TrainTime[]>> schedule;
    private List<String> stationId;
    private int sizeSchedule;
    private List<Double> stationDistance;
    private Map<Double, String> tickLabels;

    public LinePlotTrains(final String title, int windowHeight, int windowWidth, int newTrainNo, int heightPlotFile,
                          int widthPlotFile, String pathPlotFile, String pathRoute, String pathOldTrains,
                          String pathNewTrainFile, int newTrainStartDay) {
        super(title);
        this.stationDistance = new ArrayList<>();
        this.schedule = new ArrayList<>();
        this.sizeSchedule = 0;
        this.stationId = new ArrayList<>();
        this.trains = new ArrayList<>();
        this.tickLabels = new HashMap<>();
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
                this.stationId.add(st_id);
                st_dist = Math.round(Double.parseDouble(data[1]));
                this.stationDistance.add(st_dist);
                this.tickLabels.put(st_dist, st_id);
            }
            bReader.close();
            fReader.close();

            if (!pathPlotFile.endsWith(".pdf")) {
                pathPlotFile += ".pdf";
            }

            if(pathNewTrainFile!=null) {
                if(!addTrainFromFile(newTrainNo, pathNewTrainFile,newTrainStartDay)) {
                    System.out.println("Error in adding train " + pathNewTrainFile);
                }
            }

            if(!addTrainFromFolder(pathOldTrains)){
                throw new RuntimeException("Unable to read old train schedule");
            }

            final XYDataset dataset = createDataset();
            final JFreeChart chart = createChart(dataset, pathPlotFile);
            final ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(windowWidth, windowHeight));
            setContentPane(chartPanel);
            try {
                saveChartToPDF(chart, pathPlotFile, widthPlotFile, heightPlotFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean addTrainFromFolder(String pathOldTrainScheduleFolder){
        return addTrainFromFolderSingleDay(pathOldTrainScheduleFolder+ File.separator +
                        "day0", 0) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder+ File.separator +
                        "day1", 1) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder+ File.separator +
                        "day2", 2) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder+ File.separator +
                        "day3", 3) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder+ File.separator +
                        "day4", 4) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder+ File.separator +
                        "day5", 5) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder+ File.separator +
                        "day6", 6);
    }

    private boolean addTrainFromFolderSingleDay(String pathOldTrainScheduleFolder, int trainDay){
        File[] listOfFiles = new File(pathOldTrainScheduleFolder).listFiles();
        if(listOfFiles==null) {
            System.out.println("No old trains found");
            return true;
        }

        for (File file: listOfFiles) {
            if (file.isFile()) {
                int trainNo;
                try {
                    trainNo = Integer.parseInt(file.getName().split("\\.")[0]);
                }
                catch (Exception e) {
                    System.out.println("File name should be train Number.");
                    System.out.println("Skipping file : " + file.getPath());
                    e.printStackTrace();
                    continue;
                }
                if(!addTrainFromFile(trainNo,file.getPath(), trainDay)){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean addTrainFromFile(int trainNo, String filePath, int trainDay){
        int stoppageDay = trainDay;
        try {
            FileReader fReader = new FileReader(filePath);
            BufferedReader bReader = new BufferedReader(fReader);
            this.trains.add(trainDay+":"+trainNo);
            this.schedule.add(new ArrayList<>(this.stationId.size()));
            String line;
            TrainTime arrival, departure=null;
            String data[];
            String data1[];
            TrainTime d[];
            Map<String, TrainTime[]> stationTimingsMap = new HashMap<>();
            Map<String, Integer> stationStoppageNo = new HashMap<>();
            int count=0;
            while ((line = bReader.readLine()) != null) {
                d = new TrainTime[2];
                data = line.split("\\s+");
                data1 = data[1].split(":");
                arrival = new TrainTime(stoppageDay, Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure!=null && arrival.compareTo(departure)<0){
                    arrival.addDay(1);
                    stoppageDay = arrival.day;
                }
                data1 = data[2].split(":");
                departure = new TrainTime(stoppageDay, Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if(departure.compareTo(arrival)<0){
                    departure.addDay(1);
                    stoppageDay = departure.day;
                }
                d[0] = arrival;
                d[1] = departure;
                stationTimingsMap.put(Scheduler.getStationIdFromName(data[0]),d);
                stationStoppageNo.put(Scheduler.getStationIdFromName(data[0]),++count);
            }
            boolean atLeastSingleStationInRoute = false;
            boolean sameDirectionTrain = false;
            for(int j=0;j<this.stationId.size();j++){
                String stId= this.stationId.get(j);
                if(j>0){
                    if(stationStoppageNo.getOrDefault(stId,0)==(stationStoppageNo.getOrDefault(this.stationId.get(j-1),0)+1)){
                        sameDirectionTrain = true;
                    }
                }
                TrainTime[] temp = stationTimingsMap.getOrDefault(stId, null);
                this.schedule.get(this.sizeSchedule).add(temp);
                if(temp!=null){
                    atLeastSingleStationInRoute = true;
                }
            }

            bReader.close();
            fReader.close();
            if(atLeastSingleStationInRoute && sameDirectionTrain) {
                this.sizeSchedule++;
                System.out.println("Adding "+ trainDay+":"+trainNo);
            }
            else{
                this.schedule.remove(this.sizeSchedule);
                this.trains.remove(trainDay+":"+trainNo);
                System.out.println("Removing "+ trainDay+":"+trainNo);
            }
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void saveChartToPDF(JFreeChart chart, String fileName, int width, int height) throws Exception {
        if (chart == null) {
            System.out.println("Invalid Data to save as pdf.");
            return;
        }
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(fileName));

            //convert chart to PDF with iText:
            Document document = new Document(new Rectangle(width, height),
                    50, 50, 50, 50);
            try {
                PdfWriter writer = PdfWriter.getInstance(document, out);
                document.addAuthor("Naman");
                document.open();
                PdfContentByte cb = writer.getDirectContent();
                PdfTemplate tp = cb.createTemplate(width, height);
                Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());

                Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
                chart.draw(g2, r2D);
                g2.dispose();
                cb.addTemplate(tp, 0, 0);
            }
            finally {
                document.close();
            }
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public XYDataset createDataset() {
        // create the dataset...
        final XYSeriesCollection dataset = new XYSeriesCollection();
        TrainTime temp, temp2, temp3;
        TrainTime d[];
        for (int j = 0; j < this.trains.size(); j++) {
            XYSeries series1 = new XYSeries(this.trains.get(j));
            temp=null;
            for (int i = 0; i < this.schedule.get(j).size(); i++) {
                d = this.schedule.get(j).get(i);
                if(d==null){
                    //train's route not passes by this station..
                    series1.add(this.stationDistance.get(i).doubleValue(), null);
                    temp = null;
                    continue;
                }
                temp2 = this.schedule.get(j).get(i)[0];
                temp3 = this.schedule.get(j).get(i)[1];

                if (temp2 == null) {
                    series1.add(this.stationDistance.get(i).doubleValue(), null);
                    System.out.println("Invalid schedule for train " + this.trains.get(j) +
                            " at station " + this.stationId.get(i));
                }
                else {

                    if (temp != null && (temp.compareTo(temp2) > 0)) {
                        double distanceNextDay = this.stationDistance.get(i) - this.stationDistance.get(i - 1);
                        double timeDiff1 = 10080 - temp.getValue();
                        double timeDiff2 = temp2.getValue();

                        distanceNextDay = (distanceNextDay) * timeDiff1 / (timeDiff1 + timeDiff2);
                        distanceNextDay += this.stationDistance.get(i - 1);
                        series1.add(distanceNextDay, new TrainTime(6, 23, 59).getValue());
                        series1.add(distanceNextDay, null);
                        series1.add(distanceNextDay, new TrainTime(0, 0, 0).getValue());
                    }
                    series1.add(this.stationDistance.get(i).doubleValue(), temp2.getValue());
                }

                if(temp3 != null) {
                    if (temp2!=null && temp3.compareTo(temp2) < 0) {
                        series1.add(this.stationDistance.get(i).doubleValue(),
                                new TrainTime(6, 23, 59).getValue());
                        series1.add(this.stationDistance.get(i).doubleValue(), null);
                        series1.add(this.stationDistance.get(i).doubleValue(),
                                new TrainTime(0, 0, 0).getValue());
                    }
                    series1.add(this.stationDistance.get(i).doubleValue(), temp3.getValue());
                }
                temp = temp3;
            }
            dataset.addSeries(series1);
        }
        return dataset;
    }

    public JFreeChart createChart(final XYDataset dataset, String fileName) {
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
                "Train-tracking " + fileName,      // chart title
                "Station",                      // x axis label
                "Time",                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                false,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        final XYPlot plot = (XYPlot) chart.getPlot();
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
                    if(numTickValue>=0 && numTickValue<10080){
                        trainTime = new TrainTime(0,0,0);
                        trainTime.addMinutes((int)Math.ceil(numberTick.getValue()));
                        label = trainTime.getFullString();
                    }
                    else if(numTickValue<0){
                        label = "Prev week";
                    }
                    else{
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
        rangeAxis.setLowerBound(0);
        rangeAxis.setUpperBound(10079);

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
}

