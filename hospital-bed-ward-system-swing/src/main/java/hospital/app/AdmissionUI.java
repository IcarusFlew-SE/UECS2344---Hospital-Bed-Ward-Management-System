package hospital.app;

import hospital.controller.HospitalController;
import hospital.model.Admission;
import hospital.model.Patient;
import hospital.model.Ward;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

// One purpose: admit patient
public class AdmissionUI extends JPanel {
	private final HospitalController controller;
	private final JComboBox<Patient> patientBox = new JComboBox<>();
	private final JComboBox<Ward> wardBox = new JComboBox<>();
	private final JLabel resultLabel = new JLabel(" ");

	public AdmissionUI(HospitalController controller, List<Patient> patients, List<Ward> wards) {
		this.controller = controller;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		for (Patient p : patients) patientBox.addItem(p);
		for (Ward w : wards) wardBox.addItem(w);
		patientBox.setRenderer(namedUserRenderer());
		wardBox.setRenderer(wardRenderer());
		patientBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, patientBox.getPreferredSize().height));
		wardBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, wardBox.getPreferredSize().height));

		JButton submitBtn = new JButton("Admit Patient");
		submitBtn.addActionListener(e -> submitAdmission(
				(Patient) patientBox.getSelectedItem(),
				(Ward) wardBox.getSelectedItem()));

		add(label("Admit Patient"));
		add(Box.createVerticalStrut(10));
		add(label("Patient:"));
		add(patientBox);
		add(Box.createVerticalStrut(8));
		add(label("Ward:"));
		add(wardBox);
		add(Box.createVerticalStrut(10));
		add(submitBtn);
		add(Box.createVerticalStrut(8));
		add(resultLabel);
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

	private static JLabel label(String text) {
		JLabel l = new JLabel(text);
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		return l;
	}

	private static DefaultListCellRenderer namedUserRenderer() {
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

	private static DefaultListCellRenderer wardRenderer() {
		return new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof Ward w) setText(w.getWardName());
				return this;
			}
		};
	}
}
