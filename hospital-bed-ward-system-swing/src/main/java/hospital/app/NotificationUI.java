package hospital.app;

import hospital.data.HospitalDataStore;
import hospital.model.Notification;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

// NotificationUI - view and manage alerts/notifications
public class NotificationUI extends JPanel {

    private final HospitalDataStore dataStore;
    private String currentUserId;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");

    private final String[] NOTIF_COLS = {"Status", "Date / Time", "Message", "Recipient"};
    private final DefaultTableModel model = new DefaultTableModel(NOTIF_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JLabel summaryLbl = new JLabel(" ");

    public NotificationUI(HospitalDataStore dataStore, String currentUserId) {
        this.dataStore     = dataStore;
        this.currentUserId = currentUserId;

        setLayout(new BorderLayout(0, 10));
        setBackground(AppTheme.SURFACE);
        setBorder(AppTheme.pagePadding());

        // Table setup
        AppTheme.styleTable(table);
        table.setRowHeight(26);

        // Custom renderer: unread rows get a tinted background
        DefaultTableCellRenderer unreadRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    String status = (String) tbl.getModel().getValueAt(row, 0);
                    if ("🔔 NEW".equals(status)) {
                        setBackground(new Color(0xFFF3CD)); // light amber
                        setForeground(AppTheme.TEXT_PRIMARY);
                        setFont(AppTheme.FONT_BODY.deriveFont(Font.BOLD));
                    } else {
                        setBackground(Color.WHITE);
                        setForeground(AppTheme.TEXT_SECONDARY);
                        setFont(AppTheme.FONT_BODY);
                    }
                } else {
                    setBackground(AppTheme.ACCENT_LIGHT);
                    setForeground(AppTheme.TEXT_ON_DARK);
                    setFont(AppTheme.FONT_BODY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        for (int c = 0; c < NOTIF_COLS.length; c++)
            table.getColumnModel().getColumn(c).setCellRenderer(unreadRenderer);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setMaxWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setMaxWidth(110);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(table);
        AppTheme.styleScrollPane(scroll);

        // Toolbar
        summaryLbl.setFont(AppTheme.FONT_SMALL);
        summaryLbl.setForeground(AppTheme.TEXT_SECONDARY);

        JButton refreshBtn  = AppTheme.secondaryButton("↺  Refresh");
        JButton markReadBtn = AppTheme.primaryButton("✔  Mark as Read");

        refreshBtn.addActionListener(e  -> viewNotifications());
        markReadBtn.addActionListener(e -> markAsRead(table.getSelectedRow()));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.add(refreshBtn);
        toolbar.add(markReadBtn);
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(summaryLbl);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        legend.setOpaque(false);
        legend.add(legendDot(new Color(0xFFF3CD), "Unread / New"));
        legend.add(legendDot(Color.WHITE, "Read"));

        // Card
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(AppTheme.CARD);
        card.setBorder(AppTheme.sectionBorder("Notifications & Alerts"));
        card.add(legend, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(toolbar, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
        viewNotifications();
    }

    // Public API
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        viewNotifications();
    }

    public void viewNotifications() {
        model.setRowCount(0);
        List<Notification> notifications = dataStore.findNotificationsByUser(currentUserId);
        long unread = 0;
        for (Notification n : notifications) {
            if (!n.isRead()) unread++;
            model.addRow(new Object[]{
                n.isRead() ? "✓ READ" : "🔔 NEW",
                n.getDateTime().format(DT_FMT),
                n.getMessage(),
                n.getRecipientId() != null ? n.getRecipientId() : "(all)"
            });
        }
        summaryLbl.setText(notifications.size() + " notifications  |  "
                + unread + " unread");
        summaryLbl.setForeground(unread > 0 ? AppTheme.WARNING : AppTheme.SUCCESS);
    }

    public void markAsRead(int tableRow) {
        if (tableRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a notification row first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Notification> notifications = dataStore.findNotificationsByUser(currentUserId);
        if (tableRow < notifications.size()) {
            notifications.get(tableRow).setAsRead();
            viewNotifications();
        }
    }

    // Legend helper
    private static JPanel legendDot(Color color, String label) {
        JPanel dot = new JPanel();
        dot.setBackground(color);
        dot.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR));
        dot.setPreferredSize(new Dimension(16, 16));

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.setOpaque(false);
        row.add(dot);
        row.add(lbl);
        return row;
    }
}