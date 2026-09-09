package hospital.data;

import hospital.model.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * DataLoader - Loads seed demo data from a text file
 * File format: sections marked by [SECTION_NAME] with pipe-delimited fields
 */
public class DataLoader {
    
    private static final String FILE_PATH = "/data.txt";
    
    public static class SeedData {
        public List<Ward> wards;
        public List<Patient> patients;
        public List<Nurse> nurses;
        public List<Doctor> doctors;
        public List<Admin> admins;
        
        public SeedData() {
            this.wards = new ArrayList<>();
            this.patients = new ArrayList<>();
            this.nurses = new ArrayList<>();
            this.doctors = new ArrayList<>();
            this.admins = new ArrayList<>();
        }
    }
    
    public static SeedData loadSeedData() throws IOException {
        SeedData data = new SeedData();
        InputStream is = DataLoader.class.getResourceAsStream(FILE_PATH);
        if (is == null) {
            throw new IOException("Seed data file not found: " + FILE_PATH);
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        String currentSection = null;
        
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            
            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            // Identify section
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1).toUpperCase();
                continue;
            }
            
            // Parse data based on current section
            try {
                switch (currentSection) {
                    case "WARDS":
                        parseWard(line, data);
                        break;
                    case "PATIENTS":
                        parsePatient(line, data);
                        break;
                    case "NURSES":
                        parseNurse(line, data);
                        break;
                    case "DOCTORS":
                        parseDoctor(line, data);
                        break;
                    case "ADMINS":
                        parseAdmin(line, data);
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error parsing line: " + line);
                e.printStackTrace();
            }
        }
        
        reader.close();
        return data;
    }
    
    private static void parseWard(String line, SeedData data) {
        String[] parts = line.split("\\|");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Ward line must have 5 fields: wardId|wardName|capacity|wardType|bedIds");
        }
        
        String wardId = parts[0].trim();
        String wardName = parts[1].trim();
        int capacity = Integer.parseInt(parts[2].trim());
        String wardType = parts[3].trim();
        String[] bedIds = parts[4].trim().split(",");
        
        List<Bed> beds = new ArrayList<>();
        for (String bedId : bedIds) {
            beds.add(new Bed(bedId.trim()));
        }
        
        Ward ward = new Ward(wardId, wardName, capacity, wardType, beds);
        data.wards.add(ward);
    }
    
    private static void parsePatient(String line, SeedData data) {
        String[] parts = line.split("\\|");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Patient line must have 5 fields: userId|name|phone|email|dateOfBirth");
        }
        
        String userId = parts[0].trim();
        String name = parts[1].trim();
        String phone = parts[2].trim();
        String email = parts[3].trim();
        LocalDate dob = LocalDate.parse(parts[4].trim());
        
        Patient patient = new Patient(userId, name, phone, email, dob);
        data.patients.add(patient);
    }
    
    private static void parseNurse(String line, SeedData data) {
        String[] parts = line.split("\\|");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Nurse line must have 5 fields: userId|name|phone|email|shift");
        }
        
        String userId = parts[0].trim();
        String name = parts[1].trim();
        String phone = parts[2].trim();
        String email = parts[3].trim();
        String shift = parts[4].trim();
        
        Nurse nurse = new Nurse(userId, name, phone, email, shift);
        data.nurses.add(nurse);
    }
    
    private static void parseDoctor(String line, SeedData data) {
        String[] parts = line.split("\\|");
        if (parts.length < 6) {
            throw new IllegalArgumentException("Doctor line must have 6 fields: userId|name|phone|email|specialty|department");
        }
        
        String userId = parts[0].trim();
        String name = parts[1].trim();
        String phone = parts[2].trim();
        String email = parts[3].trim();
        String specialty = parts[4].trim();
        String dept = parts[5].trim();
        
        Doctor doctor = new Doctor(userId, name, phone, email, dept, specialty);
        data.doctors.add(doctor);
    }
    
    private static void parseAdmin(String line, SeedData data) {
        String[] parts = line.split("\\|");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Admin line must have 5 fields: userId|name|phone|email|accessLevel");
        }
        
        String userId = parts[0].trim();
        String name = parts[1].trim();
        String phone = parts[2].trim();
        String email = parts[3].trim();
        String accessLevel = parts[4].trim();
        
        Admin admin = new Admin(userId, name, phone, email, accessLevel);
        data.admins.add(admin);
    }
}
