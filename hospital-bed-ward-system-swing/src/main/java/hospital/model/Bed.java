package hospital.model;

import java.time.LocalDate;

public class Bed {
	private String bedId;
	private BedStatus status;
	private LocalDate lastCleanedDate;
	
	public Bed(String bedId) {
		this.bedId = bedId;
		this.status = BedStatus.AVAILABLE;
	}
	
	public boolean isAvailable() {
		return status == BedStatus.AVAILABLE;
	}
	
	public String getBedId() {return bedId;}
	public BedStatus getStatus() {return status;}
	public LocalDate getLasCleanedDate() {return lastCleanedDate;}
	
	public void updateBedStatus(BedStatus newStatus) {
		if (this.status == BedStatus.OCCUPIED && newStatus == BedStatus.RESERVED) {
			throw new IllegalStateException("Cannot use an occupied bed.");
		}
		this.status = newStatus;
		if (newStatus == BedStatus.CLEANING) {
			this.lastCleanedDate = LocalDate.now();
		}
	}
}
