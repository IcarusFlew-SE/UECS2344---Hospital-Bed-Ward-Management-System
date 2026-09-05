package hospital.app;

import hospital.controller.HospitalController;
import hospital.model.Nurse;
import hospital.model.Ward;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

public class NurseAssignmentUI extends JPanel {
	private final HospitalController controller;
	private final JComboBox<Nurse> nurseBox = new JComboBox<>();
	private final JComboBox<Ward> wardBox = new JComboBox<>();
	private final JTextField shiftField = new JTextField();
	private final JLabel resultLabel = new JLabel(" ");

	public NurseAssignmentUI(HospitalController controller, List<Nurse> nurses, List<Ward> wards) {
		this.controller = controller;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		for (Nurse n : nurses) nurseBox.addItem(n);
		for (Ward w : wards) wardBox.addItem(w);
		nurseBox.setRenderer(nurseRenderer());
		wardBox.setRenderer(wardRenderer());
		shiftField.setToolTipText("e.g Morning");
		nurseBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, nurseBox.getPreferredSize().height));
		wardBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, wardBox.getPreferredSize().height));
		shiftField.setMaximumSize(new Dimension(Integer.MAX_VALUE, shiftField.getPreferredSize().height));

		JButton submitBtn = new JButton("Assign Nurse");
		submitBtn.addActionListener(e -> assignNurse(
				(Nurse) nurseBox.getSelectedItem(),
				(Ward) wardBox.getSelectedItem(),
				shiftField.getText()));

		add(label("Assign Nurse to Ward"));
		add(Box.createVerticalStrut(10));
		add(label("Nurse:"));
		add(nurseBox);
		add(Box.createVerticalStrut(8));
		add(label("Ward:"));
		add(wardBox);
		add(Box.createVerticalStrut(8));
		add(label("Shift:"));
		add(shiftField);
		add(Box.createVerticalStrut(10));
		add(submitBtn);
		add(Box.createVerticalStrut(8));
		add(resultLabel);
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
			return;
		}

		// UC04 alt flow 3a - warn the Admin, who then relocates the nurse or changes the selection
		int choice = JOptionPane.showConfirmDialog(this,
				nurse.getName() + " is already assigned to " + nurse.getAssignedWard().getWardName()
						+ ".\nRelocate to " + ward.getWardName() + " (" + shift + ")?",
				"Schedule Conflict", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (choice == JOptionPane.YES_OPTION) {
			controller.assignNurseToWard(nurse, ward, shift, true);
			resultLabel.setText(nurse.getName() + " relocated to " + ward.getWardName() + " (" + shift + ").");
		} else {
			resultLabel.setText("Assignment cancelled. " + nurse.getName() + " stays in "
					+ nurse.getAssignedWard().getWardName() + ".");
		}
	}

	private static JLabel label(String text) {
		JLabel l = new JLabel(text);
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		return l;
	}

	private static DefaultListCellRenderer nurseRenderer() {
		return new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof Nurse n) setText(n.getName());
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
