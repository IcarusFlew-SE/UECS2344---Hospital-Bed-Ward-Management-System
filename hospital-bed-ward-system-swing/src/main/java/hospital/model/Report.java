package hospital.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Report {
	private String reportId;
    private String reportType;
    private String period;
    private LocalDate generatedDate;
    private List<?> data;
    private List<?> userData = new ArrayList<>();

    public Report(String reportId, String reportType, String period) {
        this.reportId = reportId;
        this.reportType = reportType;
        this.period = period;
        this.generatedDate = LocalDate.now();
        this.data = new ArrayList<>();
    }

    public String getReportId() { return reportId; }
    public String getReportType() { return reportType; }
    public String getPeriod() { return period; }
    public LocalDate getGeneratedDate() { return generatedDate; }
    
    public void setData(List<?> data) {this.data = data;}
    public List<?> getData() {return data;}

    public void setUserData(List<?> userData) { this.userData = userData; }
    public List<?> getUserData() { return userData; }
}
