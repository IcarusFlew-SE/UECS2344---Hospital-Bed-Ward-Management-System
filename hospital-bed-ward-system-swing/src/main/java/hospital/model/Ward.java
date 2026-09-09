package hospital.model;

import java.util.*;

public class Ward {
    private String wardId;
    private String wardName;
    private int capacity;
    private String wardType;
    private List<Bed> beds;
    
    // Create Ward. Null bed list becomes empty; non-positive capacity defaults to bed list size.
    public Ward(String wardId, String wardName, int capacity, String wardType, List<Bed> beds) {
        this.wardId = wardId;
        this.wardName = wardName;
        this.wardType = wardType;
        this.beds = beds == null ? new ArrayList<>() : new ArrayList<>(beds);
        this.capacity = capacity > 0 ? capacity : this.beds.size();
    }
    
    public String getWardId() { return wardId; }
    public String getWardName() { return wardName; }
    public int getCapacity() { return capacity; }
    public String getWardType() { return wardType; }
    // Return unmodifiable view of beds
    public List<Bed> getBeds() { return Collections.unmodifiableList(beds); }

    // Number of occupied beds
    public int getOccupiedCount() {
        int count = 0;
        for (Bed bed : beds) {
            if (bed.getStatus() == BedStatus.OCCUPIED) count++;
        }
        return count;
    }
    
    public Bed findAvailableBed() {
        for (Bed bed : beds) {
            if (bed.isAvailable()) {
                return bed;
            }
        }
        return null;
    }
    
    // Returns true when ward is full (checks capacity and availability defensively)
    public boolean isAtCapacity() {
        int effectiveCapacity = Math.min(capacity, beds.size());
        return getOccupiedCount() >= effectiveCapacity || findAvailableBed() == null;
    }

    // Convenience mutators used by controller/data store in this demo.
    public void addBed(Bed bed) {
        if (bed != null) beds.add(bed);
    }

    public boolean removeBed(Bed bed) {
        if (bed == null) return false;
        return beds.removeIf(b -> b.getBedId().equals(bed.getBedId()));
    }

    @Override
    public String toString() {
        return wardName + " (" + wardId + ")";
    }
}