package hospital.app;

import javax.swing.*;
import java.awt.Component;

import hospital.controller.HospitalController;
import hospital.data.HospitalDataStore;
import hospital.model.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class Main {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(Main::createAndShow);
	}

	private static void createAndShow() {
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

		TransferUI transferUI = new TransferUI(hc, ds, wards);
		WardStatusUI wardStatusUI = new WardStatusUI(hc, wards);
		NotificationUI notificationUI = new NotificationUI(ds, "U3");

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Admit Patient", new AdmissionUI(hc, patients, wards));
		tabs.addTab("Transfer Patient", transferUI);
		tabs.addTab("Ward Status", wardStatusUI);
		tabs.addTab("Reports", new ReportUI(hc));
		tabs.addTab("Assign Nurse", new NurseAssignmentUI(hc, nurses, wards));
		tabs.addTab("Notifications", notificationUI);

		// Admissions, bed statuses and notifications all change on other tabs, so each of these
		// screens reloads whatever it shows the moment the user switches to it.
		tabs.addChangeListener(e -> {
			Component selected = tabs.getSelectedComponent();
			if (selected == transferUI) transferUI.loadActiveAdmissions();
			if (selected == wardStatusUI) wardStatusUI.refreshBeds();
			if (selected == notificationUI) notificationUI.viewNotifications();
		});

		JFrame frame = new JFrame("Hospital Bed & Ward Management System");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(tabs);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private static void seedSampleData(HospitalDataStore dataStore) {
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
}
