package dev.melvinchia3636.airngreen.ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dev.melvinchia3636.airngreen.service.alerts.AqiAlert;

/**
 * Window-wide status line and alert banner.
 */
public final class StatusBar extends JPanel {

    private static final Color WARNING = new Color(0xB45309);

    private final JLabel message = new JLabel("Ready.");
    private final JLabel alert = new JLabel();

    /**
     * Creates an empty status bar.
     */
    public StatusBar() {
        super(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        add(message, BorderLayout.WEST);
        add(alert, BorderLayout.EAST);
    }

    /**
     * Shows a plain status message.
     *
     * @param text message to display
     */
    public void show(String text) {
        message.setText(text);
    }

    /**
     * Shows a threshold crossing until the next one.
     *
     * @param event alert to display
     */
    public void showAlert(AqiAlert event) {
        alert.setForeground(WARNING);
        alert.setText("ALERT: " + event.level().label()
                + " (AQI " + Math.round(event.aqi()) + ") - "
                + event.stationName());
    }
}
