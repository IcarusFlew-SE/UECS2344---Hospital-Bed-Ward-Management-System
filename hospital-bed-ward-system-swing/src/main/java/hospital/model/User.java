package hospital.model;

import java.util.*;

public abstract class User {
	private String userId;
	private String name;
	private String contact;
	private String email;
	
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

}
