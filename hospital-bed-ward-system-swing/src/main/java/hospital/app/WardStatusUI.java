package hospital.app;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import hospital.controller.HospitalController;
import hospital.model.Bed;
import hospital.model.BedStatus;
import hospital.model.Ward;

public class WardStatusUI extends JPanel {
	private final HospitalController controller;
	private final JComboBox<Ward> wardBox = new JComboBox<>();
	private final JComboBox<Bed> bedBox = new JComboBox<>();
	private final JComboBox<BedStatus> statusBox = new JComboBox<>();
	private final JLabel resultLabel = new JLabel(" ");

	public WardStatusUI(HospitalController controller, List<Ward> wards) {
		this.controller = controller;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		for (Ward w : wards) wardBox.addItem(w);
		wardBox.setRenderer(wardRenderer());
		bedBox.setRenderer(bedRenderer());
		for (BedStatus s : BedStatus.values()) statusBox.addItem(s);

		wardBox.addActionListener(e -> refreshBeds());
		if (wardBox.getItemCount() > 0) {
			wardBox.setSelectedIndex(0);
		}

		sizeCombo(wardBox);
		sizeCombo(bedBox);
		sizeCombo(statusBox);

		JButton submitBtn = new JButton("Update Status");
		submitBtn.addActionListener(e -> selectBed(
				(Bed) bedBox.getSelectedItem(),
				(BedStatus) statusBox.getSelectedItem()));

		add(label("Update Bed Status"));
		add(Box.createVerticalStrut(10));
		add(label("Ward:"));
		add(wardBox);
		add(Box.createVerticalStrut(8));
		add(label("Bed:"));
		add(bedBox);
		add(Box.createVerticalStrut(8));
		add(label("New Status:"));
		add(statusBox);
		add(Box.createVerticalStrut(10));
		add(submitBtn);
		add(Box.createVerticalStrut(8));
		add(resultLabel);
	}

	// UC10 - reload the beds of the selected ward so their current status is always shown
	public final void refreshBeds() {
		Ward selected = (Ward) wardBox.getSelectedItem();
		Bed previous = (Bed) bedBox.getSelectedItem();
		bedBox.removeAllItems();
		if (selected == null) return;
		for (Bed bed : selected.getBeds()) bedBox.addItem(bed);
		if (previous != null) bedBox.setSelectedItem(previous);
	}

	// Nurse use WardStatusUI: select bed for bed status
	public void selectBed(Bed bed, BedStatus newStatus) {
		if (bed == null || newStatus == null) {
			String msg = "Select a bed and a status first.";
			resultLabel.setText(msg);
			JOptionPane.showMessageDialog(this, msg, "Input Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			controller.updateBedStatus(bed, newStatus);
			String msg = bed.getBedId() + " is now " + newStatus;
			resultLabel.setText(msg);
			JOptionPane.showMessageDialog(this, msg, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
			refreshBeds();
		} catch (IllegalStateException ex) {
			// UC05 alt flow 3a - invalid status transition, the system informs the Nurse
			String msg = "Rejected: " + ex.getMessage();
			resultLabel.setText(msg);
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Rejected", JOptionPane.ERROR_MESSAGE);
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

	private static DefaultListCellRenderer bedRenderer() {
		return new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof Bed b) setText(b.getBedId() + "  -  " + b.getStatus());
				return this;
			}
		};
	}
}
