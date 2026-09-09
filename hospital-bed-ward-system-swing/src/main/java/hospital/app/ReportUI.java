package hospital.app;

import hospital.controller.HospitalController;
import hospital.model.Report;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

// ReportUI - generate occupancy and admissions reports
public class ReportUI extends JPanel {
	private final HospitalController controller;
    private final JComboBox<String> typeBox = new JComboBox<>();
    private final JTextField periodField = new JTextField(12);
    private final JLabel periodLbl = new JLabel("Period (YYYY-MM):");
    private final JTextArea reportArea = new JTextArea();
    private final JLabel statusLbl = new JLabel(" ");

    public ReportUI(HospitalController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 12));
        setBackground(AppTheme.SURFACE);
        setBorder(AppTheme.pagePadding());

        // Form card
        typeBox.addItem("Occupancy");
        typeBox.addItem("Admissions");
        typeBox.setFont(AppTheme.FONT_BODY);

        periodField.setFont(AppTheme.FONT_BODY);
        periodField.setToolTipText("e.g. 2026-09  (required for Admissions report)");

        // Auto-fill with current year-month
        periodField.setText(LocalDate.now().toString().substring(0, 7));

        // Grey out period field when Occupancy is selected (not applicable)
        typeBox.addActionListener(e -> {
            boolean isOccupancy = "Occupancy".equals(typeBox.getSelectedItem());
            periodField.setEnabled(!isOccupancy);
            periodLbl.setEnabled(!isOccupancy);
        });
        // Trigger initial state
        periodField.setEnabled(false);
        periodLbl.setEnabled(false);

        JButton generateBtn = AppTheme.primaryButton("📊  Generate Report");
        generateBtn.addActionListener(e -> requestReport(
                (String) typeBox.getSelectedItem(), periodField.getText().trim()));

        statusLbl.setFont(AppTheme.FONT_BODY);
        statusLbl.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 10);

        // Row 0: Report Type
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel typeLbl = new JLabel("Report Type:");
        typeLbl.setFont(AppTheme.FONT_BODY.deriveFont(Font.BOLD));
        typeLbl.setForeground(AppTheme.TEXT_PRIMARY);
        formGrid.add(typeLbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formGrid.add(typeBox, gbc);

        // Row 1: Period
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        periodLbl.setFont(AppTheme.FONT_BODY.deriveFont(Font.BOLD));
        periodLbl.setForeground(AppTheme.TEXT_PRIMARY);
        formGrid.add(periodLbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formGrid.add(periodField, gbc);

        // Row 2: Buttons + status
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.insets = new Insets(12, 0, 0, 10);
        formGrid.add(generateBtn, gbc);
        gbc.gridx = 1; gbc.insets = new Insets(14, 0, 0, 0);
        formGrid.add(statusLbl, gbc);

        JPanel formCard = new JPanel(new BorderLayout());
        formCard.setBackground(AppTheme.CARD);
        formCard.setBorder(AppTheme.sectionBorder("Generate System Report"));
        formCard.add(formGrid, BorderLayout.CENTER);

        // ── Report output area ────────────────────────────────────────────────
        reportArea.setFont(AppTheme.FONT_MONO);
        reportArea.setEditable(false);
        reportArea.setBackground(new Color(0xF8FAFB));
        reportArea.setForeground(AppTheme.TEXT_PRIMARY);
        reportArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportArea.setText("No report generated yet.\nSelect a report type and click 'Generate Report'.");

        JScrollPane scroll = new JScrollPane(reportArea);
        AppTheme.styleScrollPane(scroll);

        JPanel outputCard = new JPanel(new BorderLayout());
        outputCard.setBackground(AppTheme.CARD);
        outputCard.setBorder(AppTheme.sectionBorder("Report Output"));
        outputCard.add(scroll, BorderLayout.CENTER);

        add(formCard,   BorderLayout.NORTH);
        add(outputCard, BorderLayout.CENTER);
    }

    // Report generation 

    public void requestReport(String type, String period) {
        if (type == null) {
            showStatus("Select a report type.", false);
            return;
        }
        if ("Admissions".equals(type) && (period == null || period.isBlank())) {
            showStatus("Enter a period (e.g. 2026-09) for the Admissions report.", false);
            return;
        }

        String effectivePeriod = "Occupancy".equals(type) ? "" : period;
        Report report = controller.generateReport(type, effectivePeriod);

        int count = report.getData() != null ? report.getData().size() : 0;

        if (count == 0) {
            // UC03 alt flow 1a - no records found
            String msg = "No records found" + (effectivePeriod.isBlank() ? "" : " for period: " + period)
                    + ". Adjust the period and try again.";
            showStatus(msg, false);
            reportArea.setText(msg);
            return;
        }

        // Build formatted report text
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════════════════\n");
        sb.append("  HOSPITAL BED & WARD MANAGEMENT SYSTEM\n");
        sb.append("══════════════════════════════════════════════════\n");
        sb.append("  Report ID   : ").append(report.getReportId()).append("\n");
        sb.append("  Report Type : ").append(type).append("\n");
        if (!effectivePeriod.isBlank())
        sb.append("  Period      : ").append(period).append("\n");
        sb.append("  Generated   : ").append(report.getGeneratedDate()).append("\n");
        sb.append("──────────────────────────────────────────────────\n\n");

        for (Object item : report.getData()) {
            sb.append("  ▸  ").append(item).append("\n");
        }
        sb.append("\n══════════════════════════════════════════════════\n");

        reportArea.setText(sb.toString());
        reportArea.setCaretPosition(0);
        showStatus("✔  Report generated successfully.", true);
    }

    // Helpers

    private void showStatus(String msg, boolean success) {
        statusLbl.setText(msg);
        statusLbl.setForeground(success ? AppTheme.SUCCESS : AppTheme.DANGER);
    }
}