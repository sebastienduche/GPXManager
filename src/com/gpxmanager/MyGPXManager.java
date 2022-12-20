package com.gpxmanager;

import com.gpxmanager.component.MergePanel;
import com.gpxmanager.component.MyAutoHideLabel;
import com.gpxmanager.component.MyTabbedPane;
import com.gpxmanager.gpx.GPXParser;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.launcher.MyGPXManagerServer;
import net.miginfocom.swing.MigLayout;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.prefs.Preferences;

import static com.gpxmanager.Utils.getLabel;

public final class MyGPXManager extends JFrame {

    public static final String INTERNAL_VERSION = "1.2";
    public static final String VERSION = "1";
    private static final MyAutoHideLabel INFO_LABEL = new MyAutoHideLabel();
    private final JMenuItem saveFile;
    private final JMenuItem closeFile;

    private final MyGPXManager instance;
    private final Preferences prefs;
    private final JButton saveButton;
    private final MyTabbedPane myTabbedPane;
    private File openedFile = null;

    public MyGPXManager() throws HeadlessException {
        instance = this;
        prefs = Preferences.userNodeForPackage(getClass());
        String locale = prefs.get("MyGPXManager.locale", "en");
        Utils.initResources(new Locale.Builder().setLanguage(locale).build());
        saveFile = new JMenuItem(new SaveFileAction());
        setTitle("MyGPXManager");
        setLayout(new BorderLayout());
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu(getLabel("menu.file"));
        menuBar.add(menuFile);
        JMenu menuGpx = new JMenu(getLabel("menu.gpx"));
        menuBar.add(menuGpx);
        JMenu menuLanguage = new JMenu(getLabel("menu.language"));
        menuBar.add(menuLanguage);
        JMenu menuAbout = new JMenu("?");
        menuBar.add(menuAbout);
        menuFile.add(new JMenuItem(new OpenFileAction()));
        closeFile = new JMenuItem(new CloseFileAction());
        menuFile.add(closeFile);
        menuFile.addSeparator();
        menuFile.add(saveFile);
        menuFile.add(new JMenuItem(new SaveAsFileAction()));
        menuFile.addSeparator();

        final String file = prefs.get("MyGPXManager.file", "");
        if (!file.isEmpty()) {
            menuFile.addSeparator();
            JMenuItem reOpen = new JMenuItem(new ReOpenFileAction());
            reOpen.setText("-" + file);
            menuFile.add(reOpen);
        }
        menuFile.addSeparator();
        menuFile.add(new JMenuItem(new ExitAction()));
        menuGpx.add(new JMenuItem(new MergeAction()));
        ButtonGroup languageGroup = new ButtonGroup();
        JRadioButtonMenuItem englishMenu = new JRadioButtonMenuItem(new LanguageAction(Locale.ENGLISH));
        englishMenu.setSelected(Locale.ENGLISH.getLanguage().equals(locale));
        menuLanguage.add(englishMenu);
        languageGroup.add(englishMenu);
        JRadioButtonMenuItem frenchMenuItem = new JRadioButtonMenuItem(new LanguageAction(Locale.FRENCH));
        frenchMenuItem.setSelected(Locale.FRENCH.getLanguage().equals(locale));
        menuLanguage.add(frenchMenuItem);
        languageGroup.add(frenchMenuItem);
        menuAbout.add(new JMenuItem(new AboutAction()));
        menuAbout.add(new JMenuItem(new SearchUpdateAction()));
        setJMenuBar(menuBar);

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("", "grow", "[][grow][]"));
        add(panel, BorderLayout.CENTER);
        // TODO
        panel.add(new JLabel("update"), "gapleft 20, gaptop 10, hidemode 1, wrap");
        panel.add(myTabbedPane = new MyTabbedPane(), "grow, hidemode 3, wrap");
        myTabbedPane.setVisible(false);

