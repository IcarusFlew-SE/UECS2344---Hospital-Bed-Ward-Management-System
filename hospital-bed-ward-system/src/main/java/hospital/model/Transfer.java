package hospital.model;

import java.time.LocalDate;

public class Transfer {
	private String transferId;
    private Ward fromWard;
    private Ward toWard;
    private Bed toBed;
    private LocalDate transferDate;
    private String description;
    
    public Transfer(String transferId, Ward fromWard, Ward toWard, Bed toBed) {
        this.transferId = transferId;
        this.fromWard = fromWard;
        this.toWard = toWard;
        this.toBed = toBed;
        this.transferDate = LocalDate.now();
    }
    
    public Ward getFromWard() { return fromWard; }
    public Ward getToWard() { return toWard; }
    public Bed getToBed() { return toBed; }
    
    public String getSummary() {
        return "Transferred from " + fromWard.getWardName() + " to " + toWard.getWardName()
                + " on " + transferDate;
    }
}
