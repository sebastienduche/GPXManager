package com.gpxmanager.component.renderer;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

import static com.gpxmanager.Utils.meterPerSecondToKmH;

public class MeterPerSecondToKmHCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Double) {
            value = meterPerSecondToKmH((Double) value);
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
