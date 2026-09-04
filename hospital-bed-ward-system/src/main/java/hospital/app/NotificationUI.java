package hospital.app;

import hospital.data.HospitalDataStore;
import hospital.model.Notification;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class NotificationUI extends VBox{
	private final HospitalDataStore dataStore;
    private final String currentUserId;
    private final ListView<String> listView = new ListView<>();
    
    public NotificationUI(HospitalDataStore dataStore, String currentUserId) {
        this.dataStore = dataStore;
        this.currentUserId = currentUserId;
        setSpacing(10);
        setPadding(new Insets(15));

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> viewNotifications());

        Button markReadBtn = new Button("Mark Selected as Read");
        markReadBtn.setOnAction(e -> markAsRead(listView.getSelectionModel().getSelectedIndex()));

        getChildren().addAll(new Label("Notifications"), listView, refreshBtn, markReadBtn);
        viewNotifications();
    }
    
    // Actor (User) request notifications, and views them then mark as read
    public void viewNotifications() {
        listView.getItems().clear();
        for (Notification n : dataStore.findNotificationsByUser(currentUserId)) {
            String prefix = n.isRead() ? "[read] " : "[new] ";
            listView.getItems().add(prefix + n.getDateTime() + " - " + n.getMessage());
        }
    }

    public void markAsRead(int index) {
        var notifications = dataStore.findNotificationsByUser(currentUserId);
        if (index >= 0 && index < notifications.size()) {
            notifications.get(index).setAsRead();
            viewNotifications();
        }
    }
}
