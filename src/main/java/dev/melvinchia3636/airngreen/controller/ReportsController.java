package dev.melvinchia3636.airngreen.controller;

import dev.melvinchia3636.airngreen.model.CitizenReport;
import dev.melvinchia3636.airngreen.service.ReportService;
import dev.melvinchia3636.airngreen.ui.ReportsPanel;
import dev.melvinchia3636.airngreen.ui.StatusBar;

/**
 * Controller for citizen reports.
 */
public final class ReportsController {

    private final ReportsPanel view;
    private final ReportService reports;
    private final StatusBar status;

    /**
     * Creates the controller.
     *
     * @param view    report view
     * @param reports report service
     * @param status  window status line
     */
    public ReportsController(ReportsPanel view, ReportService reports,
                             StatusBar status) {
        this.view = view;
        this.reports = reports;
        this.status = status;
    }

    /**
     * Wires the view and shows stored reports.
     */
    public void start() {
        view.onSubmit(this::submit);
        view.showReports(reports.all());
    }

    private void submit() {
        try {
            CitizenReport report = reports.add(
                    view.district(), view.category(), view.note());
            view.clearNote();
            view.showReports(reports.all());
            view.showMessage("Report saved for " + report.district().label() + ".",
                    false);
            status.show("Citizen report saved.");
        } catch (IllegalArgumentException invalid) {
            view.showMessage(invalid.getMessage(), true);
            status.show("Report not saved: " + invalid.getMessage());
        }
    }
}
