package com.gpxmanager.strava;

import com.gpxmanager.strava.statistics.StatXYData;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.JPanel;
import java.util.List;
import java.util.Map;

import static com.gpxmanager.Utils.getLabel;
import static java.util.stream.Collectors.groupingBy;

public class StravaChartPanel extends JPanel {

  public StravaChartPanel() {
    setLayout(new MigLayout("", "grow", "grow"));
  }

  public void setDataBarChart(List<StatData> datas, String title) {
    removeAll();
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    datas.stream()
        .filter(statData -> statData.count() > 0)
        .forEach(statData -> dataset.addValue(statData.count(), statData.name(), statData.serie()));
    JFreeChart chart = ChartFactory.createBarChart(title,          // chart title
        "", getLabel("strava.table.distance"),
        dataset,                // data
        PlotOrientation.VERTICAL,
        true,                   // include legend
        true,
        true);

    CategoryPlot plot = (CategoryPlot) chart.getPlot();
    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setTickUnit(new NumberTickUnit(100));
    ChartPanel chartPanel = new ChartPanel(chart);
    add(chartPanel, "grow");
    updateUI();
  }

  public void setLineChart(List<StatData> datas, String title) {
    removeAll();

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    datas.forEach(statData -> dataset.addValue(statData.count(), title, statData.name()));

    final JFreeChart chart = ChartFactory.createLineChart(title,
        null, getLabel("strava.table.distance"),
        dataset, PlotOrientation.VERTICAL, true, true, false);
    ChartPanel chartPanel = new ChartPanel(chart);
    add(chartPanel, "grow");
    updateUI();
  }

  public void setXYLineChart(List<StatXYData> datas, String title) {
    removeAll();
    XYSeriesCollection dataset = new XYSeriesCollection();
    Map<String, List<StatXYData>> mapPerSerie = datas.stream()
        .collect(groupingBy(StatXYData::serie));
    for (String serie : mapPerSerie.keySet()) {
      XYSeries xySeries = new XYSeries(serie);
      dataset.addSeries(xySeries);
      mapPerSerie.get(serie)
          .forEach(statData -> xySeries.add(new XYDataItem(statData.x(), statData.y())));
    }

    JFreeChart chart = ChartFactory.createXYLineChart(title,
        "", "", dataset);
    ChartPanel chartPanel = new ChartPanel(chart);
    add(chartPanel, "grow");
    updateUI();
  }
}
