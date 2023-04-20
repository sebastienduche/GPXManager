package com.gpxmanager;

import com.google.gson.Gson;
import com.gpxmanager.component.InvertPanel;
import com.gpxmanager.component.MergePanel;
import com.gpxmanager.gpx.GPXParser;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.extensions.GarminExtension;
import com.gpxmanager.launcher.MyGPXManagerServer;
import com.gpxmanager.strava.StravaPanel;
import com.mycomponents.MyAutoHideLabel;
import com.mytabbedpane.MyTabbedPane;
import net.miginfocom.swing.MigLayout;
import org.jstrava.StravaConnection;
import org.jstrava.StravaFirstConfigurationDialog;
import org.jstrava.entities.Activity;
import org.jstrava.user.FileIdentificationStorage;
import org.jstrava.user.IdentificationStorage;
import org.xml.sax.SAXException;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gpxmanager.ProgramPreferences.FILE;
import static com.gpxmanager.ProgramPreferences.FILE1;
import static com.gpxmanager.ProgramPreferences.FILE2;
import static com.gpxmanager.ProgramPreferences.FILE3;
import static com.gpxmanager.ProgramPreferences.FILE4;
import static com.gpxmanager.ProgramPreferences.LOCALE;
import static com.gpxmanager.ProgramPreferences.LOCALTION_X;
import static com.gpxmanager.ProgramPreferences.LOCALTION_Y;
import static com.gpxmanager.ProgramPreferences.STRAVA;
import static com.gpxmanager.ProgramPreferences.STRAVA_ALL_DATA;
import static com.gpxmanager.ProgramPreferences.getPreference;
import static com.gpxmanager.ProgramPreferences.setPreference;
import static com.gpxmanager.Utils.DATE_FORMATER_DD_MM_YYYY;
import static com.gpxmanager.Utils.DEBUG_DIRECTORY;
import static com.gpxmanager.Utils.getLabel;

public final class MyGPXManager extends JFrame {
    public static final String INTERNAL_VERSION = "9.7";
    public static final String VERSION = "4.2";
    private static final MyAutoHideLabel INFO_LABEL = new MyAutoHideLabel();
    private static JMenuItem saveFile;
    private static JMenuItem saveAsFile;
    private final JMenuItem closeFile;
    private static JMenuItem connectToStravaMenuItem;
    static JButton stravaButton = null;
    private static MyGPXManager instance;
    private static JButton saveButton;
    private static MyTabbedPane myTabbedPane;
    private final LinkedList<File> openedFiles = new LinkedList<>();
    private final LinkedList<File> reopenedFiles = new LinkedList<>();

    public static final Gson GSON = new Gson();

    private static FileWriter oDebugFile = null;
    private static File debugFile = null;

    private final static GPXParser GPX_PARSER = new GPXParser();

    public MyGPXManager() throws HeadlessException {
        instance = this;
        MyGPXManagerServer.getInstance().checkVersion();
        GPX_PARSER.addExtensionParser(new GarminExtension());
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> showException(e, true));
        reopenedFiles.add(new File(getPreference(FILE1, "")));
        reopenedFiles.add(new File(getPreference(FILE2, "")));
        reopenedFiles.add(new File(getPreference(FILE3, "")));
        reopenedFiles.add(new File(getPreference(FILE4, "")));
        String locale = getPreference(LOCALE, "en");
        Utils.initResources(new Locale.Builder().setLanguage(locale).build());
        saveFile = new JMenuItem(new SaveFileAction());
        saveAsFile = new JMenuItem(new SaveAsFileAction());
        saveButton = new JButton(new SaveFileAction());
        saveButton.setText("");
        setTitle("MyGPXManager");
        setLayout(new BorderLayout());
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu(getLabel("menu.file"));
        menuBar.add(menuFile);
        JMenu menuGpx = new JMenu(getLabel("menu.gpx"));
        menuBar.add(menuGpx);
        JMenu menuLanguage = new JMenu(getLabel("menu.language"));
        menuBar.add(menuLanguage);
        JMenu menuStrava = new JMenu(getLabel("menu.strava"));
        menuBar.add(menuStrava);
        menuStrava.add(new JMenuItem(new FirstConnectionToStravaAction()));
        connectToStravaMenuItem = new JMenuItem(new ConnectToStravaAction());
        menuStrava.add(connectToStravaMenuItem);
        connectToStravaMenuItem.setEnabled(!getPreference(STRAVA, "").isBlank());
        JMenu menuAbout = new JMenu("?");
        menuBar.add(menuAbout);
        menuFile.add(new JMenuItem(new OpenFileAction()));
        menuFile.add(closeFile = new JMenuItem(new CloseFileAction()));
        menuFile.addSeparator();
        menuFile.add(saveFile);
        menuFile.add(saveAsFile);

