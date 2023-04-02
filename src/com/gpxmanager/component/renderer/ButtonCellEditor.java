package com.gpxmanager.component.renderer;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.util.EventObject;


/**
 * <p>Titre : Cave &agrave; vin
 * <p>Description : Votre description
 * <p>Copyright : Copyright (c) 1998
 * <p>Soci&eacute;t&eacute; : Seb Informatique
 *
 * @author S&eacute;bastien Duch&eacute;
 * @version 0.4
 * @since 27/05/21
 */

public final class ButtonCellEditor extends JButton implements TableCellEditor {

    private static final long serialVersionUID = 3070998772944465050L;

    public ButtonCellEditor() {
        super();
        addActionListener((e) -> fireEditingStopped());

    }

    @Override
    public void addCellEditorListener(CellEditorListener listener) {
        listenerList.add(CellEditorListener.class, listener);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener listener) {
        listenerList.remove(CellEditorListener.class, listener);
    }

    private void fireEditingStopped() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i].equals(CellEditorListener.class)) {
                CellEditorListener listener = (CellEditorListener) listeners[i + 1];
                listener.editingStopped(changeEvent);
            }
        }
    }

    private void fireEditingCanceled() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i].equals(CellEditorListener.class)) {
                CellEditorListener listener = (CellEditorListener) listeners[i + 1];
                listener.editingCanceled(changeEvent);
            }
        }
    }

    @Override
    public void cancelCellEditing() {
        fireEditingCanceled();
    }

    @Override
    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }

    @Override
    public boolean isCellEditable(EventObject event) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject event) {
        return true;
    }

    @Override
    public Object getCellEditorValue() {
        return isSelected();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        boolean isSelect = (Boolean) value;
        setSelected(isSelect);
        return this;
    }

}
