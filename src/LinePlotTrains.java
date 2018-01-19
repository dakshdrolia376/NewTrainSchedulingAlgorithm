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

@SuppressWarnings("unused")
class LinePlotTrains extends ApplicationFrame {

    private static final long serialVersionUID = 1L;

    LinePlotTrains(final String title, int windowHeight, int windowWidth, int newTrainNo, int heightPlotFile, int widthPlotFile, String pathPlotFile, String pathRoute, String pathOldTrains, String pathNewTrainFile) {
        super(title);
        ArrayList<Double> stationDistance = new ArrayList<>();
        HashMap<Double, String> tickLabels = new HashMap<>();
        ArrayList<Integer> trains;
        ArrayList<ArrayList<LocalTime[]>> schedule;

        FileReader fReader;
        BufferedReader bReader;
        try {
            fReader = new FileReader(pathRoute);
            bReader = new BufferedReader(fReader);
            String line;
            String data[];
            String st_id;
            Double st_dist;
            while ((line = bReader.readLine()) != null) {
                data = line.split("\\s+");
                st_id = data[0].trim().replaceAll(".*-", "");
                st_dist = roundDecimal(Double.parseDouble(data[1]));
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

            schedule = new ArrayList<>();
            trains = new ArrayList<>();
            if (!pathPlotFile.endsWith(".pdf")) {
                pathPlotFile += ".pdf";
            }
            trains.add(newTrainNo);

            schedule.add(0, new ArrayList<>());
            fReader = new FileReader(pathNewTrainFile);
            bReader = new BufferedReader(fReader);
            String data1[];
            while ((line = bReader.readLine()) != null) {
                if (line.equals("")) {
                    continue;
                }
                data = line.split("\\s+");
                LocalTime d[] = new LocalTime[2];
                data1 = data[1].split(":");
                d[0] = LocalTime.of(Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                data1 = data[2].split(":");
                d[1] = LocalTime.of(Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                schedule.get(0).add(d);
            }
            fReader.close();
            bReader.close();

            int oldTrainNo = 999;

            for (File file : listOfFiles) {

                if (file.isFile()) {
                    String filename = file.getName().split("\\.")[0];
                    try {
                        trains.add(Integer.parseInt(filename));
                    } catch (NumberFormatException e) {
                        trains.add(++oldTrainNo);
                    }
                    try {
                        schedule.add(0, new ArrayList<>());
                        fReader = new FileReader(file);
                        bReader = new BufferedReader(fReader);
                        while ((line = bReader.readLine()) != null) {
                            data = line.split("\\s+");
                            LocalTime d[] = new LocalTime[2];
                            data1 = data[1].split(":");
                            d[0] = LocalTime.of(Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                            data1 = data[2].split(":");
                            d[1] = LocalTime.of(Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                            schedule.get(0).add(d);
                        }
                        fReader.close();
                        bReader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("hi");
            final XYDataset dataset = createDataset(trains, schedule, stationDistance);
            final JFreeChart chart = createChart(dataset, tickLabels, pathPlotFile);
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

    private static void saveChartToPDF(JFreeChart chart, String fileName, int width, int height) throws Exception {
        if (chart == null) {
            System.out.println("Invalid Data to save as pdf.");
            return;
        }
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(fileName));

            //convert chart to PDF with iText:
            Document document = new Document(new Rectangle(width, height), 50, 50, 50, 50);
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

    private static Double roundDecimal(Double number) {
        Double temp = number % 1;
        if (temp < 0.25) {
            temp = 0.0;
        } else if (temp < 0.75) {
            temp = 0.5;
        } else {
            temp = 1.0;
        }
        return ((long) (number / 1) + temp);
    }

    private static Double getValueFromTime(int hrs, int minutes) {
        return (double) (hrs * 60 + minutes);
    }

    private static LocalTime getTimeFromValue(Double value) {
        int hrs = (int) (value / 1) / 60;
        int minutes = (int) (value / 1) % 60;
        try {
            return LocalTime.of(hrs, minutes);
        }
        catch (Exception e) {
            return LocalTime.of(0, 0);
        }
    }

    private XYDataset createDataset(ArrayList<Integer> trains, ArrayList<ArrayList<LocalTime[]>> schedule, ArrayList<Double> stationDistance) {
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
                    Double distanceNextDay = stationDistance.get(i) - stationDistance.get(i-1);
                    Double timeDiff1 = 24*60 - getValueFromTime(temp.getHour(), temp.getMinute());
                    Double timeDiff2 = getValueFromTime(temp2.getHour(), temp2.getMinute());

                    distanceNextDay = (distanceNextDay)* timeDiff1 / (timeDiff1 + timeDiff2);
                    distanceNextDay += stationDistance.get(i-1);
                    series1.add(distanceNextDay,getValueFromTime(23,59));
                    series1.add(distanceNextDay.doubleValue(), null);
                    series1.add(distanceNextDay.doubleValue(), getValueFromTime(0,0));
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

    private JFreeChart createChart(final XYDataset dataset, HashMap<Double, String> tickLabels, String fileName) {
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
                    String label = getTimeFromValue(numberTick.getValue()) + "";
                    myTicks.add(new NumberTick(TickType.MINOR, numberTick.getValue(), label,
                            numberTick.getTextAnchor(), numberTick.getRotationAnchor(),
                            (2 * Math.PI * 0) / 360.0f));
                }
                return myTicks;
            }
        };

        rangeAxis.setAutoRange(false);
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

        domainAxis.setAutoRange(false);
        domainAxis.setLowerBound(-2);
        domainAxis.setUpperBound(212);
        domainAxis.setAutoRangeIncludesZero(false);
        domainAxis.setAutoRangeStickyZero(false);
        domainAxis.setTickUnit(new NumberTickUnit(0.5));
        plot.setDomainAxis(domainAxis);

        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setDomainGridlinesVisible(true);
        return chart;
    }
}

