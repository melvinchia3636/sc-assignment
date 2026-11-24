package dev.melvinchia3636.airngreen.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import dev.melvinchia3636.airngreen.model.AqiLevel;
import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.model.StationSummary;
import dev.melvinchia3636.airngreen.service.GeoDistance;

/**
 * Station directory view: filter the public list and open one station.
 */
public final class StationsPanel extends JPanel {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter
            .ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH)
            .withZone(ZoneId.systemDefault());

    private final JTextField search = new JTextField();
    private final JButton reloadButton = new JButton("Load station list");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Station", "AQI", "Distance" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
    private final JTextArea detail = new JTextArea(6, 40);
    private final JButton loadButton = new JButton("Open selected station");
    private final JLabel notice = new JLabel(" ");
    private boolean detailLoading;
    private District district = District.KUALA_LUMPUR;

    /**
     * Creates the station directory view.
     */
    public StationsPanel() {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setRowSorter(sorter);
        table.setFillsViewportHeight(true);
        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        notice.setVisible(false);
        loadButton.setEnabled(false);

        add(toolbar(), BorderLayout.NORTH);
        add(split(), BorderLayout.CENTER);
        add(actions(), BorderLayout.SOUTH);

        search.getDocument().addDocumentListener(new SearchListener());
        table.getSelectionModel().addListSelectionListener(
                event -> {
                    if (!event.getValueIsAdjusting()) {
                        updateLoadButton();
                    }
                });
    }

    /**
     * Registers the reload action.
     *
     * @param action callback that fetches the station list
     */
    public void onReload(Runnable action) {
        reloadButton.addActionListener(event -> action.run());
    }

    /**
     * Registers the open-station action.
     *
     * @param action callback receiving the station id
     */
    public void onLoadSelected(Consumer<String> action) {
        Runnable run = () -> {
            String idx = getSelectedIdx();
            if (idx != null && !idx.isBlank()) {
                action.accept(idx);
            }
        };
        loadButton.addActionListener(event -> run.run());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    run.run();
                }
            }
        });
    }

    /**
     * Sets the district used to compute station distances.
     *
     * @param district current district
     */
    public void setDistrict(District district) {
        this.district = district;
    }

    /**
     * Replaces the table rows.
     *
     * @param stations station directory
     */
    public void setStations(List<StationSummary> stations) {
        model.setRowCount(0);
        for (StationSummary station : stations) {
            model.addRow(new Object[] {
                    station.idx(),
                    station.name(),
                    formatAqi(station.aqi()),
                    formatDistance(station)
            });
        }
    }

    /**
     * Selected station id.
     *
     * @return the id, or null when nothing is selected
     */
    public String getSelectedIdx() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return String.valueOf(model.getValueAt(table.convertRowIndexToModel(row), 0));
    }

    /**
     * Shows a station report under the table.
     *
     * @param reading loaded station report
     */
    public void showDetail(StationReading reading) {
        detail.setText("""
                %s (%s)
                AQI %s - %s
                Updated %s
                %s
                %s
                """.formatted(
                reading.name(),
                reading.idx(),
                formatAqi(reading.aqi()),
                reading.level().label(),
                TIME_FORMAT.format(Instant.ofEpochSecond(reading.updatedAt())),
                pollutants(reading),
                seriesSummary(reading)));
        detail.setCaretPosition(0);
    }

    /**
     * Shows that a station report is being fetched.
     *
     * @param idx station id being loaded
     */
    public void showDetailLoading(String idx) {
        detail.setText("Loading station " + idx + "...");
        detail.setCaretPosition(0);
    }

    /**
     * Shows a failed station lookup under the table.
     *
     * @param message user-facing failure message
     */
    public void showDetailError(String message) {
        detail.setText("Could not load this station.\n" + message);
        detail.setCaretPosition(0);
    }

    /**
     * Enables or disables the open button while a detail request runs.
     *
     * @param loading true while the detail request is running
     */
    public void setDetailLoading(boolean loading) {
        detailLoading = loading;
        loadButton.setText(loading ? "Loading..." : "Open selected station");
        updateLoadButton();
    }

    /**
     * Enables or disables the reload button.
     *
     * @param loading true while a request is running
     */
    public void setLoading(boolean loading) {
        reloadButton.setEnabled(!loading);
        reloadButton.setText(loading ? "Loading..." : "Load station list");
    }

    /**
     * Shows a short notice above the table.
     *
     * @param message text to display
     * @param warning true for an error
     */
    public void showMessage(String message, boolean warning) {
        notice.setText(message);
        notice.setForeground(AqiColors.forLevel(
                warning ? AqiLevel.SENSITIVE : AqiLevel.GOOD));
        notice.setVisible(true);
    }

    private JPanel toolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.add(new JLabel("Search:"));
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(search);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(reloadButton);
        return toolbar;
    }

    private JSplitPane split() {
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(600, 360));
        JScrollPane detailScroll = new JScrollPane(detail);
        detailScroll.setPreferredSize(new Dimension(600, 120));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tableScroll, detailScroll);
        split.setResizeWeight(0.8);
        split.setBorder(null);
        return split;
    }

    private JPanel actions() {
        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.add(notice);
        actions.add(Box.createHorizontalGlue());
        actions.add(loadButton);
        return actions;
    }

    private String formatAqi(Double aqi) {
        return aqi == null || aqi.isNaN() ? "-" : String.valueOf(Math.round(aqi));
    }

    private String formatDistance(StationSummary station) {
        double km = GeoDistance.kmBetween(
                district.latitude(), district.longitude(),
                station.latitude(), station.longitude());
        return String.format(Locale.ENGLISH, "%.1f km", km);
    }

    private String pollutants(StationReading reading) {
        List<String> parts = new ArrayList<>();
        for (String key : List.of("pm25", "pm10", "o3", "no2", "so2", "co")) {
            reading.pollutant(key).ifPresent(value -> parts.add(
                    key.toUpperCase(Locale.ROOT) + " "
                            + String.format(Locale.ENGLISH, "%.1f", value)));
        }
        return parts.isEmpty()
                ? "No pollutant breakdown reported."
                : String.join("   ", parts);
    }

    private String seriesSummary(StationReading reading) {
        if (reading.series().isEmpty()) {
            return "Recent AQI: no samples reported.";
        }

        DoubleSummaryStatistics stats = reading.series().stream()
                .mapToDouble(point -> point.value())
                .summaryStatistics();
        return String.format(Locale.ENGLISH,
                "Recent AQI: %d samples, min %.0f, max %.0f, average %.0f",
                stats.getCount(), stats.getMin(), stats.getMax(),
                stats.getAverage());
    }

    private void updateLoadButton() {
        loadButton.setEnabled(!detailLoading && table.getSelectedRow() >= 0);
    }

    private void applyFilter() {
        String text = search.getText().trim();
        sorter.setRowFilter(text.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    }

    /** Re-filters whenever the search box changes. */
    private final class SearchListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent event) {
            applyFilter();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            applyFilter();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            applyFilter();
        }
    }
}
