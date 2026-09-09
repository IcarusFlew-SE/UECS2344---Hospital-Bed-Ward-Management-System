package hospital.app;

import java.util.List;

import hospital.controller.HospitalController;
import hospital.model.Admin;
import hospital.model.Doctor;
import hospital.model.Nurse;
import hospital.model.User;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;

// UC09 - Manage User Account & Profile.
// ("Admin manages any staff account; Doctor/Nurse manage own profile").
public class AccountUI extends JPanel {
	private final HospitalController controller;
	private final List<User> allUsers;
	private final JComboBox<User> userBox = new JComboBox<>();
	private final JLabel detailsLabel = new JLabel(" ");
	private final JTextField updateContactField = new JTextField();
	private final JButton updateBtn = new JButton("Update Contact");
	private final JButton deactivateBtn = new JButton("Deactivate Account");

	private final JComboBox<String> newRoleBox = new JComboBox<>();
	private final JTextField newNameField = new JTextField();
	private final JTextField newContactField = new JTextField();
	private final JTextField newEmailField = new JTextField();
	private final JButton createBtn = new JButton("Create Account");

	public AccountUI(HospitalController controller, List<User> users) {
		this.controller = controller;
		this.allUsers = users;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		for (User u : users) userBox.addItem(u);
		userBox.addActionListener(e -> viewAccount((User) userBox.getSelectedItem()));
		sizeField(userBox);
		sizeField(updateContactField);

		updateBtn.addActionListener(e -> updateAccount((User) userBox.getSelectedItem(), updateContactField.getText()));

		deactivateBtn.addActionListener(e -> deactivateAccount((User) userBox.getSelectedItem()));

		newRoleBox.addItem("Doctor");
		newRoleBox.addItem("Nurse");
		newRoleBox.addItem("Admin");
		sizeField(newRoleBox);
		sizeField(newNameField);
		sizeField(newContactField);
		sizeField(newEmailField);

		JButton createBtn = this.createBtn;
		createBtn.addActionListener(e -> createAccount(
				(String) newRoleBox.getSelectedItem(), newNameField.getText(),
				newContactField.getText(), newEmailField.getText()));

		add(label("Manage User Account & Profile"));
		add(Box.createVerticalStrut(10));
		add(label("Select Account:"));
		add(userBox);
		add(Box.createVerticalStrut(8));
		add(detailsLabel);
		add(Box.createVerticalStrut(8));
		add(label("New Contact:"));
		add(updateContactField);
		add(Box.createVerticalStrut(8));
		add(updateBtn);
		add(Box.createHorizontalStrut(8));
		add(deactivateBtn);

		add(Box.createVerticalStrut(20));
		add(new JSeparator());
		add(Box.createVerticalStrut(10));
		add(label("Create New Account"));
		add(Box.createVerticalStrut(8));
		add(label("Role:"));
		add(newRoleBox);
		add(Box.createVerticalStrut(8));
		add(label("Name:"));
		add(newNameField);
		add(Box.createVerticalStrut(8));
		add(label("Contact:"));
		add(newContactField);
		add(Box.createVerticalStrut(8));
		add(label("Email:"));
		add(newEmailField);
		add(Box.createVerticalStrut(10));
		add(createBtn);

		if (userBox.getItemCount() > 0) {
			userBox.setSelectedIndex(0);
		}
	}

	// RBAC - Admin sees and manages every account; Doctor/Nurse are locked to their own
	// account, view/update only, matching the use case document's own stated split
	public void applyPermissions(User currentUser) {
		boolean isAdmin = currentUser != null && currentUser.getPermissions().contains("MANAGE_USERS");
		deactivateBtn.setEnabled(isAdmin);
		newRoleBox.setEnabled(isAdmin);
		newNameField.setEnabled(isAdmin);
		newContactField.setEnabled(isAdmin);
		newEmailField.setEnabled(isAdmin);
		createBtn.setEnabled(isAdmin);

		userBox.removeAllItems();
		if (isAdmin) {
			for (User u : allUsers) userBox.addItem(u);
		} else if (currentUser != null) {
			userBox.addItem(currentUser); // Doctor/Nurse: own account only
		}
		userBox.setEnabled(isAdmin); // Doctor/Nurse can't switch to browse other accounts
		if (userBox.getItemCount() > 0) userBox.setSelectedIndex(0);
	}

	// UC09 Sub-flow S2 - View
	public void viewAccount(User user) {
		if (user == null) return;
		detailsLabel.setText(user.getName() + "  |  " + user.getContact() + "  |  " + user.getEmail()
				+ "  |  Active: " + user.isActive());
	}

	// UC09 Sub-flow S3 - Update
	public void updateAccount(User user, String newContact) {
		if (user == null || newContact == null || newContact.isBlank()) {
			detailsLabel.setText("Select an account and enter a new contact.");
			return;
		}
		controller.updateUserContact(user, newContact);
		viewAccount(user);
	}

	// UC09 Sub-flow S4 - Deactivate
	public void deactivateAccount(User user) {
		if (user == null) {
			detailsLabel.setText("Select an account to deactivate.");
			return;
		}
		int choice = JOptionPane.showConfirmDialog(this,
				"Deactivate " + user.getName() + "'s account?",
				"Confirm Deactivation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (choice == JOptionPane.YES_OPTION) {
			controller.deactivateUser(user);
			viewAccount(user);
		}
	}

	// UC09 Sub-flow S1 - Create
	public void createAccount(String role, String name, String contact, String email) {
		if (role == null || name.isBlank() || contact.isBlank() || email.isBlank()) {
			detailsLabel.setText("Fill in role, name, contact, and email.");
			return;
		}
		String id = role.substring(0, 1) + System.currentTimeMillis();
		User newUser = switch (role) {
			case "Doctor" -> new Doctor(id, name, contact, email, "General", "General");
			case "Nurse" -> new Nurse(id, name, contact, email, "Morning");
			default -> new Admin(id, name, contact, email, "Standard");
		};
		controller.createUser(newUser);
		userBox.addItem(newUser);
		userBox.setSelectedItem(newUser);
		detailsLabel.setText("Created account: " + newUser.getName());
	}

	private static void sizeField(JComponent field) {
		field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
	}

	private static JLabel label(String text) {
		JLabel l = new JLabel(text);
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		return l;
	}
}
