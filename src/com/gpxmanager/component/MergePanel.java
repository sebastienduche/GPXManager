package com.gpxmanager.component;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class MergePanel extends JPanel {

    JTable table;

    public MergePanel() {
        setLayout(new MigLayout("", "grow", "grow"));
        table = new JTable();
        add(new JScrollPane(table));
    }
}
