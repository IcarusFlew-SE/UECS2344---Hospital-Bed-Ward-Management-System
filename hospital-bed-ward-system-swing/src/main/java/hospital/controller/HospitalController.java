package hospital.controller;

import hospital.data.HospitalDataStore;
import hospital.model.Admission;
import hospital.model.Bed;
import hospital.model.BedStatus;
import hospital.model.Doctor;
import hospital.model.Notification;
import hospital.model.Nurse;
import hospital.model.Patient;
import hospital.model.Report;
import hospital.model.Transfer;
import hospital.model.Ward;

public class HospitalController {
	private HospitalDataStore dataStore;
	
	public HospitalController(HospitalDataStore dataStore) {
		this.dataStore = dataStore;
	}
	
	// UC01 - Manage Patient Admission
    public Admission admitPatient(Patient patient, Ward ward) {
        return admitPatient(patient, null, ward);
    }

    public Admission admitPatient(Patient patient, Doctor doctor, Ward ward) {
        // UC01 alt flow 3a - Patient already has an active admission (assumption 2)
        Admission existing = dataStore.findActiveAdmissionByPatient(patient);
        if (existing != null) {
            throw new IllegalStateException(patient.getName()
                    + " already has an active admission in " + existing.getWard().getWardName() + ".");
        }

        // getAvalableBeds() check on Ward
        Bed bed = dataStore.findAvailableBed(ward);
        if (bed == null) {
            notify("No bed available in ward " + ward.getWardName());
            return null;
        }

        // updateStatus("Occupied") on Bed
        bed.updateBedStatus(BedStatus.OCCUPIED);
        Admission admission = new Admission(generateId("A"), patient, bed, ward);

        dataStore.saveAdmission(admission);

        // Notify if ward reached capacity
        if (ward.isAtCapacity()) {
            // UC01 flow 7 - capacity alerts are targeted at Admin specifically, not broadcast
            notifyAllWithPermission("Ward " + ward.getWardName() + " has reached capacity.", "MANAGE_WARDS");
        }
        return admission;
    }

    // UC01 - the normal, successful conclusion of an admission (previously missing:
    // Admission.discharge() and AdmissionStatus.DISCHARGED already existed in the
    // model but nothing ever called this path)
    public void dischargePatient(Admission admission) {
        admission.discharge();
    }

    // UC01 alt flow 6a - cancel before discharge
    public void cancelAdmission(Admission admission) {
        admission.cancel();
    }

    // UC02 - Transfer Patient
    public boolean transferPatient(Admission admission, Ward newWard) {
        Bed newBed = dataStore.findAvailableBed(newWard);
        if (newBed == null) {
            notify("No bed available in ward " + newWard.getWardName());
            return false;
        }
        admission.getBed().updateBedStatus(BedStatus.CLEANING);
        newBed.updateBedStatus(BedStatus.OCCUPIED);
        Transfer transfer = new Transfer(generateId("T"), admission.getWard(), newWard, newBed,
                "Transfer of " + admission.getPatient().getName());
        admission.recordTransfer(transfer);
        // UC02 - notify the destination ward's nurse specifically, not everyone
        Nurse destinationNurse = findNurseForWard(newWard);
        if (destinationNurse != null) {
            notify("Patient transferred to " + newWard.getWardName(), destinationNurse.getUserId());
        } else {
            notify("Patient transferred to " + newWard.getWardName());
        }
        return true;
    }

    // Id based overload to match lifeline in SD UC02
    public boolean transferPatient(String patientId, Ward newWard) {
        Patient patient = dataStore.findPatientById(patientId);
        if (patient == null) {
            notify("Patient ID not found: " + patientId);
            return false;
        }
        Admission activeAdmission = dataStore.findActiveAdmissionByPatient(patient);
        if (activeAdmission == null) {
            notify("No active admission found for patient: " + patient.getName());
            return false;
        }
        return transferPatient(activeAdmission, newWard);
    }

    // UC05 - Update Bed Status
    public void updateBedStatus(Bed bed, BedStatus newStatus) {
        // UC05 alt flow 3a - a bed still held by an active admission cannot be freed or reserved,
        // otherwise the same bed could be handed to a second patient.
        Admission holder = dataStore.findActiveAdmissionByBed(bed);
        if (holder != null && newStatus != BedStatus.OCCUPIED) {
            throw new IllegalStateException("Bed " + bed.getBedId() + " is still occupied by "
                    + holder.getPatient().getName() + ". Discharge or transfer the patient first.");
        }
        bed.updateBedStatus(newStatus); // throws IllegalStateException on invalid transition
    }

    // UC03 - Generate System Report
    public Report generateReport(String type, String period) {
        return generateReport(type, period, false);
    }

