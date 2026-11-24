package dev.melvinchia3636.airngreen.controller;

import dev.melvinchia3636.airngreen.model.CitizenReport;
import dev.melvinchia3636.airngreen.service.ReportService;
import dev.melvinchia3636.airngreen.ui.ReportsPanel;
import dev.melvinchia3636.airngreen.ui.StatusBar;

/** Controller for citizen reports. */
public final class ReportsController {

    private final ReportsPanel reportsPanel;
    private final ReportService reportService;
    private final StatusBar statusBar;

    /**
     * Creates the controller.
     *
     * @param reportsPanel report view
     * @param reportService report service
     * @param statusBar window status line
     */
    public ReportsController(
        ReportsPanel reportsPanel,
        ReportService reportService,
        StatusBar statusBar
    ) {
        this.reportsPanel = reportsPanel;
        this.reportService = reportService;
        this.statusBar = statusBar;
    }

    /** Wires the view and shows stored reports. */
    public void start() {
        reportsPanel.onSubmit(this::submit);
        reportsPanel.showReports(reportService.getAll());
    }

    private void submit() {
        try {
            CitizenReport savedReport = reportService.add(
                reportsPanel.getDistrict(),
                reportsPanel.getCategory(),
                reportsPanel.getNote()
            );
            reportsPanel.clearNote();
            reportsPanel.showReports(reportService.getAll());
            reportsPanel.showMessage(
                "Report saved for " + savedReport.district().label() + ".",
                false
            );
            statusBar.show("Citizen report saved.");
        } catch (IllegalArgumentException invalid) {
            reportsPanel.showMessage(invalid.getMessage(), true);
            statusBar.show("Report not saved: " + invalid.getMessage());
        }
    }
}
