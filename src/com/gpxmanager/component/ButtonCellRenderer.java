package com.gpxmanager.component;

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
  private ImageIcon image;

  public ButtonCellRenderer(String label) {
    super();
    this.label = label;
  }

  public ButtonCellRenderer(ImageIcon image) {
    super();
    this.label = "";
    this.image = image;
  }

  public ButtonCellRenderer(String label, ImageIcon image) {
    super();
    this.label = label;
    this.image = image;
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
//    setFont(FONT_PANEL);
    setText(label);
    if (image != null) {
      setIcon(image);
    }

    return this;
  }
}
