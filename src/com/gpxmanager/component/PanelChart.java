package com.gpxmanager.component;

import com.gpxmanager.gpx.beans.Track;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JPanel;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gpxmanager.Utils.getLabel;

public final class PanelChart extends JPanel {

    private static final long serialVersionUID = -6697139633950076186L;

    public PanelChart(Track track) {
        setLayout(new MigLayout("", "grow", "grow"));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        AtomicInteger i = new AtomicInteger();
        track.getTrackPoints().forEach(waypoint -> dataset.addValue(waypoint.getElevation(), getLabel("chart.elevation"), "test" + (i.getAndIncrement())));

        final JFreeChart chart = ChartFactory.createLineChart(getLabel("chart.elevation"),
                null, getLabel("chart.meter"),
                dataset, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel, "grow");
        updateUI();
    }
}
