package hospital.model;

import java.util.*;

public abstract class User {
	private String userId;
	private String name;
	private String contact;
	private String email;
	private boolean active;
	
	protected User(String userId, String name, String contact, String email) {
		this.userId = userId;
		this.name = name;
		this.contact = contact;
		this.email = email;
	}
	
	public abstract List<String> getPermissions();
	
	public String getUserId() {return userId;}
	public String getName() {return name;}
	public String getContact() {return contact;}
	public String getEmail() {return email;}
	public boolean isActive() {return active;}
	
	public void updateContact(String contact) {
		this.contact = contact;
	}
	
	public void deactivate() {
		this.active = false;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
