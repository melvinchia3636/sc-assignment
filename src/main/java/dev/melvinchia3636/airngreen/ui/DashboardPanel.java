package dev.melvinchia3636.airngreen.ui;

import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.NearbyPark;
import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.service.advice.AdviceStrategies;
import dev.melvinchia3636.airngreen.service.advice.AdviceStrategy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

/**
 * Dashboard view: current reading, audience advice and nearby parks.
 *
 * <p>
 * The panel renders state only; controllers perform the network calls.
 */
public final class DashboardPanel extends JPanel {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter
        .ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH)
        .withZone(ZoneId.systemDefault());
    private static final List<String> POLLUTANT_KEYS = List
        .of("pm25", "pm10", "o3", "no2", "so2", "co");

    private final JComboBox<District> districtBox = new JComboBox<>(
        District.values()
    );
    private final JComboBox<AdviceStrategy> audienceBox = new JComboBox<>(
        AdviceStrategies.getAll().toArray(new AdviceStrategy[0])
    );
    private final JButton refreshButton = new JButton("Refresh");

    private final JLabel aqiValue = new JLabel("--");
    private final JLabel levelLabel = new JLabel("No reading yet");
    private final JLabel stationLabel = new JLabel(" ");
    private final JLabel updatedLabel = new JLabel(" ");
    private final JLabel pollutantLabel = new JLabel(" ");
    private final JTextArea adviceArea = new JTextArea(3, 40);
    private final JLabel outdoorLabel = new JLabel(" ");
    private final DefaultListModel<String> parkModel = new DefaultListModel<>();
    private final JList<String> parkList = new JList<>(parkModel);
    private final JLabel notice = new JLabel(" ");

    /**
     * Creates the dashboard.
     *
     * @param district initially selected district
     * @param audience initially selected audience
     */
    public DashboardPanel(District district, AdviceStrategy audience) {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        districtBox.setSelectedItem(district);
        audienceBox.setSelectedItem(audience);
        audienceBox.setRenderer(new AudienceRenderer());

        adviceArea.setEditable(false);
        adviceArea.setLineWrap(true);
        adviceArea.setWrapStyleWord(true);
        adviceArea.setOpaque(false);
        parkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notice.setVisible(false);

        add(controls(), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);
    }

    /**
     * Registers the refresh action.
     *
     * @param action callback invoked by the button
     */
    public void onRefresh(Runnable action) {
        refreshButton.addActionListener(event -> action.run());
    }

    /**
     * Registers the district selection action.
     *
     * @param action callback receiving the selected district
     */
    public void onDistrictChange(Consumer<District> action) {
        districtBox.addActionListener(
            event -> action.accept((District) districtBox.getSelectedItem())
        );
    }

    /**
     * Registers the audience selection action.
     *
     * @param action callback receiving the selected strategy
     */
    public void onAudienceChange(Consumer<AdviceStrategy> action) {
        audienceBox.addActionListener(
            event -> action
                .accept((AdviceStrategy) audienceBox.getSelectedItem())
        );
    }

    /**
     * Currently selected district.
     *
     * @return the district
     */
    public District getSelectedDistrict() {
        return (District) districtBox.getSelectedItem();
    }

    /**
     * Enables or disables the refresh button.
     *
     * @param loading true while a request is running
     */
    public void setLoading(boolean loading) {
        refreshButton.setEnabled(!loading);
        refreshButton.setText(loading ? "Loading..." : "Refresh");
    }

    /**
     * Renders a reading and its advice.
     *
     * @param reading station reading
     * @param advice audience-specific guidance
     * @param outdoorMinutes suggested safe time outdoors
     */
    public void showReading(
        StationReading reading,
        String advice,
        int outdoorMinutes
    ) {
        aqiValue.setText(formatAqi(reading.aqi()));
        aqiValue.setForeground(AqiColors.forLevel(reading.level()));
        levelLabel.setText(reading.level().label());
        levelLabel.setForeground(AqiColors.forLevel(reading.level()));
        stationLabel.setText(reading.name() + " (" + reading.idx() + ")");
        updatedLabel.setText(
            "Updated "
                + TIME_FORMAT.format(Instant.ofEpochSecond(reading.updatedAt()))
        );
        pollutantLabel.setText(pollutants(reading));
        adviceArea.setText(advice);
        outdoorLabel.setText(
            outdoorMinutes > 0
                ? "Suggested outdoor time: " + outdoorMinutes + " minutes."
                : "Suggested outdoor time: avoid outdoor activity."
        );
    }

    /**
     * Replaces the nearby-park list.
     *
     * @param parks parks ordered by distance
     */
    public void showParks(List<NearbyPark> parks) {
        parkModel.clear();
        for (NearbyPark nearby : parks) {
            parkModel.addElement(
                String.format(
                    Locale.ENGLISH,
                    "%s - %.1f km (%s)",
                    nearby.park().name(),
                    nearby.distanceKm(),
                    nearby.park().facilities()
                )
            );
        }
    }

    /**
     * Shows a short notice above the reading.
     *
     * @param message text to show
     * @param warning true for an error or offline notice
     */
    public void showMessage(String message, boolean warning) {
        notice.setText(message);
        notice.setForeground(AqiColors.forMessage(warning));
        notice.setVisible(true);
    }

    /** Hides the notice line. */
    public void clearMessage() {
        notice.setVisible(false);
    }

    private JPanel controls() {
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
        controls.add(new JLabel("District:"));
        controls.add(Box.createHorizontalStrut(6));
        controls.add(districtBox);
        controls.add(Box.createHorizontalStrut(16));
        controls.add(new JLabel("Advice for:"));
        controls.add(Box.createHorizontalStrut(6));
        controls.add(audienceBox);
        controls.add(Box.createHorizontalGlue());
        controls.add(refreshButton);

        return controls;
    }

    private JScrollPane content() {
        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.add(notice);
        stack.add(readingCard());
        stack.add(Box.createVerticalStrut(12));
        stack.add(adviceCard());
        stack.add(Box.createVerticalStrut(12));
        stack.add(parksCard());
        stack.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(stack);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        return scroll;
    }

    private JPanel readingCard() {
        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.X_AXIS));

        aqiValue.setFont(aqiValue.getFont().deriveFont(Font.BOLD, 48f));
        JPanel meta = new JPanel();
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
        levelLabel.setFont(levelLabel.getFont().deriveFont(Font.BOLD, 16f));
        meta.add(levelLabel);
        meta.add(stationLabel);
        meta.add(updatedLabel);
        meta.add(pollutantLabel);

        hero.add(aqiValue);
        hero.add(Box.createHorizontalStrut(18));
        hero.add(meta);

        return card("Current air quality", hero);
    }

    private JPanel adviceCard() {
        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.add(outdoorLabel, BorderLayout.NORTH);
        body.add(adviceArea, BorderLayout.CENTER);

        return card("Outdoor activity advice", body);
    }

    private JPanel parksCard() {
        JScrollPane scroll = new JScrollPane(parkList);
        scroll.setPreferredSize(new Dimension(400, 150));

        return card("Nearby green spaces", scroll);
    }

    private JPanel card(String title, Component body) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            )
        );
        card.add(body, BorderLayout.CENTER);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        return card;
    }

    private String pollutants(StationReading reading) {
        List<String> parts = new ArrayList<>();
        for (String key : POLLUTANT_KEYS) {
            reading.pollutant(key).ifPresent(
                value -> parts.add(
                    key.toUpperCase(Locale.ROOT) + " " + formatNumber(value)
                )
            );
        }

        return parts.isEmpty()
            ? "No pollutant breakdown reported."
            : String.join("   ", parts);
    }

    private String formatAqi(double aqi) {
        return Double.isFinite(aqi) ? String.valueOf(Math.round(aqi)) : "--";
    }

    private String formatNumber(double value) {
        return String.format(Locale.ENGLISH, "%.1f", value);
    }

    /** Renders advice strategies by their display name. */
    private static final class AudienceRenderer
        extends
            DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean selected,
            boolean focused
        ) {
            super.getListCellRendererComponent(
                list,
                value,
                index,
                selected,
                focused
            );
            if (value instanceof AdviceStrategy strategy) {
                setText(strategy.name());
            }

            return this;
        }
    }
}