        JToolBar toolBar = new JToolBar();
        final JButton openButton = new JButton(new OpenFileAction());
        openButton.setText("");
        toolBar.add(openButton);
        saveButton = new JButton(new SaveFileAction());
        saveButton.setText("");
        toolBar.add(saveButton);
        toolBar.addSeparator();
        toolBar.setFloatable(true);
        setFileOpened(null);
        add(toolBar, BorderLayout.NORTH);
        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new MigLayout("", "0px[grow]0px", "0px[25:25:25]0px"));
        panelBottom.add(INFO_LABEL, "center");
        INFO_LABEL.setForeground(Color.red);
        add(panelBottom, BorderLayout.SOUTH);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new ExitAction().actionPerformed(null);
            }
        });
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(Integer.parseInt(prefs.get("MyGPXManager.x", "0")), Integer.parseInt(prefs.get("MyGPXManager.y", "0")));
        int width = Integer.parseInt(prefs.get("MyGPXManager.width", "0"));
        int height = Integer.parseInt(prefs.get("MyGPXManager.height", "0"));
        setSize(width != 0 ? width : screenSize.width, height != 0 ? height : screenSize.height);
        setVisible(true);
    }

    public static void setInfoLabel(String text) {
        INFO_LABEL.setText(text, true);
    }

    private static void cleanDebugFiles() {
        String sDir = System.getProperty("user.home") + File.separator + "MyGPXManagerDebug";
        File f = new File(sDir);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthsAgo = LocalDateTime.now().minusMonths(2);
        String[] files = f.list((dir, name) -> {
            String date = "";
            if (name.startsWith("Debug-") && name.endsWith(".log")) {
                date = name.substring(6, name.indexOf(".log"));
            }
            if (name.startsWith("DebugFtp-") && name.endsWith(".log")) {
                date = name.substring(9, name.indexOf(".log"));
            }
            if (!date.isEmpty()) {
                String[] fields = date.split("-");
                LocalDateTime dateTime = now.withMonth(Integer.parseInt(fields[1])).withDayOfMonth(Integer.parseInt(fields[0])).withYear(Integer.parseInt(fields[2]));
                return dateTime.isBefore(monthsAgo);
            }
            return false;
        });

        if (files != null) {
            for (String file : files) {
                f = new File(sDir, file);
                f.deleteOnExit();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MyGPXManager::new);
    }

    private void setFileOpened(File file) {
        boolean opened = (openedFile = file) != null;
        saveFile.setEnabled(opened);
        saveButton.setEnabled(opened);
        closeFile.setEnabled(opened);
        if (file == null || file.isDirectory()) {
            setTitle("MyGPXManager");
        } else {
            setTitle("MyGPXManager - " + file.getAbsolutePath());
        }
    }

    class OpenFileAction extends AbstractAction {
        public OpenFileAction() {
            super(getLabel("menu.openFile"), MyGPXManagerImage.OPEN);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser boiteFichier = new JFileChooser();
            boiteFichier.removeChoosableFileFilter(boiteFichier.getFileFilter());
            boiteFichier.addChoosableFileFilter(Filtre.FILTRE_GPX);
            if (JFileChooser.APPROVE_OPTION == boiteFichier.showOpenDialog(instance)) {
                File file = boiteFichier.getSelectedFile();
                if (file != null) {
                    open(file);
                }
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private void open(File file) {
        try {
            GPX gpx = new GPXParser().parseGPX(new FileInputStream(file));
            myTabbedPane.addTab(file.getName(), null, new GPXPropertiesPanel(gpx));
            prefs.put("MyGPXManager.file", file.getAbsolutePath());
            setFileOpened(file);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void save(GPX gpx, File file) {
        try {
            new GPXParser().writeGPX(gpx, new FileOutputStream(file));
        } catch (ParserConfigurationException | TransformerException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    class CloseFileAction extends AbstractAction {
        public CloseFileAction() {
            super(getLabel("menu.closeFile"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Change openedFile manaagement
            if (openedFile != null && JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(instance, getLabel("question.saveOpenedFile"), getLabel("question"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                GPXPropertiesPanel selectedComponent = myTabbedPane.getSelectedComponent(GPXPropertiesPanel.class);
                save(selectedComponent.getGpx(), openedFile);
            } else {
                setFileOpened(null);
                if (myTabbedPane.runExit()) {
                    myTabbedPane.removeAll();
                    myTabbedPane.setVisible(false);
                }
            }
        }
    }

    class ReOpenFileAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            File file = new File(prefs.get("MyGPXManager.file", ""));
            if (!file.exists()) {
                JOptionPane.showMessageDialog(instance, MessageFormat.format(getLabel("nonExistFile"), file.getAbsolutePath()), getLabel("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            SwingUtilities.invokeLater(() -> {
                open(file);
                setCursor(Cursor.getDefaultCursor());
            });
        }
    }

    class SaveFileAction extends AbstractAction {
        public SaveFileAction() {
            super(getLabel("menu.saveFile"), MyGPXManagerImage.SAVE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser boiteFichier = new JFileChooser();
            boiteFichier.removeChoosableFileFilter(boiteFichier.getFileFilter());
            boiteFichier.addChoosableFileFilter(Filtre.FILTRE_GPX);
            if (openedFile == null || !openedFile.exists()) {
                if (JFileChooser.APPROVE_OPTION == boiteFichier.showSaveDialog(instance)) {
                    openedFile = boiteFichier.getSelectedFile();
                    if (openedFile == null) {
                        setCursor(Cursor.getDefaultCursor());
                        return;
                    }
                    if (!openedFile.getName().toLowerCase().endsWith(Filtre.FILTRE_GPX.toString())) {
                        openedFile = new File(openedFile.getAbsolutePath() + Filtre.FILTRE_GPX);
                    }
                } else {
                    return;
                }
            }
            //save);
        }
    }

    class SaveAsFileAction extends AbstractAction {
        public SaveAsFileAction() {
            super(getLabel("menu.saveFileAs"), MyGPXManagerImage.SAVEAS);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser boiteFichier = new JFileChooser();
            boiteFichier.removeChoosableFileFilter(boiteFichier.getFileFilter());
            boiteFichier.addChoosableFileFilter(Filtre.FILTRE_GPX);
            if (JFileChooser.APPROVE_OPTION == boiteFichier.showSaveDialog(instance)) {
                File file = boiteFichier.getSelectedFile();
                if (file == null) {
                    setCursor(Cursor.getDefaultCursor());
                    return;
                }
                if (!file.getName().toLowerCase().endsWith(Filtre.FILTRE_GPX.toString())) {
                    file = new File(file.getAbsolutePath() + Filtre.FILTRE_GPX);
                }
                // save(file, true);
            }
        }
    }

    class MergeAction extends AbstractAction {
        public MergeAction() {
            super(getLabel("menu.merge"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            myTabbedPane.selectOrAddTab(new MergePanel(), "LABEL", MyGPXManagerImage.OPEN);
        }
    }


    class ExitAction extends AbstractAction {
        public ExitAction() {
            super(getLabel("menu.exit"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(instance, getLabel("question.exit"), getLabel("exit"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                prefs.put("MyGPXManager.x", "" + getLocation().x);
                prefs.put("MyGPXManager.y", "" + getLocation().y);
                prefs.put("MyGPXManager.width", "" + getSize().width);
                prefs.put("MyGPXManager.height", "" + getSize().height);
                cleanDebugFiles();
                System.exit(0);
            }
        }
    }

    class AboutAction extends AbstractAction {
        public AboutAction() {
            super(getLabel("menu.about"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new APropos().setVisible(true);
        }
    }

    class SearchUpdateAction extends AbstractAction {
        public SearchUpdateAction() {
            super(getLabel("menu.checkUpdate"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (MyGPXManagerServer.getInstance().hasAvailableUpdate(INTERNAL_VERSION)) {
                JOptionPane.showMessageDialog(instance, MessageFormat.format(getLabel("newVersion"), MyGPXManagerServer.getInstance().getAvailableVersion(), INTERNAL_VERSION), getLabel("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(instance, getLabel("noUpdate"), getLabel("information"), JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private class LanguageAction extends AbstractAction {
        private final Locale locale;

        public LanguageAction(Locale locale) {
            super(getLabel("menu." + locale.getLanguage()));
            this.locale = locale;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            prefs.put("MyGPXManager.locale", locale.getLanguage());
            JOptionPane.showMessageDialog(instance, getLabel("languageChanged"), getLabel("information"), JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
