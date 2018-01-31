import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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
    private List<Integer> trains;
    private List<List<LocalTime[]>> schedule;
    private List<String> stationId;
    private int sizeSchedule;
    private List<Double> stationDistance;
    private Map<Double, String> tickLabels;

    public LinePlotTrains(final String title, int windowHeight, int windowWidth, int newTrainNo, int heightPlotFile,
                          int widthPlotFile, String pathPlotFile, String pathRoute, String pathOldTrains,
                          String pathNewTrainFile, boolean newTrainFolder) {
        super(title);
        stationDistance = new ArrayList<>();
        schedule = new ArrayList<>();
        sizeSchedule = 0;
        stationId = new ArrayList<>();
        trains = new ArrayList<>();
        tickLabels = new HashMap<>();

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
                st_id = data[0].trim().replaceAll(".*-", "");
                stationId.add(st_id);
                st_dist = Scheduler.roundDecimal(Double.parseDouble(data[1]));
                stationDistance.add(st_dist);
                tickLabels.put(st_dist, st_id);
            }
            fReader.close();
            bReader.close();

            File folder = new File(pathOldTrains);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles == null) {
                System.out.println("Unable to read route");
                return;
            }

            if (!pathPlotFile.endsWith(".pdf")) {
                pathPlotFile += ".pdf";
            }
            if(pathNewTrainFile!=null) {
                if(!newTrainFolder && !addTrain(newTrainNo, pathNewTrainFile)) {
                    System.out.println("Error in adding train " + pathNewTrainFile);
                }
                else{
                    File[] listOfFiles1 = new File(pathNewTrainFile).listFiles();
                    if(listOfFiles1==null) {
                        System.out.println("No new trains found");
                        return;
                    }

                    for (File file: listOfFiles1) {
                        if (file.isFile()) {
                            if(!addTrain(newTrainNo++, file.getPath())) {
                                System.out.println("Error in adding train " + pathNewTrainFile);
                            }
                        }
                    }
                }
            }
            int tempTrainNo;

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    String filename = file.getName().split("\\.")[0];
                    try {
                        tempTrainNo = Integer.parseInt(filename);
                    }
                    catch (NumberFormatException e) {
                        tempTrainNo = ++newTrainNo;
                    }
                    if(!addTrain(tempTrainNo, file.getPath())){
                        System.out.println("Error in adding train " + file.getPath());
                    }
                }
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

    private boolean addTrain(int trainNo, String fileName){
        try {
            FileReader fReader = new FileReader(fileName);
            BufferedReader bReader = new BufferedReader(fReader);
            this.trains.add(trainNo);
            this.schedule.add(new ArrayList<>(stationId.size()));
            String line;
            String data[];
            String data1[];
            LocalTime d[];
            Map<String, LocalTime[]> stationTimingsMap = new HashMap<>();
            while ((line = bReader.readLine()) != null) {
                d = new LocalTime[2];
                data = line.split("\\s+");
                data1 = data[1].split(":");
                d[0] = LocalTime.of(Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                data1 = data[2].split(":");
                d[1] = LocalTime.of(Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                stationTimingsMap.put(data[0].trim().replaceAll(".*-", ""),d);
            }
            d = new LocalTime[2];
            d[0] = LocalTime.of(23, 59);
            d[1] = LocalTime.of(23, 59);
            for(String stId: stationId){
                schedule.get(sizeSchedule).add(stationTimingsMap.getOrDefault(stId,d));
            }
            fReader.close();
            bReader.close();
            sizeSchedule++;
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

    public double getValueFromTime(int hrs, int minutes) {
        return (double) (hrs * 60 + minutes);
    }

    public LocalTime getTimeFromValue(double value) {
        int hrs = (int) (value / 1) / 60;
        int minutes = (int) (value / 1) % 60;
        try {
            return LocalTime.of(hrs, minutes);
        }
        catch (Exception e) {
            return LocalTime.of(0, 0);
        }
    }

    public XYDataset createDataset() {
        // create the dataset...
        final XYSeriesCollection dataset = new XYSeriesCollection();
        LocalTime temp=null, temp2, temp3;
        for (int j = 0; j < trains.size(); j++) {
            XYSeries series1 = new XYSeries(trains.get(j));
            for (int i = 0; i < schedule.get(j).size(); i++) {
                temp2 = schedule.get(j).get(i)[0];
                temp3 = schedule.get(j).get(i)[1];
                if(temp2== null || temp3== null){
                    System.out.println("Invalid schedule for train " + trains.get(j) + " .Skipping it");
                    break;
                }
                if(i > 0 && (temp.compareTo(temp2) > 0)){
                    double distanceNextDay = stationDistance.get(i) - stationDistance.get(i-1);
                    double timeDiff1 = 24*60 - getValueFromTime(temp.getHour(), temp.getMinute());
                    double timeDiff2 = getValueFromTime(temp2.getHour(), temp2.getMinute());

                    distanceNextDay = (distanceNextDay)* timeDiff1 / (timeDiff1 + timeDiff2);
                    distanceNextDay += stationDistance.get(i-1);
                    series1.add(distanceNextDay,getValueFromTime(23,59));
                    series1.add(distanceNextDay, null);
                    series1.add(distanceNextDay, getValueFromTime(0,0));
                }
                series1.add(stationDistance.get(i).doubleValue(), getValueFromTime(temp2.getHour(), temp2.getMinute()));

                if(temp3.compareTo(temp2) < 0){
                    series1.add(stationDistance.get(i).doubleValue(),getValueFromTime(23,59));
                    series1.add(stationDistance.get(i).doubleValue(), null);
                    series1.add(stationDistance.get(i).doubleValue(), getValueFromTime(0,0));
                    series1.add(stationDistance.get(i).doubleValue(), getValueFromTime(temp3.getHour(), temp3.getMinute()));
                }
                series1.add(stationDistance.get(i).doubleValue(), getValueFromTime(temp3.getHour(), temp3.getMinute()));
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
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        final XYPlot plot = (XYPlot) chart.getPlot();
        // customise the range axis...
        NumberAxis rangeAxis = new NumberAxis(plot.getRangeAxis().getLabel()) {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("rawtypes")
            @Override
            public List<NumberTick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
                List allTicks = super.refreshTicks(g2, state, dataArea, edge);
                List<NumberTick> myTicks = new ArrayList<>();
                for (Object tick : allTicks) {
                    NumberTick numberTick = (NumberTick) tick;

                    String label;
                    double numTickValue = numberTick.getValue();
                    if(numTickValue>=0 && numTickValue<24*60 ){
                        label = getTimeFromValue(numberTick.getValue()) + "";
                    }
                    else if(numTickValue<0){
                        label = "Prev day";
                    }
                    else{
                        label = "Next day";
                    }
                    myTicks.add(new NumberTick(TickType.MINOR, numberTick.getValue(), label,
                            numberTick.getTextAnchor(), numberTick.getRotationAnchor(),
                            (2 * Math.PI * 0) / 360.0f));
                }
                return myTicks;
            }
        };

        rangeAxis.setAutoRange(true);
        // rangeAxis.setLowerBound(0);
        // rangeAxis.setUpperBound(1439);
        rangeAxis.setLowerBound(0);
        rangeAxis.setUpperBound(1439);
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRangeStickyZero(false);
        rangeAxis.setTickUnit(new NumberTickUnit(30));
        plot.setRangeAxis(rangeAxis);

        NumberAxis domainAxis = new NumberAxis(plot.getDomainAxis().getLabel()) {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("rawtypes")
            @Override
            public List<NumberTick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
                List allTicks = super.refreshTicks(g2, state, dataArea, edge);
                List<NumberTick> myTicks = new ArrayList<>();
                for (Object tick : allTicks) {
                    NumberTick numberTick = (NumberTick) tick;
                    String label = "";
                    if (tickLabels.containsKey(numberTick.getValue())) {
                        label = tickLabels.get(numberTick.getValue());
                    }
                    myTicks.add(new NumberTick(TickType.MINOR, numberTick.getValue(), label,
                            numberTick.getTextAnchor(), numberTick.getRotationAnchor(),
                            (2 * Math.PI * 270) / 360.0f));
                }
                return myTicks;
            }
        };

        domainAxis.setAutoRange(true);
        // domainAxis.setLowerBound(-2);
        // domainAxis.setUpperBound(212);
        domainAxis.setAutoRangeIncludesZero(false);
        domainAxis.setAutoRangeStickyZero(false);
        domainAxis.setTickUnit(new NumberTickUnit(0.5));
        plot.setDomainAxis(domainAxis);

        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        return chart;
    }
}

