package hospital.data;

import java.util.*;
import hospital.model.*;

public class HospitalDataStore {
	private List<Ward> wards = new ArrayList<>();
	private List<Admission> admissions = new ArrayList<>();
	private List<User> users = new ArrayList<>();
	private List<Notification> notifications = new ArrayList<>();
	
	public Bed findAvailableBed(Ward ward) {
		if (ward == null) return null;
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
	
	// Return unmodifiable lists to avoid external mutation of the in-memory store.
	public List<Ward> findAllWards() { return Collections.unmodifiableList(wards); }
	public List<Admission> findAllAdmissions() { return Collections.unmodifiableList(admissions); }
	public List<User> findAllUsers() { return Collections.unmodifiableList(users); }

    // A Patient may hold at most one active Admission (assumption)
    public Admission findActiveAdmissionByPatient(Patient patient) {
        if (patient == null) return null;
        for (Admission a : admissions) {
            if (a.getPatient() != null
                    && a.getPatient().getUserId().equals(patient.getUserId())
                    && a.isActive()) return a;
        }
        return null;
    }

    // A Bed held by an active Admission must not be freed
    public Admission findActiveAdmissionByBed(Bed bed) {
        if (bed == null) return null;
        for (Admission a : admissions) {
            if (a.getBed() != null
                    && bed.getBedId().equals(a.getBed().getBedId())
                    && a.isActive()) return a;
        }
        return null;
    }

    // Return active admissions
    public List<Admission> findActiveAdmissions() {
        List<Admission> result = new ArrayList<>();
        for (Admission a : admissions) {
            if (a.isActive()) result.add(a);
        }
        return result;
    }

    // Patient views own admission history
    public List<Admission> findAdmissionsByPatient(String patientId) {
        List<Admission> result = new ArrayList<>();
        for (Admission a : admissions) {
            if (a.getPatient() != null && a.getPatient().getUserId().equals(patientId)) result.add(a);
        }
        return result;
    }
	public User findUserById(String userId) {
        for (User u : users) {
            if (u.getUserId().equals(userId)) return u;
        }
        return null;
    }
    public Patient findPatientById(String patientId) {
        for (User u : users) {
            if (u instanceof Patient p && p.getUserId().equalsIgnoreCase(patientId)) {
                return p;
            }
        }
        return null;
    }

    // A null userId shows broadcast and targeted notifications
    public List<Notification> findNotificationsByUser(String userId) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : notifications) {
            // if userId==null show broadcast and targeted notifications
            if (userId == null || n.getRecipientId() == null || n.getRecipientId().equals(userId)) {
                result.add(n);
            }
        }
        return result;
    }

    // Returns every Nurse in the user list – convenience method for UI panels.
    public List<Nurse> findAllNurses() {
        List<Nurse> result = new ArrayList<>();
        for (User u : users) {
            if (u instanceof Nurse n) result.add(n);
        }
        return result;
    }

    // Simulated credential check used by LoginUI: demo password matches any active user.
    public User findUserByCredentials(String userId, String password) {
        User u = findUserById(userId);
        if (u != null && u.isActive() && "demo123".equals(password)) return u;
        return null;
    }

}
