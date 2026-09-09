package hospital.app;

import hospital.controller.HospitalController;
import hospital.data.HospitalDataStore;
import hospital.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// TransferUI - transfer, discharge/cancel and patient admission history
public class TransferUI extends JPanel {

    private final HospitalController controller;
    private final HospitalDataStore  dataStore;
    private final List<Ward>         wards;

    // Active-admissions table
    private final String[] ACT_COLS = {"Admission ID", "Patient", "Doctor", "Ward", "Bed", "Status"};
    private final DefaultTableModel activeModel = new DefaultTableModel(ACT_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable activeTable = new JTable(activeModel);

    private final JComboBox<Ward> wardBox    = new JComboBox<>();
    private final JLabel          statusLbl  = new JLabel(" ");

    // Patient history table
    private final JComboBox<Patient> patientBox = new JComboBox<>();
    private final String[] HIST_COLS = {"Admission ID", "Ward", "Bed", "Status", "Date", "Discharge"};
    private final DefaultTableModel histModel = new DefaultTableModel(HIST_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable histTable = new JTable(histModel);

    public TransferUI(HospitalController controller, HospitalDataStore dataStore,
                      List<Ward> wards, List<Patient> patients) {
        this.controller = controller;
        this.dataStore = dataStore;
        this.wards = wards;

        setLayout(new BorderLayout(0, 12));
        setBackground(AppTheme.SURFACE);
        setBorder(AppTheme.pagePadding());

        // Top section: transfer / discharge / cancel
        JPanel topCard = buildTopCard();

        // Bottom section: patient admission history
        for (Patient p : patients) patientBox.addItem(p);
        patientBox.setRenderer(patientRenderer());
        AppTheme.sizeCombo(patientBox);
        patientBox.addActionListener(e -> viewMyAdmission((Patient) patientBox.getSelectedItem()));

        AppTheme.styleTable(histTable);
        for (int c = 0; c < HIST_COLS.length; c++)
            histTable.getColumnModel().getColumn(c).setCellRenderer(AppTheme.bedStatusRenderer(3));

        JScrollPane histScroll = new JScrollPane(histTable);
        AppTheme.styleScrollPane(histScroll);

        JPanel histCard = new JPanel(new BorderLayout(0, 8));
        histCard.setBackground(AppTheme.CARD);
        histCard.setBorder(AppTheme.sectionBorder("My Admission History"));

        JPanel histTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        histTop.setOpaque(false);
        histTop.add(AppTheme.bodyLabel("Select patient: "));
        histTop.add(patientBox);
        histCard.add(histTop, BorderLayout.NORTH);
        histCard.add(histScroll, BorderLayout.CENTER);

        add(topCard, BorderLayout.CENTER);
        add(histCard, BorderLayout.SOUTH);

        loadActiveAdmissions();
        if (patientBox.getItemCount() > 0) viewMyAdmission((Patient) patientBox.getItemAt(0));
    }

    // Top card
    private JPanel buildTopCard() {
        AppTheme.styleTable(activeTable);
        for (int c = 0; c < ACT_COLS.length; c++)
            activeTable.getColumnModel().getColumn(c).setCellRenderer(AppTheme.bedStatusRenderer(5));
        activeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScroll = new JScrollPane(activeTable);
        AppTheme.styleScrollPane(tableScroll);
        tableScroll.setPreferredSize(new Dimension(0, 160));

        for (Ward w : wards) wardBox.addItem(w);
        wardBox.setRenderer(wardRenderer());
        AppTheme.sizeCombo(wardBox);

        JButton transferBtn = AppTheme.primaryButton("🔄  Transfer");
        JButton dischargeBtn = AppTheme.secondaryButton("✅  Discharge");
        JButton cancelBtn = AppTheme.dangerButton("✖  Cancel Admission");
        JButton refreshBtn = AppTheme.secondaryButton("↺  Refresh");

        transferBtn.addActionListener(e -> submitTransfer(selectedAdmission(), (Ward) wardBox.getSelectedItem()));
        dischargeBtn.addActionListener(e -> dischargePatient(selectedAdmission()));
        cancelBtn.addActionListener(e -> cancelAdmission(selectedAdmission()));
        refreshBtn.addActionListener(e -> loadActiveAdmissions());

        statusLbl.setFont(AppTheme.FONT_BODY);
        statusLbl.setForeground(AppTheme.TEXT_SECONDARY);

        // Action row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);
        actionRow.add(transferBtn);
        actionRow.add(dischargeBtn);
        actionRow.add(cancelBtn);
        actionRow.add(Box.createHorizontalStrut(20));
        actionRow.add(refreshBtn);

        // Ward selector row
        JPanel wardRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wardRow.setOpaque(false);
        wardRow.add(AppTheme.bodyLabel("Transfer destination: "));
        wardRow.add(Box.createHorizontalStrut(8));
        wardBox.setPreferredSize(new Dimension(200, 26));
        wardRow.add(wardBox);

        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 6, 0);
        bottom.add(wardRow, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 6, 0);
        bottom.add(actionRow, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 0, 0);
        bottom.add(statusLbl, gbc);

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(AppTheme.CARD);
        card.setBorder(AppTheme.sectionBorder("Transfer / Discharge / Cancel"));
        card.add(tableScroll, BorderLayout.CENTER);
        card.add(bottom,      BorderLayout.SOUTH);
        return card;
    }

    // Public actions
    public void loadActiveAdmissions() {
        activeModel.setRowCount(0);
        List<Admission> actives = dataStore.findActiveAdmissions();
        for (Admission a : actives) {
            activeModel.addRow(new Object[]{
                a.getAdmissionId(),
                a.getPatient().getName(),
                a.getDoctor() != null ? a.getDoctor().getName() : "—",
                a.getWard().getWardName(),
                a.getBed().getBedId(),
                a.getStatus().toString()
            });
        }
        if (actives.isEmpty()) showStatus("No active admissions.", false);
        else if (statusLbl.getText().startsWith("No active")) showStatus(" ", true);
    }

    public void submitTransfer(Admission admission, Ward destination) {
        if (admission == null || destination == null) {
            showStatus("Select an active admission row and a destination ward.", false);
            return;
        }
        if (admission.getWard() == destination) {
            showStatus("Patient is already in " + destination.getWardName() + ".", false);
            return;
        }
        boolean ok = controller.transferPatient(admission, destination);
        if (ok) {
            showStatus("✔  " + admission.getPatient().getName()
                    + " transferred to " + destination.getWardName()
                    + ", bed " + admission.getBed().getBedId() + ".", true);
            loadActiveAdmissions();
        } else {
            showStatus(destination.getWardName() + " is at full capacity. Select another ward.", false);
        }
    }

    public void dischargePatient(Admission admission) {
        if (admission == null) { showStatus("Select a row to discharge.", false); return; }
        try {
            controller.dischargePatient(admission);
            showStatus("✔  " + admission.getPatient().getName() + " discharged, bed released.", true);
            loadActiveAdmissions();
        } catch (IllegalStateException ex) {
            showStatus("Cannot discharge: " + ex.getMessage(), false);
        }
    }

    public void cancelAdmission(Admission admission) {
        if (admission == null) { showStatus("Select a row to cancel.", false); return; }
        int choice = JOptionPane.showConfirmDialog(this,
                "Cancel admission for " + admission.getPatient().getName() + "?",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            controller.cancelAdmission(admission);
            showStatus("✔  Admission " + admission.getAdmissionId() + " cancelled, bed released.", true);
            loadActiveAdmissions();
        } catch (IllegalStateException ex) {
            showStatus("Cannot cancel: " + ex.getMessage(), false);
        }
    }

    public void viewMyAdmission(Patient patient) {
        histModel.setRowCount(0);
        if (patient == null) return;
        List<Admission> mine = dataStore.findAdmissionsByPatient(patient.getUserId());
        if (mine.isEmpty()) {
            histModel.addRow(new Object[]{"No records", "—", "—", "—", "—", "—"});
            return;
        }
        for (Admission a : mine) {
            histModel.addRow(new Object[]{
                a.getAdmissionId(),
                a.getWard().getWardName(),
                a.getBed().getBedId(),
                a.getStatus().toString(),
                a.getAdmissionDate(),
                a.getDischargeDate() != null ? a.getDischargeDate().toString() : "—"
            });
        }
    }

    // Helpers
    // Gets the Admission object matching the currently selected table row.
    private Admission selectedAdmission() {
        int row = activeTable.getSelectedRow();
        if (row < 0) return null;
        // Re-fetch by ID from live list
        String id = (String) activeModel.getValueAt(row, 0);
        return dataStore.findActiveAdmissions().stream()
                .filter(a -> a.getAdmissionId().equals(id))
                .findFirst().orElse(null);
    }

    private void showStatus(String msg, boolean success) {
        statusLbl.setText(msg);
        statusLbl.setForeground(success ? AppTheme.SUCCESS : AppTheme.DANGER);
    }

    private static DefaultListCellRenderer patientRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Patient p) setText(p.getName());
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
                    setText(w.getWardName() + (w.isAtCapacity() ? "  ⛔ FULL" : ""));
                setFont(AppTheme.FONT_BODY);
                return this;
            }
        };
    }
}