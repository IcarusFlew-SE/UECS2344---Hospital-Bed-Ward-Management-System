package hospital.model;

import java.time.LocalDateTime;

public class Notification {
	private String notificationId;
    private String message;
    private LocalDateTime dateTime;
    private boolean isRead;
    private String recipientId; //null values indicate sent to all users
    
    public Notification(String message) {
    	this.notificationId = "S" + System.currentTimeMillis();
    	this.message = message;
    	this.dateTime = LocalDateTime.now();
    	this.isRead = false;
    }
    
    public String getMessage() { return message; }
    public LocalDateTime getDateTime() { return dateTime; }
    public boolean isRead() { return isRead; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId;}
    
    public void setAsRead() {this.isRead = true;}
}
