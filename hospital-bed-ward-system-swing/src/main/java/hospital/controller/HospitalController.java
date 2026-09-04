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
        // UC01 alt flow 3a - Patient already has an active admission (assumption 2)
        Admission existing = dataStore.findActiveAdmissionByPatient(patient);
        if (existing != null) {
            throw new IllegalStateException(patient.getName()
                    + " already has an active admission in " + existing.getWard().getWardName() + ".");
        }

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
        return assignNurseToWard(nurse, ward, shift, false);
    }

    // UC04 alt flow 3a - the System warns the Admin of the conflict; the Admin then either
    // changes the selection or relocates the Nurse (relocate = true).
    public boolean assignNurseToWard(Nurse nurse, Ward ward, String shift, boolean relocate) {
        boolean conflict = nurse.getAssignedWard() != null && nurse.getAssignedWard() != ward;
        if (conflict && !relocate) {
            return false; // UI shows the conflict warning and asks the Admin to confirm
        }
        Ward previous = nurse.getAssignedWard();
        nurse.updateAssignment(ward, shift);
        if (conflict) {
            notify("Nurse " + nurse.getName() + " relocated from " + previous.getWardName()
                    + " to " + ward.getWardName());
        } else {
            notify("Nurse " + nurse.getName() + " assigned to " + ward.getWardName());
        }
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