    public Report generateReport(String type, String period, boolean includeUserData) {
        Report report = new Report(generateId("R"), type, period);
        
        if ("Occupancy".equals(type)) {
            // UC03 S1 - retrieve all Wards and their Beds
            java.util.List<String> occupancyData = new java.util.ArrayList<>();
            
            for (Ward ward : dataStore.findAllWards()) {
                int totalBeds = ward.getBeds().size();
                int occupiedBeds = 0;
                
                for (Bed bed : ward.getBeds()) {
                    if (bed.getStatus() == BedStatus.OCCUPIED) {
                        occupiedBeds++;
                    }
                }
                
                // UC03 S1 - calculate occupancy rate per Ward
                double occupancyRate = totalBeds == 0
                        ? 0.0
                        : (occupiedBeds * 100.0) / totalBeds;
                
                occupancyData.add(
                        ward.getWardName() + ": "
                        + occupiedBeds + "/" + totalBeds
                        + " beds occupied ("
                        + String.format("%.1f", occupancyRate)
                        + "%)"
                );
            }
            
            report.setData(occupancyData);
            
        } else if ("Admissions".equals(type)) {
            // UC03 S2 - retrieve all Admission records
            java.util.List<Admission> admissions = dataStore.findAllAdmissions();
            
            int admissionCount = admissions.size();
            long totalStayDays = 0;
            int completedAdmissions = 0;
            
            // UC03 S2 - calculate average stay length
            for (Admission admission : admissions) {
                if (admission.getDischargeDate() != null) {
                    long stayDays = java.time.temporal.ChronoUnit.DAYS.between(
                            admission.getAdmissionDate(),
                            admission.getDischargeDate()
                    );
                    totalStayDays += stayDays;
                    completedAdmissions++;
                }
            }
            
            double averageStay = completedAdmissions == 0
                    ? 0.0
                    : (double) totalStayDays / completedAdmissions;
            
            java.util.List<String> admissionData = new java.util.ArrayList<>();
            admissionData.add("Total admissions: " + admissionCount);
            admissionData.add(
                    "Average stay length: "
                    + String.format("%.1f", averageStay)
                    + " days"
            );
            
            report.setData(admissionData);
        }

        if (includeUserData) {
            report.setUserData(dataStore.findAllUsers());
        }
        return report;
    }

    // UC04 - Assign Nurse to Ward
    public boolean assignNurseToWard(Nurse nurse, Ward ward, String shift) {
        return assignNurseToWard(nurse, ward, shift, false);
    }

    // UC04 alt flow 3a - the System warns the Admin of the conflict; the Admin then either
    // changes the selection or relocates the Nurse (relocate = true).
    public boolean assignNurseToWard(Nurse nurse, Ward ward, String shift, boolean relocate) {
        boolean conflict = nurse.getAssignedWard() != null
                && nurse.getAssignedWard() != ward
                && nurse.getShift() != null
                && nurse.getShift().equalsIgnoreCase(shift);

        if (conflict && !relocate) {
            return false; // UI shows the conflict warning and asks the Admin to confirm
        }

        Ward previous = nurse.getAssignedWard();
        nurse.updateAssignment(ward, shift);

        // UC04 flow 4 - notify the nurse being (re)assigned specifically, not everyone
        if (conflict) {
            notify("Nurse " + nurse.getName() + " relocated from " + previous.getWardName()
                    + " to " + ward.getWardName(), nurse.getUserId());
        } else {
            notify("Nurse " + nurse.getName() + " assigned to " + ward.getWardName(), nurse.getUserId());
        }
        return true;
    }

    // UC09 Sub-flow S1 - Create a new user account
    public void createUser(User user) {
        dataStore.saveUser(user);
    }

    // UC09 Sub-flow S3 - Update contact details
    public void updateUserContact(User user, String newContact) {
        user.updateContact(newContact);
    }

    // UC09 Sub-flow S4 - Deactivate an account
    public void deactivateUser(User user) {
        user.deactivate();
    }

    // Used by every method above - matches the "self-notify" call in the sequence diagrams.
    // Broadcast version: recipientId stays null, every actor sees it.
    public void notify(String message) {
        Notification n = new Notification(message);
        dataStore.saveNotification(n);
        System.out.println("[ALERT] " + message); // UI layer shows a real pop-up for live sessions
    }

    // Targeted version: only the named recipient sees it.
    public void notify(String message, String recipientId) {
        Notification n = new Notification(message);
        n.setRecipientId(recipientId);
        dataStore.saveNotification(n);
        System.out.println("[ALERT to " + recipientId + "] " + message);
    }

    // Helper for capacity alerts - finds every currently active user whose role permits
    // managing wards (i.e. Admin), since a capacity issue is an administrative concern.
    private void notifyAllWithPermission(String message, String requiredPermission) {
        boolean anyNotified = false;
        for (User u : dataStore.findAllUsers()) {
            if (u.isActive() && u.getPermissions().contains(requiredPermission)) {
                notify(message, u.getUserId());
                anyNotified = true;
            }
        }
        if (!anyNotified) {
            notify(message); // fall back to broadcast if nobody currently holds that permission
        }
    }

    // Helper for UC02 - finds the nurse currently assigned to a given ward, if any
    private Nurse findNurseForWard(Ward ward) {
        for (User u : dataStore.findAllUsers()) {
            if (u instanceof Nurse n && n.getAssignedWard() == ward) return n;
        }
        return null;
    }

    private String generateId(String prefix) {
        return prefix + System.currentTimeMillis();
    }
}