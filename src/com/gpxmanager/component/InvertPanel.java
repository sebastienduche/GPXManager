package com.gpxmanager.component;

import com.gpxmanager.Filtre;
import com.gpxmanager.MyGPXManager;
import com.gpxmanager.MyGPXManagerImage;
import com.gpxmanager.Utils;
import com.gpxmanager.gpx.GPXParser;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Metadata;
import com.gpxmanager.gpx.beans.Waypoint;
import com.mycomponents.JModifyTextField;
import com.mycomponents.MyAutoHideLabel;
import com.mytabbedpane.ITabListener;
import com.mytabbedpane.TabEvent;
import net.miginfocom.swing.MigLayout;
import org.xml.sax.SAXException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.gpxmanager.Utils.getLabel;

public class InvertPanel extends JPanel implements ITabListener {
    private final PropertiesPanel propertiesPanel = new PropertiesPanel(null);
    private final MyAutoHideLabel infoLabel = new MyAutoHideLabel();
    private final JModifyTextField fileTextField = new JModifyTextField();
    private final JButton browse = new JButton(new BrowseAction());
    private final MyGPXManager parent;
    private File file;
    private GPX gpx;
    private static final String INVERT_PANEL = "INVERT_PANEL";

    public InvertPanel(MyGPXManager parent) {
        this(parent, null);
    }

    public InvertPanel(MyGPXManager parent, File file) {
        this.parent = parent;
        this.file = file;
        setLayout(new MigLayout("", "[grow]", "[]20px[]20px[][]"));
        infoLabel.setForeground(Color.red);
        add(fileTextField, "growx, split 2");
        add(browse, "wrap");
        add(propertiesPanel, "growx, wrap");
        JButton invert = new JButton(new InvertAction());
        add(invert, "center, wrap");
        add(infoLabel, "center");
        loadFile();
    }

    public String getIdentifier() {
        return INVERT_PANEL;
    }

    @Override
    public boolean tabWillClose(TabEvent tabEvent) {
        return true;
    }

    @Override
    public void tabClosed() {
        parent.updateTabbedPane();
    }

    private class BrowseAction extends AbstractAction {
        public BrowseAction() {
            super(getLabel("invert.file"), MyGPXManagerImage.OPEN);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser boiteFichier = new JFileChooser();
            boiteFichier.removeChoosableFileFilter(boiteFichier.getFileFilter());
            boiteFichier.addChoosableFileFilter(Filtre.FILTRE_GPX);
            boiteFichier.setCurrentDirectory(Utils.getOpenSaveDirectory());
            if (JFileChooser.APPROVE_OPTION == boiteFichier.showOpenDialog(null)) {
                file = boiteFichier.getSelectedFile();
                if (file != null) {
                    Utils.setOpenSaveDirectory(file.getParentFile());
                    loadFile();
                } else {
                    fileTextField.setText("");
                }
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private void loadFile() {
        if (file == null) {
            return;
        }
        fileTextField.setText(file.getAbsolutePath());
        try {
            gpx = parent.getGpxParser().parseGPX(new FileInputStream(file));
            if (gpx != null) {
                propertiesPanel.load(gpx.getMetadata());
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private class InvertAction extends AbstractAction {
        public InvertAction() {
            super(getLabel("menu.invert"), MyGPXManagerImage.SAVE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (file == null) {
                JOptionPane.showMessageDialog(null, getLabel("invert.no.file"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            JFileChooser boiteFichier = new JFileChooser();
            boiteFichier.removeChoosableFileFilter(boiteFichier.getFileFilter());
            boiteFichier.addChoosableFileFilter(Filtre.FILTRE_GPX);
            boiteFichier.setCurrentDirectory(Utils.getOpenSaveDirectory());
            if (JFileChooser.APPROVE_OPTION == boiteFichier.showSaveDialog(null)) {
                File file = boiteFichier.getSelectedFile();
                if (file != null) {
                    Utils.setOpenSaveDirectory(file.getParentFile());
                    file = parent.checkFile(file);
                    if (gpx.getTracks() != null) {
                        gpx.getTracks().forEach(track -> reverseAndCleanTime(track.getTrackPoints()));
                    }
                    if (gpx.getRoutes() != null) {
                        gpx.getRoutes().forEach(route -> reverseAndCleanTime(route.getRoutePoints()));
                    }
                    if (gpx.getWaypoints() != null) {
                        reverseAndCleanTime(gpx.getWaypoints());
                    }
                    try {
                        Metadata metadata = new Metadata();
                        propertiesPanel.save(metadata);
                        gpx.setMetadata(metadata);
                        new GPXParser().writeGPX(gpx, new FileOutputStream(file));

                        infoLabel.setText(MessageFormat.format(getLabel("merge.file.saved"), file.getAbsolutePath()), true);
                    } catch (ParserConfigurationException | IOException | TransformerException |
                             ParseException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                setCursor(Cursor.getDefaultCursor());
            }
        }

        private void reverseAndCleanTime(List<Waypoint> waypoints) {
            Collections.reverse(waypoints);
            waypoints.forEach(waypoint -> waypoint.setTime(null));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvertPanel that = (InvertPanel) o;
        return Objects.equals(getIdentifier(), that.getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(INVERT_PANEL);
    }

}