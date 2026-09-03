package hospital.model;

import java.util.*;

public class Ward {
	private String wardId;
    private String wardName;
    private int capacity;
    private String wardType;
    private List<Bed> beds;
    
    public Ward(String wardId, String wardName, int capacity, String wardType, List<Bed> beds) {
        this.wardId = wardId;
        this.wardName = wardName;
        this.capacity = capacity;
        this.wardType = wardType;
        this.beds = beds;
    }
    
    public String getWardId() { return wardId; }
    public String getWardName() { return wardName; }
    public int getCapacity() { return capacity; }
    public List<Bed> getBeds() { return beds; }
    
    public Bed findAvailableBed() {
        for (Bed bed : beds) {
            if (bed.isAvailable()) {
                return bed;
            }
        }
        return null;
    }
    
    public boolean isAtCapacity() {
        return findAvailableBed() == null;
    }
}
