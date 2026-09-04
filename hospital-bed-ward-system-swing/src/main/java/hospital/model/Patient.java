package hospital.model;

import java.util.*;
import java.time.LocalDate;

public class Patient extends User{
	private LocalDate dateOfBirth;
	
	public Patient(String userId, String name, String contact, String email, LocalDate dateOfBirth) {
        super(userId, name, contact, email);
        this.dateOfBirth = dateOfBirth;
    }
	
	public LocalDate getDateOfBirth() {return dateOfBirth;}
	
	@Override
	public List<String> getPermissions() {
		return List.of("VIEW_OWN_ADMISSION");
	}
}
