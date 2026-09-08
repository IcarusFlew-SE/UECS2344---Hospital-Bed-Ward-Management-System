package hospital.app;

import hospital.controller.HospitalController;
import hospital.data.HospitalDataStore;
import hospital.model.Admission;
import hospital.model.Patient;
import hospital.model.User;
import hospital.model.Ward;

import javax.swing.*;
import java.awt.*;
import java.util.List;

// UC02 - Transfer Patient. The Doctor picks an active admission and a destination ward.
public class TransferUI extends JPanel {
	private final HospitalController controller;
	private final HospitalDataStore dataStore;
	private final JComboBox<Admission> admissionBox = new JComboBox<>();
	private final JComboBox<Ward> wardBox = new JComboBox<>();
	private final JLabel resultLabel = new JLabel(" ");
	private final JButton refreshBtn = new JButton("Refresh Admissions");
	private final JButton submitBtn = new JButton("Transfer Patient");
	private final JButton cancelBtn = new JButton("Cancel Admission");
	private final JButton dischargeBtn = new JButton("Discharge Patient");

	private final JComboBox<Patient> myAdmissionPatientBox = new JComboBox<>();
	private final DefaultListModel<String> myAdmissionModel = new DefaultListModel<>();
	private final JList<String> myAdmissionView = new JList<>(myAdmissionModel);

	public TransferUI(HospitalController controller, HospitalDataStore dataStore, List<Ward> wards, List<Patient> patients) {
		this.controller = controller;
		this.dataStore = dataStore;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		for (Ward w : wards) wardBox.addItem(w);
		admissionBox.setRenderer(admissionRenderer());
		wardBox.setRenderer(wardRenderer());
		sizeCombo(admissionBox);
		sizeCombo(wardBox);

		refreshBtn.addActionListener(e -> loadActiveAdmissions());

		submitBtn.addActionListener(e -> submitTransfer(
				(Admission) admissionBox.getSelectedItem(),
				(Ward) wardBox.getSelectedItem()));

		// UC01 flow 6a - cancel an active admission before it is discharged
		cancelBtn.addActionListener(e -> cancelAdmission((Admission) admissionBox.getSelectedItem()));

		// UC01 - discharge: the normal, successful conclusion of an admission
		dischargeBtn.addActionListener(e -> dischargePatient((Admission) admissionBox.getSelectedItem()));

		for (Patient p : patients) myAdmissionPatientBox.addItem(p);
		myAdmissionPatientBox.setRenderer(patientRenderer());
		sizeCombo(myAdmissionPatientBox);
		myAdmissionPatientBox.addActionListener(e -> viewMyAdmission((Patient) myAdmissionPatientBox.getSelectedItem()));
		myAdmissionView.setVisibleRowCount(6);

		add(label("Transfer / Discharge / Cancel"));
		add(Box.createVerticalStrut(10));
		add(label("Active Admission:"));
		add(admissionBox);
		add(Box.createVerticalStrut(4));
		add(refreshBtn);
		add(Box.createVerticalStrut(8));
		add(label("Destination Ward:"));
		add(wardBox);
		add(Box.createVerticalStrut(10));
		JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
		actionRow.add(submitBtn);
		actionRow.add(dischargeBtn);
		actionRow.add(cancelBtn);
		add(actionRow);
		add(Box.createVerticalStrut(8));
		add(resultLabel);

		add(Box.createVerticalStrut(20));
		add(new JSeparator());
		add(Box.createVerticalStrut(10));
		add(label("View My Admission (Patient)"));
		add(Box.createVerticalStrut(8));
		add(label("I am:"));
		add(myAdmissionPatientBox);
		add(Box.createVerticalStrut(8));
		add(new JScrollPane(myAdmissionView));

		loadActiveAdmissions();
	}

	private void dischargePatient(Admission selectedItem) {
		if (selectedItem == null) {
			resultLabel.setText("Select an admission to discharge.");
			return;
		}
		try {
			controller.dischargePatient(selectedItem);
			resultLabel.setText("Patient " + selectedItem.getPatient().getName() + " discharged, bed released.");
			loadActiveAdmissions();
		} catch (IllegalStateException ex) {
			resultLabel.setText("Cannot discharge: " + ex.getMessage());
		}
	}

