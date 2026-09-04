package hospital.app;

import hospital.controller.HospitalController;
import hospital.model.Bed;
import hospital.model.BedStatus;
import hospital.model.Ward;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.*;

public class WardStatusUI extends VBox{
	private final HospitalController controller;
    private final ComboBox<Ward> wardBox = new ComboBox<>();
    private final ComboBox<Bed> bedBox = new ComboBox<>();
    private final ComboBox<BedStatus> statusBox = new ComboBox<>();
    private final Label resultLabel = new Label();
    
    public WardStatusUI(HospitalController controller, List<Ward> wards) {
    	this.controller = controller;
    	
    	setSpacing(10);
    	setPadding(new Insets(15));
    	
    	wardBox.getItems().addAll(wards);
    	wardBox.setOnAction(e -> {
    		bedBox.getItems().clear();
    		if (wardBox.getValue() != null) {
    			bedBox.getItems().addAll(wardBox.getValue().getBeds());
    		}
    	});
    	statusBox.getItems().addAll(BedStatus.values());
    	
    	Button submitBtn = new Button("Update Status");
    	submitBtn.setOnAction(e -> selectBed(bedBox.getValue(), statusBox.getValue()));
    	
    	getChildren().addAll(
    		new Label("Update Bed Status"),
    		new Label("Ward: "), wardBox,
    		new Label("Bed: "), bedBox,
    		new Label("New Status: "), statusBox,
    		submitBtn,
    		resultLabel
    	);
    }
    
    // Nurse use WardStatusUI: select bed for bed status
    public void selectBed(Bed bed, BedStatus newStatus) {
        if (bed == null || newStatus == null) {
            resultLabel.setText("Select a bed and a status first.");
            return;
        }
        try {
            controller.updateBedStatus(bed, newStatus);
            resultLabel.setText(bed.getBedId() + " is now " + newStatus);
        } catch (IllegalStateException ex) {
            resultLabel.setText("Invalid transition: " + ex.getMessage());
        }
    }
}
