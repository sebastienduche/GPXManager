package com.gpxmanager.strava;

import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JPanel;
import java.util.List;

import static com.gpxmanager.Utils.getLabel;

public class StravaChartPanel extends JPanel {

  public StravaChartPanel() {
    setLayout(new MigLayout("", "grow", "grow"));
  }
  public void setDataBarChart(List<StatData> datas, String title) {
    removeAll();
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    datas.stream()
        .filter(statData -> statData.getCount() > 0)
        .forEach(statData -> dataset.addValue(statData.getCount(), statData.getName(), statData.getSerie()));
    JFreeChart chart = ChartFactory.createBarChart(title,          // chart title
        "", getLabel("strava.table.distance"),
        dataset,                // data
        PlotOrientation.VERTICAL,
        true,                   // include legend
        true,
        true);

    ChartPanel chartPanel = new ChartPanel(chart);
    add(chartPanel, "grow");
    updateUI();
  }
}
