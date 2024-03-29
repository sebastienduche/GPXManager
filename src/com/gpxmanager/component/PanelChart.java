package com.gpxmanager.component;

import com.gpxmanager.gpx.TrackRoute;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JPanel;
import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gpxmanager.Utils.getLabel;

public final class PanelChart extends JPanel {

    private static final long serialVersionUID = -6697139633950076186L;

    public PanelChart(TrackRoute track) {
        setLayout(new MigLayout("", "grow", "grow"));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        AtomicInteger i = new AtomicInteger();
        track.routePoints().forEach(waypoint -> dataset.addValue(waypoint.getElevation(), getLabel("chart.elevation"), "" + (i.getAndIncrement())));

        final JFreeChart chart = ChartFactory.createLineChart(getLabel("chart.elevation"),
                getLabel("km"), getLabel("chart.meter"),
                dataset, PlotOrientation.VERTICAL, false, true, false);
        ((CategoryPlot) chart.getPlot()).getRenderer().setSeriesPaint(0, Color.BLUE);
        ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel, "grow");
        updateUI();
    }
}
