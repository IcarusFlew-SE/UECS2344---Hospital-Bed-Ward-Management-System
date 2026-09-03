package hospital.model;

import java.util.*;

public class Admin extends User{
	private String accessLevel;
	
	public Admin(String userId, String name, String contact, String email, String accessLevel) {
		super(userId, name, contact, email);
		this.accessLevel = accessLevel;
	}
	
	@Override
	public List<String> getPermissions() {
		return List.of("MANAGE_USERS", "MANAGE_WARDS", "GENERATE_REPORT", "ASSIGN_NURSE");
	}
	
	public String getAccessLevel() {return accessLevel;}
}
