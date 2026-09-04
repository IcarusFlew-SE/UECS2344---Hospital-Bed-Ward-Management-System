package hospital.app;

import hospital.controller.HospitalController;
import hospital.model.Report;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.*;

public class ReportUI extends VBox {
	private final HospitalController controller;
    private final ComboBox<String> typeBox = new ComboBox<>();
    private final TextField periodField = new TextField();
    private final Label resultLabel = new Label();
    
    public ReportUI(HospitalController controller) {
    	this.controller = controller;
    	setSpacing(10);
    	setPadding(new Insets(15));
    	
    	typeBox.getItems().addAll("Occupancy", "Admissions");
    	periodField.setPromptText("e.g. 2026-08");
    	
    	Button submitBtn = new Button("Generate Report");
        submitBtn.setOnAction(e -> requestReport(typeBox.getValue(), periodField.getText()));

        getChildren().addAll(
                new Label("Generate System Report"),
                new Label("Report Type:"), typeBox,
                new Label("Period:"), periodField,
                submitBtn,
                resultLabel
        );
    }
    
    // Admin request to generate reports
    public void requestReport(String type, String period) {
        if (type == null || period == null || period.isBlank()) {
            resultLabel.setText("Select a report type and enter a period.");
            return;
        }
        Report report = controller.generateReport(type, period);
        int count = report.getData() != null ? report.getData().size() : 0;
        resultLabel.setText("Report " + report.getReportId() + " generated (" + count + " records).");
    }
}
