package hospital.app;

import hospital.controller.HospitalController;
import hospital.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// AdmissionUI - admit patients and view current admissions
public class AdmissionUI extends JPanel {

    private final HospitalController controller;
    private final List<Patient> patients;

    private final JComboBox<Patient> patientBox = new JComboBox<>();
    private final JComboBox<Doctor>  doctorBox  = new JComboBox<>();
    private final JComboBox<Ward>    wardBox    = new JComboBox<>();
    private final JLabel             resultLabel = new JLabel(" ");

    // Active-admissions mini-table
    private final String[] ADMIT_COLS = {"Admission ID", "Patient", "Doctor", "Ward", "Bed", "Status", "Date"};
    private final DefaultTableModel admitTableModel = new DefaultTableModel(ADMIT_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable admitTable = new JTable(admitTableModel);

    public AdmissionUI(HospitalController controller, List<Patient> patients,
                       List<Doctor> doctors, List<Ward> wards) {
        this.controller = controller;
        this.patients   = patients;

        setLayout(new BorderLayout(0, 12));
        setBackground(AppTheme.SURFACE);
        setBorder(AppTheme.pagePadding());

        // ── Top: admit form ───────────────────────────────────────────────────
        JPanel formCard = buildFormCard(doctors, wards);

        // ── Bottom: admissions table ──────────────────────────────────────────
        AppTheme.styleTable(admitTable);
        // Colour the Status column by bed-status colours
        for (int col = 0; col < ADMIT_COLS.length; col++)
            admitTable.getColumnModel().getColumn(col).setCellRenderer(AppTheme.bedStatusRenderer(5));

        JScrollPane tableScroll = new JScrollPane(admitTable);
        AppTheme.styleScrollPane(tableScroll);
        tableScroll.setPreferredSize(new Dimension(0, 180));

        JPanel tableCard = new JPanel(new BorderLayout(0, 6));
        tableCard.setBackground(AppTheme.CARD);
        tableCard.setBorder(AppTheme.sectionBorder("Current Active Admissions (read-only overview)"));
        tableCard.add(tableScroll, BorderLayout.CENTER);

        add(formCard,  BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    // Form card builder

    private JPanel buildFormCard(List<Doctor> doctors, List<Ward> wards) {
        for (Patient p : patients) patientBox.addItem(p);
        for (Doctor  d : doctors)  doctorBox.addItem(d);
        for (Ward    w : wards)    wardBox.addItem(w);

        patientBox.setRenderer(userRenderer("Patient"));
        doctorBox.setRenderer(userRenderer("Doctor"));
        wardBox.setRenderer(wardRenderer());

        AppTheme.sizeCombo(patientBox);
        AppTheme.sizeCombo(doctorBox);
        AppTheme.sizeCombo(wardBox);

        resultLabel.setFont(AppTheme.FONT_BODY);
        resultLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JButton submitBtn = AppTheme.primaryButton("🏥  Admit Patient");
        submitBtn.addActionListener(e -> submitAdmission(
                (Patient) patientBox.getSelectedItem(),
                (Doctor)  doctorBox.getSelectedItem(),
                (Ward)    wardBox.getSelectedItem()));

        // Form layout using GridBagLayout for two-column look
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 10);

        addRow(grid, gbc, 0, "Patient:",          patientBox);
        addRow(grid, gbc, 1, "Admitting Doctor:",  doctorBox);
        addRow(grid, gbc, 2, "Target Ward:",       wardBox);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        gbc.insets = new Insets(12, 0, 0, 10);
        grid.add(submitBtn, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        gbc.insets = new Insets(14, 0, 0, 0);
        grid.add(resultLabel, gbc);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppTheme.CARD);
        card.setBorder(AppTheme.sectionBorder("Admit Patient"));
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // Submit action

    // Called by the button and also from RBAC-aware callers.
    public void submitAdmission(Patient patient, Ward ward) {
        submitAdmission(patient, null, ward);
    }

    public void submitAdmission(Patient patient, Doctor doctor, Ward ward) {
        if (patient == null || ward == null) {
            showStatus("Select a patient and a ward first.", false);
            JOptionPane.showMessageDialog(this, "Select a patient and a ward first.",
                    "Input Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Admission result = controller.admitPatient(patient, doctor, ward);
            if (result != null) {
                String msg = "✔  Admitted. Bed assigned: " + result.getBed().getBedId();
                showStatus(msg, true);
                JOptionPane.showMessageDialog(this, msg, "Admission Confirmed",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshAdmitTable();
            } else {
                String msg = "No bed available in " + ward.getWardName() + ". Select another ward.";
                showStatus(msg, false);
                JOptionPane.showMessageDialog(this, msg, "No Beds Available",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalStateException ex) {
            showStatus(ex.getMessage(), false);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Admission Conflict",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // Refresh the mini-table of current admissions
    public void refreshAdmitTable() {
        admitTableModel.setRowCount(0);
        // Table is populated lazily by TransferUI in this demo.
    }

    // Allows admission-table rows to be added by the caller
    public DefaultTableModel getAdmitTableModel() { return admitTableModel; }

    // Helpers
    private void showStatus(String msg, boolean success) {
        resultLabel.setText(msg);
        resultLabel.setForeground(success ? AppTheme.SUCCESS : AppTheme.DANGER);
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc, int row,
                               String labelText, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        gbc.insets = new Insets(6, 0, 6, 10);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(AppTheme.FONT_BODY.deriveFont(Font.BOLD));
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private static <T extends User> DefaultListCellRenderer userRenderer(String type) {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User u) setText(u.getName());
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
                if (value instanceof Ward w)
                    setText(w.getWardName() + "  [" + w.getWardType() + "]"
                            + (w.isAtCapacity() ? "  ⛔ FULL" : ""));
                setFont(AppTheme.FONT_BODY);
                return this;
            }
        };
    }
}