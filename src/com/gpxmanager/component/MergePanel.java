package com.gpxmanager.component;

import com.gpxmanager.Filtre;
import com.gpxmanager.MyGPXManagerImage;
import com.gpxmanager.gpx.GPXParser;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Metadata;
import com.gpxmanager.gpx.beans.Route;
import com.gpxmanager.gpx.beans.Track;
import com.gpxmanager.gpx.beans.Waypoint;
import com.mycomponents.tablecomponents.ButtonCellEditor;
import com.mycomponents.tablecomponents.ButtonCellRenderer;
import net.miginfocom.swing.MigLayout;
import org.xml.sax.SAXException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;

import static com.gpxmanager.Utils.getLabel;

public class MergePanel extends JPanel {
    // TODO MERGE FILE IN RIGHT ORDER
    private JButton merge = new JButton(new MergeAction());
    private final MergeTableModel model = new MergeTableModel();
    private final PropertiesPanel propertiesPanel = new PropertiesPanel(null);

    JTable table;

    public MergePanel() {
        setLayout(new MigLayout("", "[grow]", "[][grow]"));
        table = new JTable(model);
        TableColumnModel tcm = table.getColumnModel();
        TableColumn tc = tcm.getColumn(MergeTableModel.UP);
        tc.setCellRenderer(new ButtonCellRenderer(MyGPXManagerImage.ARROW_UP));
        tc.setCellEditor(new ButtonCellEditor());
        tc.setMinWidth(25);
        tc.setMaxWidth(25);
        tc = tcm.getColumn(MergeTableModel.DOWN);
        tc.setCellRenderer(new ButtonCellRenderer(MyGPXManagerImage.ARROW_DOWN));
        tc.setCellEditor(new ButtonCellEditor());
        tc.setMinWidth(25);
        tc.setMaxWidth(25);
        tc = tcm.getColumn(MergeTableModel.DELETE);
        tc.setCellRenderer(new ButtonCellRenderer(MyGPXManagerImage.DELETE));
        tc.setCellEditor(new ButtonCellEditor());
        tc.setMinWidth(25);
        tc.setMaxWidth(25);
        JButton addFile = new JButton(new AddAction());
        add(addFile, "wrap");
        add(new JScrollPane(table), "grow, wrap");
        add(propertiesPanel, "growx, wrap");
        add(merge, "center");

    }

    private class AddAction extends AbstractAction {
        public AddAction() {
            super(getLabel("merge.add.file"), MyGPXManagerImage.ADD);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser boiteFichier = new JFileChooser();
            boiteFichier.removeChoosableFileFilter(boiteFichier.getFileFilter());
            boiteFichier.addChoosableFileFilter(Filtre.FILTRE_GPX);
            if (JFileChooser.APPROVE_OPTION == boiteFichier.showOpenDialog(null)) {
                File file = boiteFichier.getSelectedFile();
                if (file != null) {
                    model.addFile(file);
                }
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private class MergeAction extends AbstractAction {
        public MergeAction() {
            super(getLabel("merge.save.file"), MyGPXManagerImage.SAVE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser boiteFichier = new JFileChooser();
            boiteFichier.removeChoosableFileFilter(boiteFichier.getFileFilter());
            boiteFichier.addChoosableFileFilter(Filtre.FILTRE_GPX);
            if (JFileChooser.APPROVE_OPTION == boiteFichier.showSaveDialog(null)) {
                File file = boiteFichier.getSelectedFile();
                if (file != null) {
                    try {
                        GPX gpx = null;
                        HashSet<Track> tracks = null;
                        HashSet<Route> routes = null;
                        HashSet<Waypoint> waypoints = null;
                        for (File modelFile : model.getFiles()) {
                            if (gpx == null) {
                                gpx = new GPXParser().parseGPX(new FileInputStream(modelFile));
                                if (gpx.getTracks() == null) {
                                    gpx.setTracks(new HashSet<>());
                                }
                                tracks = gpx.getTracks();
                                if (gpx.getRoutes() == null) {
                                    gpx.setRoutes(new HashSet<>());
                                }
                                routes = gpx.getRoutes();
                                if (gpx.getWaypoints() == null) {
                                    gpx.setWaypoints(new HashSet<>());
                                }
                                waypoints = gpx.getWaypoints();
                            } else {
                                GPX gpx1 = new GPXParser().parseGPX(new FileInputStream(modelFile));
                                if (gpx1.getTracks() != null) {
                                    tracks.addAll(gpx1.getTracks());
                                }
                                if (gpx1.getRoutes() != null) {
                                    routes.addAll(gpx1.getRoutes());
                                }
                                if (gpx1.getWaypoints() != null) {
                                    waypoints.addAll(gpx1.getWaypoints());
                                }
                            }
                        }
                        if (gpx == null) {
                            return;
                        }
                        Metadata metadata = new Metadata();
                        propertiesPanel.save(metadata);
                        gpx.setMetadata(metadata);
                        new GPXParser().writeGPX(gpx, new FileOutputStream(file));

                    } catch (ParserConfigurationException | IOException | TransformerException | SAXException |
                             ParseException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private static class MergeTableModel extends DefaultTableModel {
        public static final int UP = 0;
        public static final int DOWN = 1;
        public static final int DELETE = 2;

        ArrayList<File> files = new ArrayList<>();

        @Override
        public int getRowCount() {
            return files == null ? 0 : files.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 3) {
                return getLabel("merge.file");
            }
            return "";
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column < 3) {
                return true;
            }
            return false;
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (column == 3) {
                return files.get(row).getAbsolutePath();
            }
            return Boolean.FALSE;
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            if (column == UP) {
                if (row > 0) {
                    File file = files.remove(row);
                    files.add(row - 1, file);
                    fireTableDataChanged();
                } else {
                    JOptionPane.showMessageDialog(null, getLabel("merge.unable.move.up"), getLabel("information"), JOptionPane.ERROR_MESSAGE);
                }
            } else if (column == DOWN) {
                if (row < files.size() - 1) {
                    File file = files.remove(row);
                    files.add(row + 1, file);
                    fireTableDataChanged();
                } else {
                    JOptionPane.showMessageDialog(null, getLabel("merge.unable.move.down"), getLabel("information"), JOptionPane.ERROR_MESSAGE);
                }
            } else if (column == DELETE) {
                File file = files.get(row);
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, MessageFormat.format(getLabel("merge.confirm.remove"), file.getAbsolutePath()), getLabel("question"), JOptionPane.YES_NO_OPTION)) {
                    files.remove(file);
                    fireTableDataChanged();
                }
            }
        }

        public void addFile(File file) {
            files.add(file);
            fireTableDataChanged();
        }

        public ArrayList<File> getFiles() {
            return files;
        }
    }
}
