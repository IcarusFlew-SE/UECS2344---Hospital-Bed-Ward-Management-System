package hospital.app;

import hospital.controller.HospitalController;
import hospital.model.Bed;
import hospital.model.BedStatus;
import hospital.model.Nurse;
import hospital.model.Ward;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

// UC05 - Update Bed Status. Also covers UC06 (Nurse views own ward assignment) and
// UC10 (anyone views the full ward/bed overview), since all three are ward-information
// reads/writes and stay together per the Design CD Addendum.
public class WardStatusUI extends JPanel {
	private final HospitalController controller;
	private final List<Ward> wards;
	private final JComboBox<Ward> wardBox = new JComboBox<>();
	private final JComboBox<Bed> bedBox = new JComboBox<>();
	private final JComboBox<BedStatus> statusBox = new JComboBox<>();
	private final JLabel resultLabel = new JLabel(" ");

	private final JComboBox<Nurse> nurseBox = new JComboBox<>();
	private final JLabel myAssignmentLabel = new JLabel(" ");

	private final DefaultListModel<String> overviewModel = new DefaultListModel<>();
	private final JList<String> overviewView = new JList<>(overviewModel);

	public WardStatusUI(HospitalController controller, List<Ward> wards, List<Nurse> nurses) {
		this.controller = controller;
		this.wards = wards;
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

		// UC06 - Nurse checks their own current ward and shift assignment
		for (Nurse n : nurses) nurseBox.addItem(n);
		nurseBox.setRenderer(nurseRenderer());
		sizeCombo(nurseBox);
		nurseBox.addActionListener(e -> viewMyAssignment((Nurse) nurseBox.getSelectedItem()));

		// UC10 - full ward/bed overview
		overviewView.setVisibleRowCount(10);
		JButton refreshOverviewBtn = new JButton("Refresh Overview");
		refreshOverviewBtn.addActionListener(e -> viewAllWardStatus());
		viewAllWardStatus();

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

		add(Box.createVerticalStrut(20));
		add(new JSeparator());
		add(Box.createVerticalStrut(10));
		add(label("My Ward Assignment (Nurse)"));
		add(Box.createVerticalStrut(8));
		add(label("I am:"));
		add(nurseBox);
		add(Box.createVerticalStrut(8));
		add(myAssignmentLabel);

		add(Box.createVerticalStrut(20));
		add(new JSeparator());
		add(Box.createVerticalStrut(10));
		add(label("Track Ward & Bed Status (All Wards)"));
		add(Box.createVerticalStrut(8));
		add(refreshOverviewBtn);
		add(Box.createVerticalStrut(8));
		add(new JScrollPane(overviewView));
	}

	// UC10 - reload the beds of the selected ward so their current status is always shown
	public void refreshBeds() {
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
			resultLabel.setText("Select a bed and a status first.");
			return;
		}
		try {
			controller.updateBedStatus(bed, newStatus);
			resultLabel.setText(bed.getBedId() + " is now " + newStatus);
			refreshBeds();
			viewAllWardStatus();
		} catch (IllegalStateException ex) {
			// UC05 alt flow 3a - invalid status transition, the system informs the Nurse
			resultLabel.setText("Rejected: " + ex.getMessage());
		}
	}

	// UC06 Normal Flow - Nurse requests their current ward/shift assignment
	public void viewMyAssignment(Nurse nurse) {
		if (nurse == null) return;
		Ward assigned = nurse.getAssignedWard();
		myAssignmentLabel.setText(assigned == null
				? "No current assignment on record."
				: "Assigned to: " + assigned.getWardName() + " (shift: " + nurse.getShift() + ")");
	}

	// UC10 Normal Flow - Actor requests the ward/bed status overview for every ward
	public void viewAllWardStatus() {
		overviewModel.clear();
		for (Ward w : wards) {
			for (Bed b : w.getBeds()) {
				overviewModel.addElement(w.getWardName() + "  -  " + b.getBedId() + "  -  " + b.getStatus());
			}
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
}
