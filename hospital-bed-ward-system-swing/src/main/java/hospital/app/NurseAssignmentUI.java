package hospital.app;

import hospital.controller.HospitalController;
import hospital.data.HospitalDataStore;
import hospital.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// NurseAssignmentUI - assign nurses to wards and view assignments
public class NurseAssignmentUI extends JPanel {

    private final HospitalController controller;
    private final HospitalDataStore dataStore;

    private final JComboBox<Nurse> nurseBox = new JComboBox<>();
    private final JComboBox<Ward> wardBox = new JComboBox<>();
    private final JComboBox<String> shiftBox = new JComboBox<>();
    private final JLabel resultLbl = new JLabel(" ");

    // Assignment summary table
    private final String[] ASSIGN_COLS = {"Nurse", "Assigned Ward", "Shift", "Status"};
    private final DefaultTableModel assignModel = new DefaultTableModel(ASSIGN_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable assignTable = new JTable(assignModel);

    public NurseAssignmentUI(HospitalController controller, HospitalDataStore dataStore, List<Ward> wards) {
        this.controller = controller;
        this.dataStore = dataStore;

        setLayout(new BorderLayout(0, 12));
        setBackground(AppTheme.SURFACE);
        setBorder(AppTheme.pagePadding());

        // Form card
        // Nurses are loaded dynamically from the data store so newly created nurses appear
        reloadNurses();
        for (Ward w : wards) wardBox.addItem(w);
        shiftBox.addItem("Morning");
        shiftBox.addItem("Afternoon");
        shiftBox.addItem("Night");

        nurseBox.setRenderer(nurseRenderer());
        wardBox.setRenderer(wardRenderer());
        AppTheme.sizeCombo(nurseBox);
        AppTheme.sizeCombo(wardBox);
        AppTheme.sizeCombo(shiftBox);

        resultLbl.setFont(AppTheme.FONT_BODY);
        resultLbl.setForeground(AppTheme.TEXT_SECONDARY);

        JButton assignBtn  = AppTheme.primaryButton("👩‍⚕️  Assign Nurse");
        JButton refreshBtn = AppTheme.secondaryButton("↺  Refresh");
        assignBtn.addActionListener(e  -> assignNurse(
                (Nurse) nurseBox.getSelectedItem(),
                (Ward)  wardBox.getSelectedItem(),
                (String) shiftBox.getSelectedItem()));
        refreshBtn.addActionListener(e -> refreshAssignmentTable());

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 10);

        addRow(formGrid, gbc, 0, "Nurse:", nurseBox);
        addRow(formGrid, gbc, 1, "Ward:",  wardBox);
        addRow(formGrid, gbc, 2, "Shift:", shiftBox);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.insets = new Insets(12, 0, 0, 10);
        formGrid.add(assignBtn, gbc);
        gbc.gridx = 1; gbc.insets = new Insets(12, 0, 0, 10);
        formGrid.add(refreshBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.insets = new Insets(4, 0, 0, 0);
        formGrid.add(resultLbl, gbc);

        JPanel formCard = new JPanel(new BorderLayout());
        formCard.setBackground(AppTheme.CARD);
        formCard.setBorder(AppTheme.sectionBorder("Assign Nurse to Ward"));
        formCard.add(formGrid, BorderLayout.CENTER);

        // Assignment summary table
        AppTheme.styleTable(assignTable);
        JScrollPane tableScroll = new JScrollPane(assignTable);
        AppTheme.styleScrollPane(tableScroll);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(AppTheme.CARD);
        tableCard.setBorder(AppTheme.sectionBorder("Current Nurse Assignments"));
        tableCard.add(tableScroll, BorderLayout.CENTER);

        add(formCard,  BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);

        refreshAssignmentTable();
    }

    // Actions

    public void assignNurse(Nurse nurse, Ward ward, String shift) {
        if (nurse == null || ward == null || shift == null || shift.isBlank()) {
            showStatus("Select a nurse, a ward, and a shift.", false);
            return;
        }

        boolean ok = controller.assignNurseToWard(nurse, ward, shift);

        if (ok) {
            showStatus("✔  " + nurse.getName() + " assigned to " + ward.getWardName()
                    + " (" + shift + ").", true);
            refreshAssignmentTable();
            return;
        }

        // UC04 alt flow 3a – shift conflict
        String conflictMsg = nurse.getName() + " already has a " + nurse.getShift()
                + " shift at " + nurse.getAssignedWard().getWardName()
                + ".\nRelocate to " + ward.getWardName() + " (" + shift + ")?";
        int choice = JOptionPane.showConfirmDialog(this, conflictMsg,
                "Schedule Conflict", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            controller.assignNurseToWard(nurse, ward, shift, true);
            showStatus("✔  " + nurse.getName() + " relocated to "
                    + ward.getWardName() + " (" + shift + ").", true);
        } else {
            showStatus("Assignment cancelled. " + nurse.getName()
                    + " stays in " + nurse.getAssignedWard().getWardName() + ".", false);
        }
        refreshAssignmentTable();
    }

    public void refreshAssignmentTable() {
        reloadNurses();
        assignModel.setRowCount(0);
        for (Nurse n : dataStore.findAllNurses()) {
            Ward assigned = n.getAssignedWard();
            assignModel.addRow(new Object[]{
                n.getName(),
                assigned != null ? assigned.getWardName() : "— Unassigned —",
                n.getShift() != null ? n.getShift() : "—",
                n.isActive() ? "Active" : "Inactive"
            });
        }
    }

    // Reload nurse combo from live data store (picks up newly created nurses)
    private void reloadNurses() {
        Object prev = nurseBox.getSelectedItem();
        nurseBox.removeAllItems();
        for (Nurse n : dataStore.findAllNurses()) nurseBox.addItem(n);
        if (prev != null) nurseBox.setSelectedItem(prev);
    }

    // Helpers

    private void showStatus(String msg, boolean success) {
        resultLbl.setText(msg);
        resultLbl.setForeground(success ? AppTheme.SUCCESS : AppTheme.DANGER);
    }

    private static void addRow(JPanel p, GridBagConstraints gbc, int row,
                               String text, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        gbc.insets = new Insets(6, 0, 6, 10);
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_BODY.deriveFont(Font.BOLD));
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        p.add(field, gbc);
    }

    private static DefaultListCellRenderer nurseRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Nurse n)
                    setText(n.getName() + "  (" + n.getShift() + " shift)");
                setFont(AppTheme.FONT_BODY);
                return this;
            }
        };
    }

    private static DefaultListCellRenderer wardRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Ward w) setText(w.getWardName());
                setFont(AppTheme.FONT_BODY);
                return this;
            }
        };
    }
}