package hospital.model;

import java.util.*;

public class Doctor extends User {
	private String dept;
	private String specialization;
	
	public Doctor(String userId, String name, String contact, String email, String dept, String specialization) {
		super(userId, name, contact, email);
		this.dept = dept;
		this.specialization = specialization;
	}
	
	public String getDept() {return dept;}
	public String getSpecialization() {return specialization;}
	
	@Override
	public List<String> getPermissions() {
		return List.of("ADMIT_PATIENT", "TRANSFER_PATIENT", "VIEW_ADMISSION");
	}
}
