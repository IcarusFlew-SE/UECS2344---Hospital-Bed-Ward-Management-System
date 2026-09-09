package hospital.app;

import hospital.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

// LoginUI - modal login dialog for application start
public class LoginUI extends JDialog {

    private User loggedInUser = null;
    private final JComboBox<User> userBox;
    private final JPasswordField passwordField;
    private final JLabel errorLabel;

    private static final String DEMO_PASSWORD = "demo123";

    public LoginUI(Frame owner, List<User> users) {
        super(owner, "Hospital System – Login", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Header panel
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.PRIMARY);
        header.setBorder(new EmptyBorder(28, 30, 24, 30));

        JLabel crossIcon = new JLabel("🏥");
        crossIcon.setFont(new Font("SansSerif", Font.PLAIN, 44));
        crossIcon.setForeground(AppTheme.TEXT_ON_DARK);
        crossIcon.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLabel = new JLabel("Hospital Bed & Ward");
        titleLabel.setFont(AppTheme.FONT_TITLE.deriveFont(20f));
        titleLabel.setForeground(AppTheme.TEXT_ON_DARK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitleLabel = new JLabel("Management System");
        subtitleLabel.setFont(AppTheme.FONT_BODY);
        subtitleLabel.setForeground(new Color(0xA8C4D4));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel titleGroup = new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.add(crossIcon);
        titleGroup.add(Box.createVerticalStrut(6));
        titleGroup.add(titleLabel);
        titleGroup.add(subtitleLabel);

        header.add(titleGroup, BorderLayout.CENTER);

        // Form panel
        JPanel form = new JPanel();
        form.setBackground(AppTheme.CARD);
        form.setBorder(new EmptyBorder(28, 36, 28, 36));
        form.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1.0;

        // User selector
        userBox = new JComboBox<>();
        for (User u : users) userBox.addItem(u);
        userBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User u)
                    setText(u.getName() + "  (" + u.getClass().getSimpleName() + ")");
                setFont(AppTheme.FONT_BODY);
                return this;
            }
        });
        userBox.setFont(AppTheme.FONT_BODY);

        // Password field
        passwordField = new JPasswordField();
        passwordField.setFont(AppTheme.FONT_BODY);
        passwordField.setToolTipText("Demo password: demo123");

        // Error label (hidden until needed)
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(AppTheme.DANGER);
        errorLabel.setFont(AppTheme.FONT_SMALL);

        // Hint label
        JLabel hint = new JLabel("Demo password: demo123");
        hint.setFont(AppTheme.FONT_SMALL);
        hint.setForeground(AppTheme.TEXT_SECONDARY);

        // Login button
        JButton loginBtn = AppTheme.primaryButton("Login");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        loginBtn.addActionListener(e -> attemptLogin());

        // Allow Enter key to submit
        passwordField.addActionListener(e -> attemptLogin());

        // Layout rows
        addRow(form, gbc, 0, userLabel("Select Account"), userBox);
        addRow(form, gbc, 1, userLabel("Password"), passwordField);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        form.add(hint, gbc);

        gbc.gridy = 3;
        form.add(errorLabel, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(14, 0, 0, 0);
        form.add(loginBtn, gbc);

        // Footer
        JPanel footer = new JPanel();
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(new EmptyBorder(8, 0, 8, 0));
        JLabel footerLabel = new JLabel("Healthcare Management System");
        footerLabel.setFont(AppTheme.FONT_SMALL);
        footerLabel.setForeground(AppTheme.TEXT_SECONDARY);
        footer.add(footerLabel);

        // Assemble
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(400, 0));
        setLocationRelativeTo(owner);
    }

    private void attemptLogin() {
        User selected = (User) userBox.getSelectedItem();
        String pw = new String(passwordField.getPassword());

        if (selected == null) {
            errorLabel.setText("Please select a user account.");
            return;
        }
        if (!DEMO_PASSWORD.equals(pw)) {
            errorLabel.setText("Incorrect password. Try: demo123");
            passwordField.setText("");
            passwordField.requestFocus();
            return;
        }
        if (!selected.isActive()) {
            errorLabel.setText("This account has been deactivated.");
            return;
        }
        loggedInUser = selected;
        dispose();
    }

    // Returns the successfully authenticated user, or null if dialog was closed.
    public User getLoggedInUser() {
        return loggedInUser;
    }

    // Helpers
    private static JLabel userLabel(String text) {
        JLabel lbl = new JLabel(text + ":");
        lbl.setFont(AppTheme.FONT_BODY.deriveFont(Font.BOLD));
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        return lbl;
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc, int row, JLabel label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.insets = new Insets(row == 0 ? 0 : 10, 0, 2, 8);
        panel.add(label, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.insets = new Insets(row == 0 ? 0 : 10, 0, 2, 0);
        panel.add(field, gbc);
    }
}