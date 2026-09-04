package hospital.data;

import java.util.*;
import hospital.model.*;

public class HospitalDataStore {
	private List<Ward> wards = new ArrayList<>();
	private List<Admission> admissions = new ArrayList<>();
	private List<User> users = new ArrayList<>();
	private List<Notification> notifications = new ArrayList<>();
	
	public Bed findAvailableBed(Ward ward) {
		return ward.findAvailableBed();
	}
	
	public void saveWard(Ward w) {
		wards.add(w);
	}
	
	public void saveAdmission(Admission a) {
		admissions.add(a);
	}
	
	public void saveUser(User u) {
		users.add(u);
	}
	
	public void saveNotification(Notification n) {
		notifications.add(n);
	}
	
    public List<Ward> findAllWards() { return wards; }
    public List<Admission> findAllAdmissions() { return admissions; }
    public List<User> findAllUsers() { return users; }
	public User findUserById(String userId) {
        for (User u : users) {
            if (u.getUserId().equals(userId)) return u;
        }
        return null;
    }

    public List<Notification> findNotificationsByUser(String userId) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : notifications) {
            if (n.getRecipientId() == null || n.getRecipientId().equals(userId)) {
                result.add(n);
            }
        }
        return result;
    }
	
}
