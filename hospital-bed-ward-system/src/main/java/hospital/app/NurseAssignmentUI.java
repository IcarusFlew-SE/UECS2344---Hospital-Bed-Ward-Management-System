package hospital.app;

import hospital.controller.HospitalController;
import hospital.model.Nurse;
import hospital.model.Ward;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.*;

public class NurseAssignmentUI extends VBox{
	private final HospitalController controller;
    private final ComboBox<Nurse> nurseBox = new ComboBox<>();
    private final ComboBox<Ward> wardBox = new ComboBox<>();
    private final TextField shiftField = new TextField();
    private final Label resultLabel = new Label();
    
    public NurseAssignmentUI(HospitalController controller, List<Nurse> nurses, List<Ward> wards) {
    	this.controller = controller;
    	setSpacing(10);
    	setPadding(new Insets(15));
    	
    	nurseBox.getItems().addAll(nurses);
    	wardBox.getItems().addAll(wards);
    	shiftField.setPromptText("e.g Morning");
    	
    	Button submitBtn = new Button("Assign Nurse");
        submitBtn.setOnAction(e -> assignNurse(nurseBox.getValue(), wardBox.getValue(), shiftField.getText()));
        
        getChildren().addAll(
        	new Label("Assign Nurse to Ward"),
        	new Label("Nurse: "), nurseBox,
        	new Label("Ward: "), wardBox,
        	new Label("Shift: "), shiftField,
        	submitBtn,
        	resultLabel
        );
    }
    
    // Admin assign Nurse
    public void assignNurse(Nurse nurse, Ward ward, String shift) {
        if (nurse == null || ward == null || shift == null || shift.isBlank()) {
            resultLabel.setText("Select a nurse, a ward, and enter a shift.");
            return;
        }
        boolean ok = controller.assignNurseToWard(nurse, ward, shift);
        if (ok) {
            resultLabel.setText(nurse.getName() + " assigned to " + ward.getWardName() + " (" + shift + ").");
        } else {
            resultLabel.setText("Conflict: " + nurse.getName() + " is already assigned elsewhere.");
        }
    }
}
