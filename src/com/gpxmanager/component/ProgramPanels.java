package com.gpxmanager.component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;


/**
 * Titre : Cave &agrave; vin
 * Description : Votre description
 * Copyright : Copyright (c) 2012
 * Soci&eacute;t&eacute; : Seb Informatique
 *
 * @author S&eacute;bastien Duch&eacute;
 * @version 1.6
 * @since 13/06/22
 */
public class ProgramPanels {

  private static final JTabbedPane TABBED_PANE = new JTabbedPane();
  private static final List<TabLabel> TAB_LABELS = new ArrayList<>();


  public static int findTab(ImageIcon image, Component component) {
    for (int i = 0; i < TABBED_PANE.getTabCount(); i++) {
      try {
        if (TABBED_PANE.getTabComponentAt(i) != null && TABBED_PANE.getIconAt(i) != null && TABBED_PANE.getIconAt(i).equals(image)) {
          return i;
        }
      } catch (RuntimeException ignored) {
      }
    }
    if (component != null) {
      return TABBED_PANE.indexOfComponent(component);
    }
    return -1;
  }

  public static int getSelectedTabIndex() {
    return TABBED_PANE.getSelectedIndex();
  }

  public static <T> T getSelectedComponent(Class<T> className) {
    return className.cast(TABBED_PANE.getSelectedComponent());
  }


  public static void selectOrAddTab(Component component, String tabLabel, Icon icon) {
    new MySwingWorker() {
      @Override
      protected void done() {
        try {
          TABBED_PANE.setSelectedComponent(component);
        } catch (IllegalArgumentException e) {
          addTab(tabLabel, icon, component);
        }
      }
    }.execute();
  }

  public static void insertTab(String title, Icon icon, Component component, int index) {
    new MySwingWorker() {
      @Override
      protected void done() {
        TABBED_PANE.insertTab(title, icon, component, null, index);
//        addCloseButtonToTab(component, getSelectedTabIndex(), true);
        insertTabLabel(index, title);
        TABBED_PANE.setSelectedIndex(index);
      }
    }.execute();
  }

  public static void addTab(String title, Icon icon, Component component) {
    new MySwingWorker() {
      @Override
      protected void done() {
        TABBED_PANE.addTab(title, icon, component);
//        addCloseButtonToTab(component);
        int index = TABBED_PANE.getTabCount() - 1;
        addTabLabel(index, title);
        TABBED_PANE.setSelectedIndex(index);
        TABBED_PANE.setVisible(true);
      }
    }.execute();
  }

  public static void setTitleAt(int index, String title) {
    TABBED_PANE.setTitleAt(index, title);
    updateTabLabel(index, title);
    TABBED_PANE.setSelectedIndex(index);
  }

  private static void addTabLabel(int index, String label) {
    TAB_LABELS.add(new TabLabel(index, label));
  }

  private static void insertTabLabel(int index, String label) {
    TAB_LABELS.stream().filter(tabLabel -> tabLabel.getIndex() >= index).forEach(TabLabel::incrementIndex);
    TAB_LABELS.add(new TabLabel(index, label));
  }

  private static void updateTabLabel(int index, String label) {
    TAB_LABELS.stream()
        .filter(tabLabel -> tabLabel.getIndex() == index)
        .forEach(tabLabel -> tabLabel.setLabel(label));
    TAB_LABELS.add(new TabLabel(index, label));
  }

  public static void removeSelectedTab() {
    removeTabAt(TABBED_PANE.getSelectedIndex());
  }

  public static void removeTabAt(int index) {
    TABBED_PANE.removeTabAt(index);
    final List<TabLabel> tabLabels = TAB_LABELS.stream().filter(tabLabel -> tabLabel.getIndex() == index).toList();
    TAB_LABELS.removeAll(tabLabels);
    TAB_LABELS.stream().filter(tabLabel -> tabLabel.getIndex() > index).forEach(TabLabel::decrementIndex);
  }

//  private static void addCloseButtonToTab(final Component component) {
//    addCloseButtonToTab(component, -1, true);
//  }

  public static void removeAll() {
    TABBED_PANE.removeAll();
    TAB_LABELS.clear();
  }

  public static boolean runExit() {
    for (Component c : TABBED_PANE.getComponents()) {
      if (c instanceof ITabListener) {
        if (!((ITabListener) c).tabWillClose(null)) {
//          Program.Debug("ProgramPanels: Exiting progam cancelled!");
          return false;
        }
      }
    }
    return true;
  }

  public static JTabbedPane getTabbedPane() {
    return TABBED_PANE;
  }


  static class TabLabel {
    private int index;
    private String label;
    private String modifiedLabel;
    private boolean modified;

    public TabLabel(int index, String label) {
      this.index = index;
      this.label = label;
      modifiedLabel = label + "*";
      modified = false;
    }

    public int getIndex() {
      return index;
    }

    public String getLabel() {
      return modified ? modifiedLabel : label;
    }

    public void setLabel(String label) {
      this.label = label;
      modifiedLabel = label + "*";
    }

    public boolean isModified() {
      return modified;
    }

    public void setModified(boolean modified) {
      this.modified = modified;
    }

    public void decrementIndex() {
      index--;
    }

    public void incrementIndex() {
      index++;
    }
  }
}
