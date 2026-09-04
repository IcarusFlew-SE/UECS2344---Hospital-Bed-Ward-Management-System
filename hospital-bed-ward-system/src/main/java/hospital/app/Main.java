package hospital.app;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;

import hospital.controller.HospitalController;
import hospital.data.*;
import hospital.model.*;

import java.util.*;
import java.time.LocalDate;

public class Main extends Application {

	@Override
	public void start(Stage stage) {
		HospitalDataStore ds = new HospitalDataStore();
		HospitalController hc = new HospitalController(ds);
		
		seedSampleData(ds);
		
		List<Patient> patients = new ArrayList<>();
        List<Nurse> nurses = new ArrayList<>();
        for (User u : ds.findAllUsers()) {
            if (u instanceof Patient p) patients.add(p);
            if (u instanceof Nurse n) nurses.add(n);
        }
        List<Ward> wards = ds.findAllWards();
        
        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Admit Patient", new AdmissionUI(hc, patients, wards)));
        tabs.getTabs().add(new Tab("Ward Status", new WardStatusUI(hc, wards)));
        tabs.getTabs().add(new Tab("Reports", new ReportUI(hc)));
        tabs.getTabs().add(new Tab("Assign Nurse", new NurseAssignmentUI(hc, nurses, wards)));
        tabs.getTabs().add(new Tab("Notifications", new NotificationUI(ds, "U3")));
        tabs.getTabs().forEach(t -> t.setClosable(false));

        stage.setScene(new Scene(tabs, 800, 600));
        stage.setTitle("Hospital Bed & Ward Management System");
        stage.show();
	}
	
	private void seedSampleData(HospitalDataStore dataStore) {
		Bed b1 = new Bed("B1");
		Bed b2 = new Bed("B2");
		Ward general = new Ward("W1", "General Ward", 2, "General", List.of(b1, b2));
		
		Bed b3 = new Bed("B3");
		Ward icu = new Ward("W2", "ICU", 1, "Intensive Care", List.of(b3));
		
		dataStore.saveWard(general);
		dataStore.saveWard(icu);
		
        dataStore.saveUser(new Patient("P1", "Ali Bin Ahmad", "0123456789", "ali@mail.com", LocalDate.of(1990, 1, 1)));
        dataStore.saveUser(new Patient("P2", "Mei Ling", "0121111111", "mei@mail.com", LocalDate.of(1985, 5, 5)));
        dataStore.saveUser(new Nurse("U2", "Ben Lee", "0129999999", "ben@mail.com", "Morning"));
        dataStore.saveUser(new Doctor("U1", "Dr. Alice Tan", "0126666666", "alice@mail.com", "Medicine", "General"));
        dataStore.saveUser(new Admin("U3", "Chloe Wong", "0127777777", "chloe@mail.com", "Full"));
	}
	
	public static void main(String[] args) {
        launch(args);
    }
}
