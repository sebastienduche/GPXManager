package com.gpxmanager.component;

import com.gpxmanager.Filtre;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.LinkedList;

import static com.gpxmanager.Utils.getLabel;

public class MergePanel extends JPanel {

    private JButton addFile = new JButton(new AddAction());
    private MergeTableModel model = new MergeTableModel();

    JTable table;

    public MergePanel() {
        setLayout(new MigLayout("", "[grow]", "[][grow]"));
        table = new JTable(model);
        add(addFile, "wrap");
        add(new JScrollPane(table), "grow");
    }

    private class AddAction extends AbstractAction {
        public AddAction() {
            super(getLabel("merge.add.file"));
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

    private class MergeTableModel extends DefaultTableModel {

        LinkedList<File> files = new LinkedList<>();

        @Override
        public int getRowCount() {
            return files == null ? 0 : files.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return super.getColumnName(column);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int column) {
            return files.get(row).getAbsolutePath();
        }

        public void addFile(File file) {
            files.add(file);
        }

        public LinkedList<File> getFiles() {
            return files;
        }
    }
}
