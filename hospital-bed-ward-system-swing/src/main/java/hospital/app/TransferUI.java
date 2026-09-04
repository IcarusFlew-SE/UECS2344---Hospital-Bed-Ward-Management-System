package hospital.app;

import hospital.controller.HospitalController;
import hospital.data.HospitalDataStore;
import hospital.model.Admission;
import hospital.model.Ward;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

// UC02 - Transfer Patient. The Doctor picks an active admission and a destination ward.
public class TransferUI extends JPanel {
	private final HospitalController controller;
	private final HospitalDataStore dataStore;
	private final JComboBox<Admission> admissionBox = new JComboBox<>();
	private final JComboBox<Ward> wardBox = new JComboBox<>();
	private final JLabel resultLabel = new JLabel(" ");

	public TransferUI(HospitalController controller, HospitalDataStore dataStore, List<Ward> wards) {
		this.controller = controller;
		this.dataStore = dataStore;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		for (Ward w : wards) wardBox.addItem(w);
		admissionBox.setRenderer(admissionRenderer());
		wardBox.setRenderer(wardRenderer());
		sizeCombo(admissionBox);
		sizeCombo(wardBox);

		JButton refreshBtn = new JButton("Refresh Admissions");
		refreshBtn.addActionListener(e -> loadActiveAdmissions());

		JButton submitBtn = new JButton("Transfer Patient");
		submitBtn.addActionListener(e -> submitTransfer(
				(Admission) admissionBox.getSelectedItem(),
				(Ward) wardBox.getSelectedItem()));

		add(label("Transfer Patient"));
		add(Box.createVerticalStrut(10));
		add(label("Active Admission:"));
		add(admissionBox);
		add(Box.createVerticalStrut(4));
		add(refreshBtn);
		add(Box.createVerticalStrut(8));
		add(label("Destination Ward:"));
		add(wardBox);
		add(Box.createVerticalStrut(10));
		add(submitBtn);
		add(Box.createVerticalStrut(8));
		add(resultLabel);

		loadActiveAdmissions();
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
}
