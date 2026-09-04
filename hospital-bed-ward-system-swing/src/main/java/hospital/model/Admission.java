package hospital.model;

import java.time.LocalDate;

public class Admission {
	private String admissionId;
	private Patient patient;
	private Bed bed;
	private Ward ward;
	private LocalDate admissionDate;
	private LocalDate dischargeDate;
	private AdmissionStatus status;
	
	public Admission(String admissionId, Patient patient, Bed bed, Ward ward) {
		this.admissionId = admissionId;
		this.patient = patient;
		this.bed = bed;
		this.ward = ward;
		this.admissionDate = LocalDate.now();
		this.status = AdmissionStatus.ADMITTED;
	}
	
	public String getAdmissionId() {return admissionId;}
	public Patient getPatient() {return patient;}
	public Bed getBed() {return bed;}
	public Ward getWard() {return ward;}
	public AdmissionStatus getStatus() {return status;}
	
	public void discharge() {
		this.status = AdmissionStatus.DISCHARGED;
		this.dischargeDate = LocalDate.now();
		this.bed.updateBedStatus(BedStatus.CLEANING);
	}
	
	public void cancel() {
		if (this.status != AdmissionStatus.ADMITTED) {
			throw new IllegalStateException("Only an active admission can be cancelled.");
		}
		this.status = AdmissionStatus.CANCELLED;
		this.bed.updateBedStatus(BedStatus.AVAILABLE);
	}
	
	 public void recordTransfer(Transfer t) {
	        this.status = AdmissionStatus.TRANSFERRED;
	        this.ward = t.getToWard();
	        this.bed = t.getToBed();
	    }
}