	// RBAC - enable only the sections the currently acting user's role is permitted to use,
	// per the use case document's actor lists (UC01/UC02 = Doctor, UC07 = Patient)
	public void applyPermissions(User currentUser) {
		boolean canManageAdmissions = currentUser != null
				&& (currentUser.getPermissions().contains("TRANSFER_PATIENT")
					|| currentUser.getPermissions().contains("ADMIT_PATIENT"));
		submitBtn.setEnabled(canManageAdmissions);
		dischargeBtn.setEnabled(canManageAdmissions);
		cancelBtn.setEnabled(canManageAdmissions);
		wardBox.setEnabled(canManageAdmissions);

		boolean canViewOwn = currentUser != null
				&& currentUser.getPermissions().contains("VIEW_OWN_ADMISSION");
		myAdmissionPatientBox.setEnabled(canViewOwn);
	}

	// Doctor selects the Patient's active Admission
	public void loadActiveAdmissions() {
		admissionBox.removeAllItems();
		for (Admission a : dataStore.findActiveAdmissions()) {
			admissionBox.addItem(a);
		}
		if (admissionBox.getItemCount() == 0) {
			resultLabel.setText("No active admissions to transfer.");
		} else if (resultLabel.getText().startsWith("No active admissions")) {
			resultLabel.setText(" ");
		}
	}

	// Doctor confirms the transfer; the controller records it and updates both beds
	public void submitTransfer(Admission admission, Ward destination) {
		if (admission == null || destination == null) {
			resultLabel.setText("Select an active admission and a destination ward.");
			return;
		}
		if (admission.getWard() == destination) {
			resultLabel.setText("Patient is already in " + destination.getWardName() + ".");
			return;
		}

		boolean ok = controller.transferPatient(admission, destination);
		if (ok) {
			resultLabel.setText(admission.getPatient().getName() + " transferred to "
					+ destination.getWardName() + ", bed " + admission.getBed().getBedId() + ".");
			loadActiveAdmissions();
		} else {
			// UC02 alt flow 3a - destination ward is at full capacity
			resultLabel.setText(destination.getWardName() + " is at full capacity. Select another ward.");
		}
	}

	// UC01 flow 6a - Doctor/Admin cancels an active admission before discharge
	public void cancelAdmission(Admission admission) {
		if (admission == null) {
			resultLabel.setText("Select an admission to cancel.");
			return;
		}
		try {
			controller.cancelAdmission(admission);
			resultLabel.setText("Admission " + admission.getAdmissionId() + " cancelled, bed released.");
			loadActiveAdmissions();
		} catch (IllegalStateException ex) {
			resultLabel.setText("Cannot cancel: " + ex.getMessage());
		}
	}

	// UC07 - Patient requests their own admission details, active or past
	public void viewMyAdmission(Patient patient) {
		myAdmissionModel.clear();
		if (patient == null) return;
		List<Admission> mine = dataStore.findAdmissionsByPatient(patient.getUserId());
		if (mine.isEmpty()) {
			myAdmissionModel.addElement("No admission records found.");
			return;
		}
		for (Admission a : mine) {
			myAdmissionModel.addElement(a.getAdmissionId() + " - " + a.getStatus()
					+ " - Ward: " + a.getWard().getWardName() + " - Bed: " + a.getBed().getBedId());
		}
	}

	private static void sizeCombo(JComboBox<?> combo) {
		combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, combo.getPreferredSize().height));
	}

	private static JLabel label(String text) {
		JLabel l = new JLabel(text);
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		return l;
	}

	private static DefaultListCellRenderer admissionRenderer() {
		return new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof Admission a) {
					setText(a.getPatient().getName() + " - " + a.getWard().getWardName()
							+ " / bed " + a.getBed().getBedId() + " (" + a.getStatus() + ")");
				}
				return this;
			}
		};
	}

	private static DefaultListCellRenderer wardRenderer() {
		return new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof Ward w) {
					setText(w.getWardName() + (w.isAtCapacity() ? " (full)" : ""));
				}
				return this;
			}
		};
	}

	private static DefaultListCellRenderer patientRenderer() {
		return new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof Patient p) setText(p.getName());
				return this;
			}
		};
	}
}