package hospital.app;

import java.util.*;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.*;

import hospital.controller.HospitalController;
import hospital.model.Admission;
import hospital.model.Doctor;
import hospital.model.Patient;
import hospital.model.User;
import hospital.model.Ward;

// One purpose: admit patient
public class AdmissionUI extends JPanel {
	private final HospitalController controller;
	private final JComboBox<Patient> patientBox = new JComboBox<>();
	private final JComboBox<Doctor> doctorBox = new JComboBox<>();
	private final JComboBox<Ward> wardBox = new JComboBox<>();
	private final JLabel resultLabel = new JLabel(" ");

	public AdmissionUI(HospitalController controller, List<Patient> patients,
			List<Doctor> doctors, List<Ward> wards) {
		this.controller = controller;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		for (Patient p : patients) patientBox.addItem(p);
		for (Doctor d : doctors) doctorBox.addItem(d);
		for (Ward w : wards) wardBox.addItem(w);
		patientBox.setRenderer(namedUserRenderer());
		doctorBox.setRenderer(namedUserRenderer());
		wardBox.setRenderer(wardRenderer());
		patientBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, patientBox.getPreferredSize().height));
		doctorBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, doctorBox.getPreferredSize().height));
		wardBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, wardBox.getPreferredSize().height));

		JButton submitBtn = new JButton("Admit Patient");
		// UC01 - the admitting Doctor is recorded on the Admission ("Doctor manages Admission")
		submitBtn.addActionListener(e -> submitAdmission(
				(Patient) patientBox.getSelectedItem(),
				(Doctor) doctorBox.getSelectedItem(),
				(Ward) wardBox.getSelectedItem()));

		add(label("Admit Patient"));
		add(Box.createVerticalStrut(10));
		add(label("Patient:"));
		add(patientBox);
		add(Box.createVerticalStrut(8));
		add(label("Admitting Doctor:"));
		add(doctorBox);
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
		submitAdmission(patient, null, ward);
	}

	// Overload for submitAdmission
	public void submitAdmission(Patient patient, Doctor doctor, Ward ward) {
		if (patient == null || ward == null) {
			resultLabel.setText("Select a patient and a ward first.");
			JOptionPane.showMessageDialog(this, "Select a patient and a ward first.", "Input Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			Admission result = controller.admitPatient(patient, doctor, ward);
			if (result != null) {
				String msg = "Admitted. Bed assigned: " + result.getBed().getBedId();
				resultLabel.setText(msg);
                JOptionPane.showMessageDialog(this, msg, "Admission Confirmed", JOptionPane.INFORMATION_MESSAGE);
            } else {
				String msg = "No bed available in " + ward.getWardName() + ". Select another ward.";
                resultLabel.setText(msg);
                JOptionPane.showMessageDialog(this, msg, "No Beds Available", JOptionPane.ERROR_MESSAGE);
			}
		} catch (IllegalStateException ex) {
			resultLabel.setText(ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Admission Conflict", JOptionPane.WARNING_MESSAGE);
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
				if (value instanceof User u) setText(u.getName());
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
