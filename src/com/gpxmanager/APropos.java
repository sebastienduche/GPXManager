package com.gpxmanager;

import net.miginfocom.swing.MigLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

final class APropos extends JDialog {
    static final long serialVersionUID = 150505;
    private final JButton ok = new JButton("OK");
    private final JLabel MyCellarLabel1 = new JLabel("MyGPXManager");
    private final JLabel MyCellarLabel2 = new JLabel("Copyright: S.Duché");
    private final JLabel MyCellarLabel3 = new JLabel("Release: " + MyGPXManager.INTERNAL_VERSION);
    private final JLabel MyCellarLabel4 = new JLabel("Version: " + MyGPXManager.VERSION);

    APropos() {
        super(new JFrame(), "About", true);
        init();
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width) / 2, (screenSize.height - getSize().height) / 2);
    }

    private void init() {
        ok.addActionListener((e) -> dispose());
        MyCellarLabel1.setForeground(Color.red);
        MyCellarLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        MyCellarLabel2.setHorizontalAlignment(SwingConstants.LEFT);
        MyCellarLabel3.setHorizontalAlignment(SwingConstants.LEFT);
        MyCellarLabel4.setHorizontalAlignment(SwingConstants.LEFT);
        MyCellarLabel1.setFont(new Font("Arial", Font.BOLD, 13));
        setLayout(new MigLayout("", "[][]", "[]"));
        add(MyCellarLabel1, "center, span 2, wrap");
        add(MyCellarLabel2, "gaptop 20px, wrap");
        add(MyCellarLabel3, "wrap");
        add(MyCellarLabel4, "wrap");
        add(ok, "gaptop 20px, span 2, center");

        setResizable(false);
    }

    private static class IconPanel extends JPanel {
        static final long serialVersionUID = 1505051;
        private final ImageIcon img;

        private IconPanel(ImageIcon img) {
            this.img = img;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            final Image image = img.getImage();
            if (image != null) {
                g.drawImage(image, 0, 0, 64, 64, this);
            }
        }
    }
}
