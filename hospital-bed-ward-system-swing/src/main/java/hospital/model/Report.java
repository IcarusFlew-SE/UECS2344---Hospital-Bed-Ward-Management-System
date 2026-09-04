package hospital.model;

import java.time.LocalDate;
import java.util.*;

public class Report {
	private String reportId;
    private String reportType;
    private LocalDate generatedDate;
    private List<?> data;
    
    public Report(String reportId, String reportType, String period) {
        this.reportId = reportId;
        this.reportType = reportType;
        this.generatedDate = LocalDate.now();
    }
    
    public String getReportId() { return reportId; }
    public String getReportType() { return reportType; }
    public LocalDate getGeneratedDate() { return generatedDate; }
    
    public void setData(List<?> data) {this.data = data;}
    public List<?> getData() {return data;}
}
