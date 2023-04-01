package com.gpxmanager.strava;

import com.gpxmanager.MyGPXManager;
import com.mytabbedpane.ITabListener;
import com.mytabbedpane.TabEvent;
import net.miginfocom.swing.MigLayout;
import org.jstrava.entities.Activity;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.util.List;

public class StravaPanel extends JPanel implements ITabListener {

    private JTable table;

    public StravaPanel(List<Activity> activities) {
        setLayout(new MigLayout("", "[grow]", "[grow]"));
        SwingUtilities.invokeLater(() -> {
            table = new JTable(new StravaTableModel(activities));
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
