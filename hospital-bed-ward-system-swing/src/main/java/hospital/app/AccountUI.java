package hospital.app;

import hospital.controller.HospitalController;
import hospital.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

// AccountUI - manage user accounts and profiles
public class AccountUI extends JPanel {

    private final HospitalController controller;
    private final List<User> allUsers;

    // User table
    private final String[] USER_COLS = {"User ID", "Name", "Role", "Contact", "Email", "Active"};
    private final DefaultTableModel tableModel = new DefaultTableModel(USER_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable userTable = new JTable(tableModel);

    // Manage-account section
    private final JLabel detailsLabel = new JLabel(" ");
    private final JTextField updateContactFld = new JTextField(16);
    private final JButton updateBtn = AppTheme.primaryButton("Update Contact");
    private final JButton deactivateBtn = AppTheme.dangerButton("Deactivate Account");

    // Create-account section
    private final JComboBox<String> newRoleBox = new JComboBox<>();
    private final JTextField newNameFld = new JTextField(16);
    private final JTextField newContactFld = new JTextField(16);
    private final JTextField newEmailFld = new JTextField(16);
    private final JButton createBtn = AppTheme.primaryButton("Create Account");

    public AccountUI(HospitalController controller, List<User> users) {
        this.controller = controller;
        this.allUsers = users;

        setLayout(new BorderLayout(0, 12));
        setBackground(AppTheme.SURFACE);
        setBorder(AppTheme.pagePadding());

        // User table
        AppTheme.styleTable(userTable);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) viewSelectedAccount();
        });

        JScrollPane tableScroll = new JScrollPane(userTable);
        AppTheme.styleScrollPane(tableScroll);
        tableScroll.setPreferredSize(new Dimension(0, 180));

        JPanel tableCard = new JPanel(new BorderLayout(0, 4));
        tableCard.setBackground(AppTheme.CARD);
        tableCard.setBorder(AppTheme.sectionBorder("User Accounts"));
        tableCard.add(tableScroll, BorderLayout.CENTER);

        // Bottom panels
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 12, 0));
        bottomRow.setOpaque(false);
        bottomRow.add(buildManagePanel());
        bottomRow.add(buildCreatePanel());

        add(tableCard, BorderLayout.CENTER);
        add(bottomRow, BorderLayout.SOUTH);

        // Wire role selectors
        newRoleBox.addItem("Patient");
        newRoleBox.addItem("Doctor");
        newRoleBox.addItem("Nurse");
        newRoleBox.addItem("Admin");

        createBtn.addActionListener(e -> createAccount(
            (String) newRoleBox.getSelectedItem(),
            newNameFld.getText(),
            newContactFld.getText(),
            newEmailFld.getText()));
        
        updateBtn.addActionListener(e -> updateAccount(updateContactFld.getText()));
        deactivateBtn.addActionListener(e -> deactivateAccount());

        refreshTable();
    }

    // Panel builders
    private JPanel buildManagePanel() {
        AppTheme.sizeField(updateContactFld);
        detailsLabel.setFont(AppTheme.FONT_BODY);
        detailsLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 6, 0);
        grid.add(detailsLabel, gbc);

        gbc.gridwidth = 1;
        addRow(grid, gbc, 1, "New Contact:", updateContactFld);

        gbc.gridx = 0; gbc.gridy = 2; gbc.insets = new Insets(10, 0, 0, 6);
        grid.add(updateBtn, gbc);
        gbc.gridx = 1; gbc.insets = new Insets(10, 0, 0, 0);
        grid.add(deactivateBtn, gbc);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppTheme.CARD);
        card.setBorder(AppTheme.sectionBorder("Manage Selected Account"));
        card.add(grid, BorderLayout.NORTH);
        return card;
    }

    private JPanel buildCreatePanel() {
        AppTheme.sizeCombo(newRoleBox);
        AppTheme.sizeField(newNameFld);
        AppTheme.sizeField(newContactFld);
        AppTheme.sizeField(newEmailFld);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addRow(grid, gbc, 0, "Role:", newRoleBox);
        addRow(grid, gbc, 1, "Name:", newNameFld);
        addRow(grid, gbc, 2, "Contact:", newContactFld);
        addRow(grid, gbc, 3, "Email:", newEmailFld);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.insets = new Insets(10, 0, 0, 0);
        grid.add(createBtn, gbc);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppTheme.CARD);
        card.setBorder(AppTheme.sectionBorder("Create New Account"));
        card.add(grid, BorderLayout.NORTH);
        return card;
    }

    // View account details for the selected row
    public void viewSelectedAccount() {
        User u = getSelectedUser();
        if (u == null) {
            detailsLabel.setText("Select a row to manage.");
            return;
        }
        detailsLabel.setText("<html><b>" + u.getName() + "</b>  |  "
                + u.getClass().getSimpleName() + "  |  " + u.getContact()
                + "  |  " + u.getEmail()
                + "  |  Active: " + (u.isActive() ? "<font color='green'>Yes</font>"
                                                   : "<font color='red'>No</font>")
                + "</html>");
    }

    // Update contact
    public void updateAccount(String newContact) {
        User u = getSelectedUser();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Select a user row first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (newContact == null || newContact.isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter a new contact number.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        controller.updateUserContact(u, newContact);
        refreshTable();
        viewSelectedAccount();
        JOptionPane.showMessageDialog(this, "Contact updated for " + u.getName() + ".",
                "Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    // Deactivate account
    public void deactivateAccount() {
        User u = getSelectedUser();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Select a user row first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Deactivate " + u.getName() + "'s account? This cannot be undone.",
                "Confirm Deactivation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            controller.deactivateUser(u);
            refreshTable();
            viewSelectedAccount();
        }
    }

    // Create new account
    public void createAccount(String role, String name, String contact, String email) {
        if (role == null || name.isBlank() || contact.isBlank() || email.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Fill in role, name, contact, and email.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = role.substring(0, 1).toUpperCase() + System.currentTimeMillis();
        User newUser = switch (role) {
            case "Patient" -> new Patient(id, name, contact, email, LocalDate.now());
            case "Doctor" -> new Doctor(id, name, contact, email, "General", "General");
            case "Nurse" -> new Nurse(id, name, contact, email, "Morning");
            default -> new Admin(id, name, contact, email, "Standard");
        };
        controller.createUser(newUser);
        refreshTable();
        newNameFld.setText("");
        newContactFld.setText("");
        newEmailFld.setText("");
        JOptionPane.showMessageDialog(this,
                "Account created for " + name + "  [" + role + "].",
                "Account Created", JOptionPane.INFORMATION_MESSAGE);
    }

    // Refreshes the user table
    public void refreshTable() {
        tableModel.setRowCount(0);
        for (User u : allUsers) {
            tableModel.addRow(new Object[]{
                u.getUserId(),
                u.getName(),
                u.getClass().getSimpleName(),
                u.getContact(),
                u.getEmail(),
                u.isActive() ? "✔" : "✖"
            });
        }
    }


    private User getSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0 || row >= allUsers.size()) return null;
        // Match by userId column (col 0)
        String uid = (String) tableModel.getValueAt(row, 0);
        return allUsers.stream()
                .filter(u -> u.getUserId().equals(uid))
                .findFirst().orElse(null);
    }

    private static void addRow(JPanel p, GridBagConstraints gbc, int row, String text, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        gbc.insets = new Insets(5, 0, 5, 8);
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_BODY.deriveFont(Font.BOLD));
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        p.add(field, gbc);
    }
}