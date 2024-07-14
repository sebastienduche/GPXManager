package com.gpxmanager.component;

import com.gpxmanager.MyGPXManager;
import com.gpxmanager.MyGPXManagerImage;
import com.gpxmanager.Utils;
import com.gpxmanager.gpx.GPXUtils;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Metadata;
import com.mycomponents.MyAutoHideLabel;
import com.mycomponents.tablecomponents.ButtonCellEditor;
import com.mycomponents.tablecomponents.ButtonCellRenderer;
import com.mytabbedpane.ITabListener;
import com.mytabbedpane.TabEvent;
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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

import static com.gpxmanager.Utils.checkFileExtension;
import static com.gpxmanager.Utils.checkFileNameWithExtension;
import static com.gpxmanager.Utils.createFileChooser;
import static com.gpxmanager.Utils.getLabel;
import static com.gpxmanager.component.MergePanel.MergeTableModel.MergeTableColumn.DELETE;
import static com.gpxmanager.component.MergePanel.MergeTableModel.MergeTableColumn.DOWN;
import static com.gpxmanager.component.MergePanel.MergeTableModel.MergeTableColumn.FILE;
import static com.gpxmanager.component.MergePanel.MergeTableModel.MergeTableColumn.UP;

public class MergePanel extends JPanel implements ITabListener {
  private static final String MERGE_PANEL = "MERGE_PANEL";
  private final MergeTableModel model = new MergeTableModel();
  private final PropertiesPanel propertiesPanel = new PropertiesPanel(null, null, false);
  private final MyAutoHideLabel infoLabel = new MyAutoHideLabel();
  private File previousDir;

  public MergePanel() {
    setLayout(new MigLayout("", "[grow]", "[][grow]"));
    JTable table = new JTable(model);
    TableColumnModel tcm = table.getColumnModel();
    TableColumn tc = tcm.getColumn(UP.ordinal());
    tc.setCellRenderer(new ButtonCellRenderer(MyGPXManagerImage.ARROW_UP));
    tc.setCellEditor(new ButtonCellEditor());
    tc.setMinWidth(25);
    tc.setMaxWidth(25);
    tc = tcm.getColumn(DOWN.ordinal());
    tc.setCellRenderer(new ButtonCellRenderer(MyGPXManagerImage.ARROW_DOWN));
    tc.setCellEditor(new ButtonCellEditor());
    tc.setMinWidth(25);
    tc.setMaxWidth(25);
    tc = tcm.getColumn(DELETE.ordinal());
    tc.setCellRenderer(new ButtonCellRenderer(MyGPXManagerImage.DELETE));
    tc.setCellEditor(new ButtonCellEditor());
    tc.setMinWidth(25);
    tc.setMaxWidth(25);
    JButton addFile = new JButton(new AddAction());
    infoLabel.setForeground(Color.red);
    add(addFile, "wrap");
    add(new JScrollPane(table), "grow, wrap");
    add(propertiesPanel, "growx, wrap");
    JButton merge = new JButton(new MergeAction());
    add(merge, "center, wrap");
    add(infoLabel, "center");
  }

  public String getIdentifier() {
    return MERGE_PANEL;
  }

  @Override
  public boolean tabWillClose(TabEvent tabEvent) {
    return true;
  }

  @Override
  public void tabClosed() {
    MyGPXManager.updateTabbedPane();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MergePanel that = (MergePanel) o;
    return Objects.equals(getIdentifier(), that.getIdentifier());
  }

  @Override
  public int hashCode() {
    return Objects.hash(MERGE_PANEL);
  }

  static class MergeTableModel extends DefaultTableModel {

    ArrayList<File> files = new ArrayList<>();

    @Override
    public int getRowCount() {
      return files == null ? 0 : files.size();
    }

    @Override
    public int getColumnCount() {
      return MergeTableColumn.values().length;
    }

    @Override
    public String getColumnName(int column) {
      MergeTableColumn mergeTableColumn = MergeTableColumn.values()[column];
      if (mergeTableColumn == FILE) {
        return getLabel("merge.file");
      }
      return "";
    }

    @Override
    public boolean isCellEditable(int row, int column) {
      return MergeTableColumn.values()[column] != FILE;
    }

    @Override
    public Object getValueAt(int row, int column) {
      MergeTableColumn mergeTableColumn = MergeTableColumn.values()[column];
      if (mergeTableColumn == FILE) {
        return files.get(row).getAbsolutePath();
      }
      return Boolean.FALSE;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
      MergeTableColumn mergeTableColumn = MergeTableColumn.values()[column];
      if (mergeTableColumn == UP) {
        if (row > 0) {
          File file = files.remove(row);
          files.add(row - 1, file);
          fireTableDataChanged();
        } else {
          JOptionPane.showMessageDialog(null, getLabel("merge.unable.move.up"), getLabel("information"), JOptionPane.ERROR_MESSAGE);
        }
      } else if (mergeTableColumn == DOWN) {
        if (row < files.size() - 1) {
          File file = files.remove(row);
          files.add(row + 1, file);
          fireTableDataChanged();
        } else {
          JOptionPane.showMessageDialog(null, getLabel("merge.unable.move.down"), getLabel("information"), JOptionPane.ERROR_MESSAGE);
        }
      } else if (mergeTableColumn == DELETE) {
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

    enum MergeTableColumn {
      UP,
      DOWN,
      DELETE,
      FILE,
    }
  }

  private class AddAction extends AbstractAction {
    public AddAction() {
      super(getLabel("merge.add.file"), MyGPXManagerImage.ADD);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser fileChooser = createFileChooser();
      fileChooser.setMultiSelectionEnabled(true);
      fileChooser.setCurrentDirectory(previousDir);
      if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null)) {
        File[] files = fileChooser.getSelectedFiles();
        if (files != null) {
          previousDir = files.length > 0 ? files[0].getParentFile() : null;
          for (File file : files) {
            if (checkFileExtension(file)) {
              model.addFile(file);
            }
          }
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
      if (model.getFiles().isEmpty()) {
        JOptionPane.showMessageDialog(null, getLabel("merge.no.files"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (model.getFiles().size() == 1) {
        JOptionPane.showMessageDialog(null, getLabel("merge.one.file"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
        return;
      }
      JFileChooser fileChooser = createFileChooser();
      fileChooser.setCurrentDirectory(Utils.getOpenSaveDirectory());
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(null)) {
        File file = fileChooser.getSelectedFile();
        file = checkFileNameWithExtension(file);
        if (file != null) {
          Utils.setOpenSaveDirectory(file.getParentFile());
          try {
            GPX gpx = GPXUtils.mergeFiles(model.getFiles());
            if (gpx == null) {
              return;
            }
            Metadata metadata = new Metadata();
            propertiesPanel.save(metadata);
            gpx.setMetadata(metadata);
            GPXUtils.writeFile(gpx, file);

            infoLabel.setText(MessageFormat.format(getLabel("merge.file.saved"), file.getAbsolutePath()), true);
          } catch (ParserConfigurationException | IOException | TransformerException | SAXException |
                   ParseException ex) {
            throw new RuntimeException(ex);
          }
        }
        setCursor(Cursor.getDefaultCursor());
      }
    }
  }

}
