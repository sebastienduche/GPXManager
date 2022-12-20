package com.gpxmanager.component;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Titre : MyTabbedPane
 * Description : Votre description
 * Copyright : Copyright (c) 2022
 * Soci&eacute;t&eacute; : Seb Informatique
 *
 * @author S&eacute;bastien Duch&eacute;
 * @version 1.7
 * @since 20/12/22
 */
public class MyTabbedPane extends JTabbedPane {

    private static final List<TabLabel> TAB_LABELS = new ArrayList<>();

    public int findTab(ImageIcon image, Component component) {
        for (int i = 0; i < getTabCount(); i++) {
            try {
                if (getTabComponentAt(i) != null && getIconAt(i) != null && getIconAt(i).equals(image)) {
                    return i;
                }
            } catch (RuntimeException ignored) {
            }
        }
        if (component != null) {
            return indexOfComponent(component);
        }
        return -1;
    }

    public <T> T getSelectedComponent(Class<T> className) {
        return className.cast(getSelectedComponent());
    }

    public void selectOrAddTab(Component component, String tabLabel, Icon icon) {
        new MySwingWorker() {
            @Override
            protected void done() {
                try {
                    setSelectedComponent(component);
                } catch (IllegalArgumentException e) {
                    addTab(tabLabel, icon, component);
                }
            }
        }.execute();
    }

    public void insertTab(String title, Icon icon, Component component, int index) {
        new MySwingWorker() {
            @Override
            protected void done() {
                insertTab(title, icon, component, null, index);
//        addCloseButtonToTab(component, getSelectedTabIndex(), true);
                insertTabLabel(index, title);
                setSelectedIndex(index);
            }
        }.execute();
    }

    public void addTab(String title, Icon icon, Component component) {
        new MySwingWorker() {
            @Override
            protected void done() {
                addTab(title, icon, component, null);
//        addCloseButtonToTab(component);
                int index = getTabCount() - 1;
                addTabLabel(index, title);
                setSelectedIndex(index);
                setVisible(true);
            }
        }.execute();
    }

    public void setTitleAt(int index, String title) {
        super.setTitleAt(index, title);
        updateTabLabel(index, title);
        setSelectedIndex(index);
    }

    public void removeSelectedTab() {
        removeTabAt(getSelectedIndex());
    }

    public void removeTabAt(int index) {
        super.removeTabAt(index);
        final List<TabLabel> tabLabels = TAB_LABELS.stream().filter(tabLabel -> tabLabel.getIndex() == index).toList();
        TAB_LABELS.removeAll(tabLabels);
        TAB_LABELS.stream().filter(tabLabel -> tabLabel.getIndex() > index).forEach(TabLabel::decrementIndex);
    }

//  private static void addCloseButtonToTab(final Component component) {
//    addCloseButtonToTab(component, -1, true);
//  }

    public void removeAll() {
        super.removeAll();
        TAB_LABELS.clear();
    }

    public boolean runExit() {
        for (Component c : getComponents()) {
            if (c instanceof ITabListener) {
                if (!((ITabListener) c).tabWillClose(null)) {
//          Program.Debug("ProgramPanels: Exiting progam cancelled!");
                    return false;
                }
            }
        }
        return true;
    }

    private void addTabLabel(int index, String label) {
        TAB_LABELS.add(new TabLabel(index, label));
    }

    private void insertTabLabel(int index, String label) {
        TAB_LABELS.stream().filter(tabLabel -> tabLabel.getIndex() >= index).forEach(TabLabel::incrementIndex);
        TAB_LABELS.add(new TabLabel(index, label));
    }

    private void updateTabLabel(int index, String label) {
        TAB_LABELS.stream()
                .filter(tabLabel -> tabLabel.getIndex() == index)
                .forEach(tabLabel -> tabLabel.setLabel(label));
        TAB_LABELS.add(new TabLabel(index, label));
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
