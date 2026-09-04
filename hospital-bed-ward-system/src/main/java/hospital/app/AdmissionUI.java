package hospital.app;

import hospital.controller.HospitalController;
import hospital.model.Admission;
import hospital.model.Patient;
import hospital.model.Ward;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.*;

// One purpose: admit patient
public class AdmissionUI extends VBox{
	private final HospitalController controller;
	private final ComboBox<Patient> patientBox = new ComboBox<>();
	private final ComboBox<Ward> wardBox = new ComboBox<>();
    private final Label resultLabel = new Label();
    
    public AdmissionUI(HospitalController controller, List<Patient> patients, List<Ward> wards) {
    	this.controller = controller;
    	setSpacing(10);
    	setPadding(new Insets(15));
    	
    	patientBox.getItems().addAll(patients);
    	wardBox.getItems().addAll(wards);
    	
    	Button submitBtn = new Button("Admit Patient");
    	submitBtn.setOnAction(e -> submitAdmission(patientBox.getValue(), wardBox.getValue()));
    	
    	getChildren().addAll(
    		new Label("Admit Patient"),
    		new Label("Patient: "), patientBox,
    		new Label("Ward: "), wardBox,
    		submitBtn,
    		resultLabel
    	);
    }
	
    // Doctor use AdmissionUI: submitAdmission
    public void submitAdmission(Patient patient, Ward ward) {
        if (patient == null || ward == null) {
            resultLabel.setText("Select a patient and a ward first.");
            return;
        }
        Admission result = controller.admitPatient(patient, ward);
        if (result != null) {
            resultLabel.setText("Admitted. Bed assigned: " + result.getBed().getBedId());
        } else {
            resultLabel.setText("No bed available in " + ward.getWardName() + ".");
        }
    }
}
