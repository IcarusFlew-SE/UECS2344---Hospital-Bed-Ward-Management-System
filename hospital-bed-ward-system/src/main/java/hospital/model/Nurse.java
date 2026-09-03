package hospital.model;

import java.util.*;

public class Nurse extends User{
	private String shift;
	private Ward assignedWard;
	
	public Nurse(String userId, String name, String contact, String email, String shift) {
		super(userId, name, contact, email);
		this.shift = shift;
	}
	
	public String getShift() {return shift;}
	public Ward getAssignedWard() {return assignedWard;}
	
	@Override
	public List<String> getPermissions() {
		return List.of("UPDATE_BED_STATUS", "VIEW_WARD_ASSIGNMENT");
	}
	
	public void updateAssignment(Ward ward, String shift) {
		this.assignedWard = ward;
		this.shift = shift;
	}
}
