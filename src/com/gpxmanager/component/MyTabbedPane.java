package com.gpxmanager.component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

    public void selectOrAddTab(Component component, String title, Icon icon) {
        new MySwingWorker() {
            @Override
            protected void done() {
                try {
                    setSelectedComponent(component);
                } catch (IllegalArgumentException e) {
                    addTab(title, icon, component);
                }
            }
        }.execute();
    }

    public void insertTab(String title, Component component, int index) {
        insertTab(title, null, component, index, false);
    }

    public void insertTab(String title, Component component, int index, boolean withCloseButton) {
        insertTab(title, null, component, index, withCloseButton);
    }

    public void insertTab(String title, Icon icon, Component component, int index) {
        insertTab(title, icon, component, index, false);
    }

    public void insertTab(String title, Icon icon, Component component, int index, boolean withCloseButton) {
        new MySwingWorker() {
            @Override
            protected void done() {
                insertTab(title, icon, component, null, index);
                if (withCloseButton) {
                    addCloseButtonToTab(component, getSelectedIndex(), true);
                }
                insertTabLabel(index, title);
                setSelectedIndex(index);
            }
        }.execute();
    }

    public void addTab(String title, Icon icon, Component component) {
        addTab(title, icon, component, false);
    }

    public void addTab(String title, Component component) {
        addTab(title, null, component, false);
    }

    public void addTab(String title, Component component, boolean withCloseButton) {
        addTab(title, null, component, withCloseButton);
    }

    public void addTab(String title, Icon icon, Component component, boolean withCloseButton) {
        new MySwingWorker() {
            @Override
            protected void done() {
                addTab(title, icon, component, null);
                if (withCloseButton) {
                    addCloseButtonToTab(component);
                }
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
        if (!doBeforeRemoving(index)) {
            return;
        }
        super.removeTabAt(index);
        final List<TabLabel> tabLabels = TAB_LABELS.stream().filter(tabLabel -> tabLabel.getIndex() == index).toList();
        TAB_LABELS.removeAll(tabLabels);
        TAB_LABELS.stream().filter(tabLabel -> tabLabel.getIndex() > index).forEach(TabLabel::decrementIndex);
    }

    /**
     * Can be overridden to decide if a tab can be closed
     *
     * @param index clicked tab
     */
    public boolean doBeforeRemoving(int index) {
        return true;
    }

    public void removeAll() {
        super.removeAll();
        TAB_LABELS.clear();
    }

    public boolean runExit() {
        for (Component c : getComponents()) {
            if (c instanceof ITabListener) {
                if (!((ITabListener) c).tabWillClose(null)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addCloseButtonToTab(final Component component) {
        addCloseButtonToTab(component, -1, true);
    }

    private void addCloseButtonToTab(final Component component, int indexToGoBack, boolean leftTabDirection) {
        final int index = indexOfComponent(component);
        if (index != -1) {
            setTabComponentAt(index, new JButtonTabComponent(this, indexToGoBack));
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_W)
                        && (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK)) {

                    // Ctrl-W permet de fermer les onglets du JTabbedPane
                    final int selectedIndex = getSelectedIndex();
                    if ((selectedIndex != -1) && (getSelectedComponent().equals(component))) {

                        // Un onglet est actif, supprimer le composant
                        removeTabAt(selectedIndex);
                        int previousIndex = leftTabDirection ? selectedIndex - 1 : indexToGoBack;
                        if (previousIndex != -1 && getTabCount() > previousIndex) {
                            setSelectedIndex(previousIndex);
                        }

                        removeKeyListener(this);
                        e.consume();
                    }
                }
            }
        });
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
