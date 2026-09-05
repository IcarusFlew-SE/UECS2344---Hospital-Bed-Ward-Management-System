package hospital.app;

import hospital.controller.HospitalController;
import hospital.model.Report;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;

public class ReportUI extends JPanel {
	private final HospitalController controller;
	private final JComboBox<String> typeBox = new JComboBox<>();
	private final JTextField periodField = new JTextField();
	private final JLabel resultLabel = new JLabel(" ");

	public ReportUI(HospitalController controller) {
		this.controller = controller;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		typeBox.addItem("Occupancy");
		typeBox.addItem("Admissions");
		periodField.setToolTipText("e.g. 2026-08");
		typeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, typeBox.getPreferredSize().height));
		periodField.setMaximumSize(new Dimension(Integer.MAX_VALUE, periodField.getPreferredSize().height));

		JButton submitBtn = new JButton("Generate Report");
		submitBtn.addActionListener(e -> requestReport(
				(String) typeBox.getSelectedItem(),
				periodField.getText()));

		add(label("Generate System Report"));
		add(Box.createVerticalStrut(10));
		add(label("Report Type:"));
		add(typeBox);
		add(Box.createVerticalStrut(8));
		add(label("Period:"));
		add(periodField);
		add(Box.createVerticalStrut(10));
		add(submitBtn);
		add(Box.createVerticalStrut(8));
		add(resultLabel);
	}

	// Admin request to generate reports
	public void requestReport(String type, String period) {
		if (type == null || period == null || period.isBlank()) {
			resultLabel.setText("Select a report type and enter a period.");
			return;
		}
		Report report = controller.generateReport(type, period);
		int count = report.getData() != null ? report.getData().size() : 0;
		if (count == 0) {
			// UC03 alt flow 1a - no records found for the specified period
			resultLabel.setText("No records found for " + period + ". Adjust the period and try again.");
			return;
		}
		resultLabel.setText("Report " + report.getReportId() + " (" + type + ", " + period
				+ ") generated with " + count + " records.");
	}

	private static JLabel label(String text) {
		JLabel l = new JLabel(text);
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		return l;
	}
}
