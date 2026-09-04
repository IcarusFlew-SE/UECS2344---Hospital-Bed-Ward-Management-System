package hospital.controller;

import hospital.data.HospitalDataStore;
import hospital.model.*;

public class HospitalController {
	private HospitalDataStore dataStore;
	
	public HospitalController(HospitalDataStore dataStore) {
		this.dataStore = dataStore;
	}
	
	// UC01 - Manage Patient Admission
    public Admission admitPatient(Patient patient, Ward ward) {
        Bed bed = dataStore.findAvailableBed(ward);
        if (bed == null) {
            notify("No bed available in ward " + ward.getWardName());
            return null;
        }
        bed.updateBedStatus(BedStatus.OCCUPIED);
        Admission admission = new Admission(generateId("A"), patient, bed, ward);
        dataStore.saveAdmission(admission);

        if (ward.isAtCapacity()) {
            notify("Ward " + ward.getWardName() + " has reached capacity.");
        }
        return admission;
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
        Transfer transfer = new Transfer(generateId("T"), admission.getWard(), newWard, newBed);
        admission.recordTransfer(transfer);
        notify("Patient transferred to " + newWard.getWardName());
        return true;
    }

    // UC05 - Update Bed Status
    public void updateBedStatus(Bed bed, BedStatus newStatus) {
        bed.updateBedStatus(newStatus); // throws IllegalStateException on invalid transition
    }

    // UC03 - Generate System Report
    public Report generateReport(String type, String period) {
        Report report = new Report(generateId("R"), type, period);
        if ("Occupancy".equals(type)) {
            report.setData(dataStore.findAllWards());
        } else if ("Admissions".equals(type)) {
            report.setData(dataStore.findAllAdmissions());
        }
        return report;
    }

    // UC04 - Assign Nurse to Ward
    public boolean assignNurseToWard(Nurse nurse, Ward ward, String shift) {
        boolean conflict = nurse.getAssignedWard() != null && nurse.getAssignedWard() != ward;
        if (conflict) {
            return false; // UI shows conflict warning, does not override automatically
        }
        nurse.updateAssignment(ward, shift);
        notify("Nurse " + nurse.getName() + " assigned to " + ward.getWardName());
        return true;
    }

    // Used by every method above - matches the "self-notify" call in the sequence diagrams
    public void notify(String message) {
        Notification n = new Notification(message);
        dataStore.saveNotification(n);
        System.out.println("[ALERT] " + message); // UI layer shows a real pop-up for live sessions
    }

    private String generateId(String prefix) {
        return prefix + System.currentTimeMillis();
    }
}
