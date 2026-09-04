package hospital.app;

import hospital.data.HospitalDataStore;
import hospital.model.Notification;

import javax.swing.*;
import java.awt.Component;
import java.awt.FlowLayout;

public class NotificationUI extends JPanel {
	private final HospitalDataStore dataStore;
	private final String currentUserId;
	private final DefaultListModel<String> listModel = new DefaultListModel<>();
	private final JList<String> listView = new JList<>(listModel);

	public NotificationUI(HospitalDataStore dataStore, String currentUserId) {
		this.dataStore = dataStore;
		this.currentUserId = currentUserId;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		listView.setVisibleRowCount(12);
		JScrollPane scroll = new JScrollPane(listView);
		scroll.setAlignmentX(Component.LEFT_ALIGNMENT);

		JButton refreshBtn = new JButton("Refresh");
		refreshBtn.addActionListener(e -> viewNotifications());

		JButton markReadBtn = new JButton("Mark Selected as Read");
		markReadBtn.addActionListener(e -> markAsRead(listView.getSelectedIndex()));

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
		buttons.add(refreshBtn);
		buttons.add(markReadBtn);

		add(label("Notifications"));
		add(Box.createVerticalStrut(10));
		add(scroll);
		add(Box.createVerticalStrut(10));
		add(buttons);
		viewNotifications();
	}

	// Actor (User) request notifications, and views them then mark as read
	public void viewNotifications() {
		listModel.clear();
		for (Notification n : dataStore.findNotificationsByUser(currentUserId)) {
			String prefix = n.isRead() ? "[read] " : "[new] ";
			listModel.addElement(prefix + n.getDateTime() + " - " + n.getMessage());
		}
	}

	public void markAsRead(int index) {
		var notifications = dataStore.findNotificationsByUser(currentUserId);
		if (index >= 0 && index < notifications.size()) {
			notifications.get(index).setAsRead();
			viewNotifications();
		}
	}

	private static JLabel label(String text) {
		JLabel l = new JLabel(text);
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		return l;
	}
}
