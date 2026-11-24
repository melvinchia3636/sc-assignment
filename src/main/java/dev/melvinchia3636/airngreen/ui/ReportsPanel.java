package dev.melvinchia3636.airngreen.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import dev.melvinchia3636.airngreen.model.AqiLevel;
import dev.melvinchia3636.airngreen.model.CitizenReport;
import dev.melvinchia3636.airngreen.model.District;

/**
 * Citizen report view: a validated form plus the stored report list.
 */
public final class ReportsPanel extends JPanel {

    private static final String[] CATEGORIES = {
            "Open burning",
            "Construction dust",
            "Vehicle smoke",
            "Industrial emission",
            "Other"
    };
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter
            .ofPattern("d MMM, HH:mm", Locale.ENGLISH)
            .withZone(ZoneId.systemDefault());

    private final JComboBox<District> districtBox =
            new JComboBox<>(District.values());
    private final JComboBox<String> categoryBox = new JComboBox<>(CATEGORIES);
    private final JTextArea noteArea = new JTextArea(4, 40);
    private final JButton submitButton = new JButton("Submit report");
    private final JLabel message = new JLabel(" ");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Time", "District", "Category", "Description" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    /**
     * Creates the report view.
     */
    public ReportsPanel() {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        message.setVisible(false);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(3).setPreferredWidth(400);

        add(form(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(hint(), BorderLayout.SOUTH);
    }

    /**
     * Registers the submit action.
     *
     * @param action callback that validates and stores the draft
     */
    public void onSubmit(Runnable action) {
        submitButton.addActionListener(event -> action.run());
    }

    /**
     * Currently selected district.
     *
     * @return the district
     */
    public District getDistrict() {
        return (District) districtBox.getSelectedItem();
    }

    /**
     * Currently selected category.
     *
     * @return the category label
     */
    public String getCategory() {
        return String.valueOf(categoryBox.getSelectedItem());
    }

    /**
     * Current description text.
     *
     * @return the raw note
     */
    public String getNote() {
        return noteArea.getText();
    }

    /**
     * Clears the description after a successful submit.
     */
    public void clearNote() {
        noteArea.setText("");
    }

    /**
     * Replaces the table rows.
     *
     * @param reports reports ordered newest first
     */
    public void showReports(List<CitizenReport> reports) {
        model.setRowCount(0);
        for (CitizenReport report : reports) {
            model.addRow(new Object[] {
                    TIME_FORMAT.format(Instant.ofEpochSecond(report.createdAt())),
                    report.district().label(),
                    report.category(),
                    report.note()
            });
        }
    }

    /**
     * Shows validation feedback under the form.
     *
     * @param text    message to display
     * @param warning true for a validation failure
     */
    public void showMessage(String text, boolean warning) {
        message.setText(text);
        message.setForeground(AqiColors.forLevel(
                warning ? AqiLevel.SENSITIVE : AqiLevel.GOOD));
        message.setVisible(true);
    }

    private JPanel form() {
        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.X_AXIS));
        fields.add(new JLabel("District:"));
        fields.add(Box.createHorizontalStrut(6));
        fields.add(districtBox);
        fields.add(Box.createHorizontalStrut(16));
        fields.add(new JLabel("Category:"));
        fields.add(Box.createHorizontalStrut(6));
        fields.add(categoryBox);
        fields.add(Box.createHorizontalGlue());

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.add(fields, BorderLayout.NORTH);
        body.add(new JScrollPane(noteArea), BorderLayout.CENTER);
        body.add(actions(), BorderLayout.SOUTH);

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Report a pollution problem"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        card.add(body, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(700, 190));
        return card;
    }

    private JPanel actions() {
        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.add(message);
        actions.add(Box.createHorizontalGlue());
        actions.add(submitButton);
        return actions;
    }

    private JPanel hint() {
        JPanel hint = new JPanel(new BorderLayout());
        hint.add(new JLabel("Reports are stored locally in ~/.airngreen/reports.json."),
                BorderLayout.WEST);
        return hint;
    }
}
