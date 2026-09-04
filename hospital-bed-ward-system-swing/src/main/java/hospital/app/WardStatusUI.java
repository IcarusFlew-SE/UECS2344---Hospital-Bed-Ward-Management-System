package hospital.app;

import hospital.controller.HospitalController;
import hospital.model.Bed;
import hospital.model.BedStatus;
import hospital.model.Ward;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

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

		wardBox.addActionListener(e -> {
			bedBox.removeAllItems();
			Ward selected = (Ward) wardBox.getSelectedItem();
			if (selected != null) {
				for (Bed bed : selected.getBeds()) bedBox.addItem(bed);
			}
		});
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

	// Nurse use WardStatusUI: select bed for bed status
	public void selectBed(Bed bed, BedStatus newStatus) {
		if (bed == null || newStatus == null) {
			resultLabel.setText("Select a bed and a status first.");
			return;
		}
		try {
			controller.updateBedStatus(bed, newStatus);
			resultLabel.setText(bed.getBedId() + " is now " + newStatus);
		} catch (IllegalStateException ex) {
			resultLabel.setText("Invalid transition: " + ex.getMessage());
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
				if (value instanceof Bed b) setText(b.getBedId());
				return this;
			}
		};
	}
}