        if (!reopenedFiles.isEmpty()) {
            AtomicBoolean hasSeparator = new AtomicBoolean(false);
            reopenedFiles.stream()
                    .distinct()
                    .filter(File::isFile)
                    .forEach(file -> {
                        if (!hasSeparator.get()) {
                            menuFile.addSeparator();
                            hasSeparator.set(true);
                        }
                        JMenuItem reOpen = new JMenuItem(new ReOpenFileAction(file));
                        reOpen.setText("-" + file);
                        menuFile.add(reOpen);
                    });
        }
        menuFile.addSeparator();
        menuFile.add(new JMenuItem(new ExitAction()));
        menuGpx.add(new JMenuItem(new MergeAction()));
        menuGpx.add(new JMenuItem(new InvertAction()));
        ButtonGroup languageGroup = new ButtonGroup();
        JRadioButtonMenuItem englishMenu = new JRadioButtonMenuItem(new LanguageAction(Locale.ENGLISH));
        englishMenu.setSelected(Locale.ENGLISH.getLanguage().equals(locale));
        menuLanguage.add(englishMenu);
        languageGroup.add(englishMenu);
        JRadioButtonMenuItem frenchMenuItem = new JRadioButtonMenuItem(new LanguageAction(Locale.FRENCH));
        frenchMenuItem.setSelected(Locale.FRENCH.getLanguage().equals(locale));
        menuLanguage.add(frenchMenuItem);
        languageGroup.add(frenchMenuItem);
        Locale nl = Locale.of("nl");
        JRadioButtonMenuItem dutchMenuItem = new JRadioButtonMenuItem(new LanguageAction(nl));
        dutchMenuItem.setSelected(nl.getLanguage().equals(locale));
        menuLanguage.add(dutchMenuItem);
        languageGroup.add(dutchMenuItem);
        menuAbout.add(new JMenuItem(new AboutAction()));
        menuAbout.add(new JMenuItem(new SearchUpdateAction()));
        setJMenuBar(menuBar);

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("", "grow", "[][grow][]"));
        add(panel, BorderLayout.CENTER);
        MyAutoHideLabel update = new MyAutoHideLabel();
        panel.add(update, "gapleft 20, gaptop 10, hidemode 1, wrap");
        if (MyGPXManagerServer.getInstance().hasAvailableUpdate(MyGPXManagerServer.getLocalVersion())) {
            update.setVisible(true);
            update.setText(MessageFormat.format(getLabel("updateAvailable"), MyGPXManagerServer.getInstance().getAvailableVersion(), VERSION + " - " + INTERNAL_VERSION), true, 30000, false);
        } else {
            update.setVisible(false);
        }
        panel.add(myTabbedPane = new MyTabbedPane(), "grow, hidemode 3, wrap");
        myTabbedPane.setVisible(false);

        JToolBar toolBar = new JToolBar();
        final JButton openButton = new JButton(new OpenFileAction());
        openButton.setText("");
        toolBar.add(openButton);
        toolBar.add(saveButton);
        toolBar.addSeparator();
        final JButton mergeButton = new JButton(new MergeAction());
        mergeButton.setText("");
        toolBar.add(mergeButton);
        final JButton invertButton = new JButton(new InvertAction());
        invertButton.setText("");
        toolBar.add(invertButton);
        toolBar.addSeparator();
        stravaButton = new JButton(new ConnectToStravaAction());
        stravaButton.setText("");
        stravaButton.setEnabled(!getPreference(STRAVA, "").isBlank());
        toolBar.add(stravaButton);
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
        setLocation(Integer.parseInt(getPreference(LOCALTION_X, "0")), Integer.parseInt(getPreference(LOCALTION_Y, "0")));
        int width = Integer.parseInt(getPreference(ProgramPreferences.WIDTH, "0"));
        int height = Integer.parseInt(getPreference(ProgramPreferences.HEIGHT, "0"));
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

    public static void updateTabbedPane() {
        if (myTabbedPane.getTabCount() == 0) {
            myTabbedPane.setVisible(false);
            saveButton.setEnabled(false);
            saveFile.setEnabled(false);
            saveAsFile.setEnabled(false);
        }
    }

    public GPXParser getGpxParser() {
        return GPX_PARSER;
    }

    public void closeFile(GPXPropertiesPanel gpxPropertiesPanel) {
        if (gpxPropertiesPanel.isModified() && JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(instance, MessageFormat.format(getLabel("question.saveOpenedFile"), gpxPropertiesPanel.getFile()), getLabel("question"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
            try {
                gpxPropertiesPanel.save();
                save(gpxPropertiesPanel.getGpx(), gpxPropertiesPanel.getFile());
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (!openedFiles.isEmpty()) {
            openedFiles.remove(gpxPropertiesPanel.getFile());
            setFileOpened(null);
            myTabbedPane.removeSelectedTab();
            updateTabbedPane();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MyGPXManager::new);
    }

    private void setFileOpened(File file) {
        if (file != null) {
            openedFiles.add(file);
        }
        boolean opened = !openedFiles.isEmpty();
        saveFile.setEnabled(opened);
        saveAsFile.setEnabled(opened);
        saveButton.setEnabled(opened);
        closeFile.setEnabled(opened);
    }

    public File checkFile(File file) {
        if (file == null) {
            return null;
        }
        if (!file.getName().toLowerCase().endsWith(Filtre.FILTRE_GPX.toString())) {
            return new File(file.getAbsolutePath() + Filtre.FILTRE_GPX);
        }
        return file;
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
            boiteFichier.setCurrentDirectory(Utils.getOpenSaveDirectory());
            if (JFileChooser.APPROVE_OPTION == boiteFichier.showOpenDialog(instance)) {
                File file = boiteFichier.getSelectedFile();
                if (file != null) {
                    Utils.setOpenSaveDirectory(file.getParentFile());
                    open(file);
                }
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    static class FirstConnectionToStravaAction extends AbstractAction {
        public FirstConnectionToStravaAction() {
            super(getLabel("menu.firstConnectionStrava"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            StravaFirstConfigurationDialog stravaFirstConfigurationDialog = new StravaFirstConfigurationDialog();

            IdentificationStorage identificationStorage = stravaFirstConfigurationDialog.getIdentificationStorage();
            if (identificationStorage != null) {
                if (identificationStorage instanceof FileIdentificationStorage) {
                    // The file contains the data
                    File fileSaved = ((FileIdentificationStorage) identificationStorage).getFile();
                    ProgramPreferences.setPreference(ProgramPreferences.STRAVA, fileSaved.getAbsolutePath());
                    connectToStravaMenuItem.setEnabled(!getPreference(ProgramPreferences.STRAVA, "").isBlank());
                    stravaButton.setEnabled(!getPreference(ProgramPreferences.STRAVA, "").isBlank());
                }
                StravaConnection stravaConnection;
                try {
                    stravaConnection = new StravaConnection(identificationStorage);
                } catch (IOException | URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
                myTabbedPane.addTab(getLabel("menu.strava"), MyGPXManagerImage.STRAVA, new StravaPanel(stravaConnection, stravaConnection.getStrava().getCurrentAthleteActivities(1, 50)), true);
            }
        }
    }

    static class ConnectToStravaAction extends AbstractAction {
        public ConnectToStravaAction() {
            super(getLabel("menu.connectStrava"), MyGPXManagerImage.STRAVA);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            File file = new File(getPreference(ProgramPreferences.STRAVA, ""));
            FileIdentificationStorage fileIdentificationStorage = new FileIdentificationStorage(file);

            StravaConnection stravaConnection;
            try {
                stravaConnection = new StravaConnection(fileIdentificationStorage);
                setInfoLabel(getLabel("strava.connectionOK"));
            } catch (IOException | URISyntaxException ex) {
                throw new RuntimeException(ex);
            }

            List<Activity> activities = loadDataIfExist();
            if (activities.isEmpty()) {
                activities = stravaConnection.getStrava().getCurrentAthleteActivities(1, 50);
            }

            myTabbedPane.addTab(getLabel("menu.strava"), MyGPXManagerImage.STRAVA, new StravaPanel(stravaConnection, activities), true);
        }
    }


    private void open(File file) {
        try {
            GPX gpx = GPX_PARSER.parseGPX(new FileInputStream(file));
            myTabbedPane.addTab(file.getName(), new GPXPropertiesPanel(file, gpx), true);
            reopenedFiles.addFirst(file);
            setFileOpened(file);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void save(GPX gpx, File file) {
        try {
            GPX_PARSER.writeGPX(gpx, new FileOutputStream(file));
        } catch (ParserConfigurationException | TransformerException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        setInfoLabel(MessageFormat.format(getLabel("file.saved"), file.getAbsolutePath()));
    }

    private static List<Activity> loadDataIfExist() {
        String existingFile = getPreference(STRAVA_ALL_DATA, null);
        if (existingFile != null && new File(existingFile).exists()) {
            try (FileReader fileReader = new FileReader(existingFile);
                 BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String json = bufferedReader.lines().reduce(String::concat).orElseThrow();
                return new ArrayList<>(List.of(GSON.fromJson(json, Activity[].class)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Collections.emptyList();
    }

    private void showException(Throwable e, boolean showWindowErrorAndExit) {
        StackTraceElement[] st = e.getStackTrace();
        String error = "";
        for (StackTraceElement s : st) {
            error = error.concat("\n" + s);
        }

        if (showWindowErrorAndExit) {
            JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        Utils.saveError(e);
        Debug("Program: ERROR:");
        Debug("Program: " + e);
        Debug("Program: " + error);
        e.printStackTrace();
        if (debugFile != null) {
            try {
                oDebugFile.flush();
                oDebugFile.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
//            try {
//                sendErrorToGitHub(e.toString(), debugFile);
//            } catch (IOException ignored) {
//            }
            oDebugFile = null;
        }

        if (showWindowErrorAndExit) {
            System.exit(999);
        }
    }

    public static void Debug(String sText) {
        try {
            if (oDebugFile == null) {
                String sDir = System.getProperty("user.home");
                if (!sDir.isEmpty()) {
                    sDir += File.separator + DEBUG_DIRECTORY;
                }
                File f_obj = new File(sDir);
                if (f_obj.exists() || f_obj.mkdir()) {
                    String sDate = LocalDate.now().format(DATE_FORMATER_DD_MM_YYYY);
                    debugFile = new File(sDir, "Debug-" + sDate + ".log");
                    oDebugFile = new FileWriter(debugFile, true);
                }
            }
            oDebugFile.write("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "]: " + sText + "\n");
            oDebugFile.flush();
        } catch (IOException ignored) {
        }
    }

    private static void closeDebug() {
        if (oDebugFile == null) {
            return;
        }

        try {
            oDebugFile.flush();
            oDebugFile.close();
        } catch (IOException ignored) {
        }
    }

    public static MyGPXManager getInstance() {
        return instance;
    }

    class CloseFileAction extends AbstractAction {
        public CloseFileAction() {
            super(getLabel("menu.closeFile"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            GPXPropertiesPanel selectedComponent = myTabbedPane.getSelectedComponent(GPXPropertiesPanel.class);
            closeFile(selectedComponent);
        }
    }

    class ReOpenFileAction extends AbstractAction {

        private final File file;

        public ReOpenFileAction(File file) {
            this.file = file;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
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
            if (openedFiles.isEmpty()) {
                if (JFileChooser.APPROVE_OPTION == boiteFichier.showSaveDialog(instance)) {
                    File openedFile = boiteFichier.getSelectedFile();
                    if (openedFile == null) {
                        setCursor(Cursor.getDefaultCursor());
                        return;
                    }
                    if (!openedFile.getName().toLowerCase().endsWith(Filtre.FILTRE_GPX.toString())) {
                        openedFile = new File(openedFile.getAbsolutePath() + Filtre.FILTRE_GPX);
                    }
                    GPXPropertiesPanel selectedComponent = myTabbedPane.getSelectedComponent(GPXPropertiesPanel.class);
                    try {
                        selectedComponent.save();
                        save(selectedComponent.getGpx(), openedFile);
                    } catch (ParseException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } else {
                GPXPropertiesPanel selectedComponent = myTabbedPane.getSelectedComponent(GPXPropertiesPanel.class);
                try {
                    selectedComponent.save();
                    save(selectedComponent.getGpx(), selectedComponent.getFile());
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
            }
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
            boiteFichier.setCurrentDirectory(Utils.getOpenSaveDirectory());
            if (JFileChooser.APPROVE_OPTION == boiteFichier.showSaveDialog(instance)) {
                File file = boiteFichier.getSelectedFile();
                file = checkFile(file);
                if (file == null) {
                    setCursor(Cursor.getDefaultCursor());
                    return;
                }
                Utils.setOpenSaveDirectory(file.getParentFile());
                if (!file.getName().toLowerCase().endsWith(Filtre.FILTRE_GPX.toString())) {
                    file = new File(file.getAbsolutePath() + Filtre.FILTRE_GPX);
                }
                GPXPropertiesPanel selectedComponent = myTabbedPane.getSelectedComponent(GPXPropertiesPanel.class);
                save(selectedComponent.getGpx(), file);
            }
        }
    }

    static class MergeAction extends AbstractAction {
        public MergeAction() {
            super(getLabel("menu.merge"), MyGPXManagerImage.MERGE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            myTabbedPane.selectOrAddTab(new MergePanel(instance), getLabel("merge.title"), MyGPXManagerImage.MERGE, true);
        }
    }

    static class InvertAction extends AbstractAction {
        public InvertAction() {
            super(getLabel("menu.invert"), MyGPXManagerImage.INVERT);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            myTabbedPane.selectOrAddTab(new InvertPanel(instance), getLabel("invert.action"), MyGPXManagerImage.INVERT, true);
        }
    }


    class ExitAction extends AbstractAction {
        public ExitAction() {
            super(getLabel("menu.exit"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(instance, getLabel("question.exit"), getLabel("exit"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                AtomicInteger i = new AtomicInteger(1);
                reopenedFiles.stream()
                        .limit(4)
                        .forEach(file -> setPreference(FILE + i.getAndIncrement(), file.getAbsolutePath()));
                setPreference(LOCALTION_X, String.valueOf(getLocation().x));
                setPreference(LOCALTION_Y, String.valueOf(getLocation().y));
                setPreference(ProgramPreferences.WIDTH, String.valueOf(getSize().width));
                setPreference(ProgramPreferences.HEIGHT, String.valueOf(getSize().height));
                cleanDebugFiles();
                closeDebug();
                System.exit(0);
            }
        }
    }

    static class AboutAction extends AbstractAction {
        public AboutAction() {
            super(getLabel("menu.about"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new About().setVisible(true);
        }
    }

    static class SearchUpdateAction extends AbstractAction {
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

    private static class LanguageAction extends AbstractAction {
        private final Locale locale;

        public LanguageAction(Locale locale) {
            super(getLabel("menu." + locale.getLanguage()));
            this.locale = locale;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setPreference(LOCALE, locale.getLanguage());
            JOptionPane.showMessageDialog(instance, getLabel("languageChanged"), getLabel("information"), JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
