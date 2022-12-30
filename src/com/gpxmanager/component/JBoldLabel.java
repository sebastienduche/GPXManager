package com.gpxmanager.component;

import javax.swing.JLabel;
import java.awt.Font;

public class JBoldLabel extends JLabel {

    public JBoldLabel(String label) {
        super(label);
        setFont(getFont().deriveFont(Font.BOLD));
    }
}
