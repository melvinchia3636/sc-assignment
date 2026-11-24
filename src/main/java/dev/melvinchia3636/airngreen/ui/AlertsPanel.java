package dev.melvinchia3636.airngreen.ui;

import dev.melvinchia3636.airngreen.model.AlertThreshold;
import dev.melvinchia3636.airngreen.model.AqiAlert;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/** Alert settings view: threshold and history. */
public final class AlertsPanel extends JPanel {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter
        .ofPattern("d MMM, HH:mm:ss", Locale.ENGLISH)
        .withZone(ZoneId.systemDefault());

    private final JComboBox<AlertThreshold> thresholdBox = new JComboBox<>(
        AlertThreshold.values()
    );
    private final DefaultListModel<String> historyModel = new DefaultListModel<>();
    private final JList<String> historyList = new JList<>(historyModel);

    /**
     * Creates the alerts view.
     *
     * @param threshold initially selected threshold
     */
    public AlertsPanel(AlertThreshold threshold) {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        thresholdBox.setSelectedItem(threshold);
        add(settingsCard(), BorderLayout.NORTH);
        add(historyCard(), BorderLayout.CENTER);
    }

    /**
     * Registers the threshold selection action.
     *
     * @param action callback receiving the new threshold
     */
    public void onThresholdChange(Consumer<AlertThreshold> action) {
        thresholdBox.addActionListener(
            event -> action
                .accept((AlertThreshold) thresholdBox.getSelectedItem())
        );
    }

    /**
     * Replaces the alert history list.
     *
     * <p>
     * Safe to call from any thread; the model is updated on the event-dispatch
     * thread.
     *
     * @param alerts alerts ordered newest first
     */
    public void showHistory(List<AqiAlert> alerts) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showHistory(alerts));

            return;
        }

        historyModel.clear();
        for (AqiAlert alert : alerts) {
            historyModel
                .addElement(
                    String.format(
                        Locale.ENGLISH,
                        "%s  %s  AQI %.0f  %s",
                        TIME_FORMAT
                            .format(Instant.ofEpochMilli(alert.createdAt())),
                        alert.level().label(),
                        alert.aqi(),
                        alert.stationName()
                    )
                );
        }
        if (alerts.isEmpty()) {
            historyModel.addElement(
                "No alerts yet. Raise a reading above the threshold."
            );
        }
    }

    private JPanel settingsCard() {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.add(new JLabel("Notify me when air quality reaches:"));
        row.add(Box.createHorizontalStrut(8));
        row.add(thresholdBox);
        row.add(Box.createHorizontalGlue());

        JTextArea explanation = new JTextArea("""
            Alerts fire when a fresh reading reaches the selected band and is
            worse than the last announced reading. Repeated refreshes in the
            same band stay quiet; cleaner air re-arms the alert.\
            """);
        explanation.setEditable(false);
        explanation.setOpaque(false);
        explanation.setLineWrap(true);
        explanation.setWrapStyleWord(true);

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.add(row, BorderLayout.NORTH);
        body.add(explanation, BorderLayout.CENTER);

        return card("Pollution alerts", body);
    }

    private JPanel historyCard() {
        JScrollPane scroll = new JScrollPane(historyList);
        scroll.setPreferredSize(new Dimension(600, 240));

        return card("Alert history", scroll);
    }

    private JPanel card(String title, java.awt.Component body) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            )
        );
        card.add(body, BorderLayout.CENTER);

        return card;
    }
}
