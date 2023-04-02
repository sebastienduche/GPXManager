package com.gpxmanager.strava;

import com.gpxmanager.MyGPXManager;
import com.gpxmanager.component.renderer.ButtonCellEditor;
import com.gpxmanager.component.renderer.ButtonCellRenderer;
import com.gpxmanager.component.renderer.DurationCellRenderer;
import com.gpxmanager.component.renderer.MeterPerSecondToKmHCellRenderer;
import com.gpxmanager.component.renderer.RoundDoubleCellRenderer;
import com.mytabbedpane.ITabListener;
import com.mytabbedpane.TabEvent;
import net.miginfocom.swing.MigLayout;
import org.jstrava.StravaConnection;
import org.jstrava.entities.Activity;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.List;

import static com.gpxmanager.Utils.getLabel;
import static com.gpxmanager.strava.StravaTableModel.COL_ALTITUDE;
import static com.gpxmanager.strava.StravaTableModel.COL_DISTANCE;
import static com.gpxmanager.strava.StravaTableModel.COL_DOWNLOAD;
import static com.gpxmanager.strava.StravaTableModel.COL_SPEED_AVG;
import static com.gpxmanager.strava.StravaTableModel.COL_SPEED_MAX;
import static com.gpxmanager.strava.StravaTableModel.COL_TIME;
import static com.gpxmanager.strava.StravaTableModel.COL_VIEW;

public class StravaPanel extends JPanel implements ITabListener {

    private final StravaConnection stravaConnection;
    private JTable table;

    public StravaPanel(StravaConnection stravaConnection, List<Activity> activities) {
        this.stravaConnection = stravaConnection;
        setLayout(new MigLayout("", "[grow]", "[grow]"));
        SwingUtilities.invokeLater(() -> {
            table = new JTable(new StravaTableModel(stravaConnection, activities));
            table.getColumnModel().getColumn(COL_TIME).setCellRenderer(new DurationCellRenderer());
            table.getColumnModel().getColumn(COL_DISTANCE).setCellRenderer(new RoundDoubleCellRenderer());
            table.getColumnModel().getColumn(COL_SPEED_MAX).setCellRenderer(new MeterPerSecondToKmHCellRenderer());
            table.getColumnModel().getColumn(COL_SPEED_AVG).setCellRenderer(new MeterPerSecondToKmHCellRenderer());
            table.getColumnModel().getColumn(COL_VIEW).setCellRenderer(new ButtonCellRenderer(getLabel("strava.view")));
            table.getColumnModel().getColumn(COL_DOWNLOAD).setCellRenderer(new ButtonCellRenderer(getLabel("strava.download")));
            table.getColumnModel().getColumn(COL_VIEW).setCellEditor(new ButtonCellEditor());
            table.getColumnModel().getColumn(COL_DOWNLOAD).setCellEditor(new ButtonCellEditor());
            DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
            leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
            table.getColumnModel().getColumn(COL_ALTITUDE).setCellRenderer(leftRenderer);
            table.setAutoCreateRowSorter(true);
            add(new JScrollPane(table), "grow");
        });

    }

    @Override
    public boolean tabWillClose(TabEvent tabEvent) {
        return true;
    }

    @Override
    public void tabClosed() {
        MyGPXManager.updateTabbedPane();
    }
}
