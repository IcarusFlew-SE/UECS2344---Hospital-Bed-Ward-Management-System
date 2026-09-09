package hospital.app;

import hospital.controller.HospitalController;
import hospital.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// WardStatusUI - bed status update, nurse assignment view and ward overview
public class WardStatusUI extends JPanel {

    private final HospitalController controller;
    private final List<Ward> wards;

    // Bed-status update form
    private final JComboBox<Ward> wardBox = new JComboBox<>();
    private final JComboBox<Bed> bedBox = new JComboBox<>();
    private final JComboBox<BedStatus> statusBox = new JComboBox<>();
    private final JLabel resultLbl = new JLabel(" ");

    // Nurse assignment view
    private final JComboBox<Nurse> nurseBox = new JComboBox<>();
    private final JLabel assignmentLabel = new JLabel(" ");

    // Overview table
    private final String[] OV_COLS = {"Ward", "Ward Type", "Bed ID", "Status", "Last Cleaned"};
    private final DefaultTableModel ovModel = new DefaultTableModel(OV_COLS, 0) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable ovTable = new JTable(ovModel);

    // Occupancy panels (one progress bar per ward)
    private JPanel occupancyPanel;

    public WardStatusUI(HospitalController controller, List<Ward> wards, List<Nurse> nurses) {
        this.controller = controller;
        this.wards = wards;

        setLayout(new BorderLayout(0, 12));
        setBackground(AppTheme.SURFACE);
        setBorder(AppTheme.pagePadding());

        // Left column: form + nurse + occupancy
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);
        leftCol.add(buildUpdateForm());
        leftCol.add(Box.createVerticalStrut(10));
        leftCol.add(buildNursePanel(nurses));
        leftCol.add(Box.createVerticalStrut(10));
        leftCol.add(buildOccupancyPanel());

        // Right column: overview table
        JPanel rightCol = buildOverviewPanel();

