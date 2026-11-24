package dev.melvinchia3636.airngreen.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

/**
 * Main window: four tabs and a shared status line.
 */
public final class MainFrame extends JFrame {

    /**
     * Creates the window.
     *
     * @param dashboard air-quality tab
     * @param stations  station directory tab
     * @param reports   citizen reports tab
     * @param alerts    alert settings tab
     * @param status    shared status line
     */
    public MainFrame(JPanel dashboard, JPanel stations, JPanel reports,
                     JPanel alerts, StatusBar status) {
        super("Air & Green | Urban Air Quality Monitor");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setSize(1040, 760);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Air quality", dashboard);
        tabs.addTab("Stations", stations);
        tabs.addTab("Reports", reports);
        tabs.addTab("Alerts", alerts);

        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }
}
