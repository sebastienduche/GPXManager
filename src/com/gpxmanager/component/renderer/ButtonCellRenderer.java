package com.gpxmanager.component.renderer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;


/**
 * Titre : Cave &agrave; vin
 * Description : Votre description
 * Copyright : Copyright (c) 2004
 * Soci&eacute;t&eacute; : Seb Informatique
 *
 * @author S&eacute;bastien Duch&eacute;
 * @version 0.9
 * @since 05/05/22
 */
public class ButtonCellRenderer extends JButton implements TableCellRenderer {

    private static final long serialVersionUID = -6826155883692278688L;
    private final String label;
    private String tooltips;
    private ImageIcon image;

    public ButtonCellRenderer(String label) {
        super();
        this.label = label;
    }

    public ButtonCellRenderer(String label, ImageIcon image, String tooltips) {
        super();
        this.label = label;
        this.image = image;
        this.tooltips = tooltips;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        if (value == null) {
            return this;
        }
        boolean isSelect = (Boolean) value;
        setSelected(isSelect);
        setText(label);
        if (image != null) {
            setIcon(image);
        }
        if (tooltips != null) {
            setToolTipText(tooltips);
        }

        return this;
    }
}