        // Split
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCol, rightCol);
        split.setDividerLocation(340);
        split.setDividerSize(6);
        split.setResizeWeight(0.35);
        split.setOpaque(false);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
        viewAllWardStatus();
    }

    // Section builders

    private JPanel buildUpdateForm() {
        for (Ward w : wards) wardBox.addItem(w);
        for (BedStatus s : BedStatus.values()) statusBox.addItem(s);
        wardBox.setRenderer(wardRenderer());
        bedBox.setRenderer(bedRenderer());
        AppTheme.sizeCombo(wardBox);
        AppTheme.sizeCombo(bedBox);
        AppTheme.sizeCombo(statusBox);
        wardBox.addActionListener(e -> refreshBeds());
        if (wardBox.getItemCount() > 0) wardBox.setSelectedIndex(0);

        resultLbl.setFont(AppTheme.FONT_BODY);
        resultLbl.setForeground(AppTheme.TEXT_SECONDARY);

        JButton submitBtn = AppTheme.primaryButton("Update Status");
        submitBtn.addActionListener(e -> selectBed(
                (Bed) bedBox.getSelectedItem(),
                (BedStatus) statusBox.getSelectedItem()));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = formGbc();
        addFormRow(grid, gbc, 0, "Ward:", wardBox);
        addFormRow(grid, gbc, 1, "Bed:", bedBox);
        addFormRow(grid, gbc, 2, "New Status:", statusBox);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        grid.add(submitBtn, gbc);
        gbc.gridy = 4; gbc.insets = new Insets(4, 0, 0, 0);
        grid.add(resultLbl, gbc);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppTheme.CARD);
        card.setBorder(AppTheme.sectionBorder("Update Bed Status"));
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildNursePanel(List<Nurse> nurses) {
        for (Nurse n : nurses) nurseBox.addItem(n);
        nurseBox.setRenderer(nurseRenderer());
        AppTheme.sizeCombo(nurseBox);
        nurseBox.addActionListener(e -> viewMyAssignment((Nurse) nurseBox.getSelectedItem()));
        if (nurseBox.getItemCount() > 0) viewMyAssignment((Nurse) nurseBox.getItemAt(0));

        assignmentLabel.setFont(AppTheme.FONT_BODY);
        assignmentLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = formGbc();
        addFormRow(grid, gbc, 0, "I am:", nurseBox);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.insets = new Insets(4, 0, 0, 0);
        grid.add(assignmentLabel, gbc);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppTheme.CARD);
        card.setBorder(AppTheme.sectionBorder("My Ward Assignment"));
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildOccupancyPanel() {
        occupancyPanel = new JPanel();
        occupancyPanel.setLayout(new BoxLayout(occupancyPanel, BoxLayout.Y_AXIS));
        occupancyPanel.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppTheme.CARD);
        card.setBorder(AppTheme.sectionBorder("Ward Occupancy"));
        card.add(occupancyPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildOverviewPanel() {
        AppTheme.styleTable(ovTable);
        // Make the overview table sortable and use last-column resize to keep status readable
        ovTable.setAutoCreateRowSorter(true);
        ovTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        // Colour every cell based on the Status column (index 3)
        DefaultTableCellRenderer statusRenderer = AppTheme.bedStatusRenderer(3);
        for (int c = 0; c < OV_COLS.length; c++)
            ovTable.getColumnModel().getColumn(c).setCellRenderer(statusRenderer);

        // Widen Status column
        ovTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(ovTable);
        AppTheme.styleScrollPane(scroll);
        scroll.setViewportBorder(BorderFactory.createEmptyBorder(6,6,6,6));

        JButton refreshBtn = AppTheme.secondaryButton("↺  Refresh");
        refreshBtn.addActionListener(e -> viewAllWardStatus());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        top.setOpaque(false);
        top.add(refreshBtn);

        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(AppTheme.CARD);
        card.setBorder(AppTheme.sectionBorder("Bed Status Overview"));
        card.add(top, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // Public actions

    public final void refreshBeds() {
        Ward selected = (Ward) wardBox.getSelectedItem();
        Bed previous  = (Bed)  bedBox.getSelectedItem();
        bedBox.removeAllItems();
        if (selected == null) return;
        for (Bed bed : selected.getBeds()) bedBox.addItem(bed);
        if (previous != null) bedBox.setSelectedItem(previous);
    }

    public void selectBed(Bed bed, BedStatus newStatus) {
        if (bed == null || newStatus == null) {
            showStatus("Select a bed and a status first.", false);
            return;
        }
        try {
            controller.updateBedStatus(bed, newStatus);
            showStatus("✔  " + bed.getBedId() + " is now " + newStatus, true);
            JOptionPane.showMessageDialog(this,
                    bed.getBedId() + " status updated to " + newStatus,
                    "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshBeds();
            viewAllWardStatus();
        } catch (IllegalStateException ex) {
            showStatus("Rejected: " + ex.getMessage(), false);
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Update Rejected", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void viewMyAssignment(Nurse nurse) {
        if (nurse == null) return;
        Ward assigned = nurse.getAssignedWard();
        assignmentLabel.setText(assigned == null
                ? "No current assignment on record."
                : "Assigned to: " + assigned.getWardName() + "  (shift: " + nurse.getShift() + ")");
        assignmentLabel.setForeground(assigned == null ? AppTheme.WARNING : AppTheme.SUCCESS);
    }

    public void viewAllWardStatus() {
        ovModel.setRowCount(0);
        occupancyPanel.removeAll();

        for (Ward w : wards) {
            for (Bed b : w.getBeds()) {
                ovModel.addRow(new Object[]{
                    w.getWardName(),
                    w.getWardType(),
                    b.getBedId(),
                    b.getStatus().toString(),
                    b.getLastCleanedDate() != null ? b.getLastCleanedDate().toString() : "—"
                });
            }

            // Occupancy progress bar
            int total    = w.getBeds().size();
            int occupied = w.getOccupiedCount();
            int pct      = total == 0 ? 0 : (int) (occupied * 100.0 / total);

            JLabel wardLbl = new JLabel(w.getWardName() + "  (" + occupied + "/" + total + ")");
            wardLbl.setFont(AppTheme.FONT_SMALL.deriveFont(Font.BOLD));
            wardLbl.setForeground(AppTheme.TEXT_PRIMARY);
            wardLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue(pct);
            bar.setStringPainted(true);
            bar.setString(pct + "%");
            bar.setFont(AppTheme.FONT_SMALL);
            bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
            bar.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (pct >= 100) {
                bar.setForeground(AppTheme.DANGER);
            } else if (pct >= 75) {
                bar.setForeground(AppTheme.WARNING);
            } else {
                bar.setForeground(AppTheme.SUCCESS);
            }

            occupancyPanel.add(wardLbl);
            occupancyPanel.add(Box.createVerticalStrut(2));
            occupancyPanel.add(bar);
            occupancyPanel.add(Box.createVerticalStrut(8));
        }
        occupancyPanel.revalidate();
        occupancyPanel.repaint();
    }

    // Helpers

    private void showStatus(String msg, boolean success) {
        resultLbl.setText(msg);
        resultLbl.setForeground(success ? AppTheme.SUCCESS : AppTheme.DANGER);
    }

    private static GridBagConstraints formGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    private static void addFormRow(JPanel p, GridBagConstraints gbc, int row, String text, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = new Insets(5, 0, 5, 8);
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_BODY.deriveFont(Font.BOLD));
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        p.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        p.add(field, gbc);
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

    private static DefaultListCellRenderer bedRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Bed b) setText(b.getBedId() + "  –  " + b.getStatus());
                setFont(AppTheme.FONT_BODY);
                return this;
            }
        };
    }

    private static DefaultListCellRenderer nurseRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Nurse n) setText(n.getName() + "  (" + n.getShift() + ")");
                setFont(AppTheme.FONT_BODY);
                return this;
            }
        };
    }
}
