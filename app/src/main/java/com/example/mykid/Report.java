package com.example.mykid;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Comparator;

@Entity
public class Report implements Serializable {
    @PrimaryKey (autoGenerate = true)
    private int reportId;
    private String reportName;
    private String reportLocation;
    private String reportDate;
    private String reportTime;
    private String reporterName;
    private String reportImage;

    public Report(){ }

    public Report(@NonNull String reportName, String reportLocation, @NonNull String reportDate, @NonNull String reportTime, @NonNull String reporterName, String reportImage) {
        this.reportName = reportName;
        this.reportLocation = reportLocation;
        this.reportDate = reportDate;
        this.reportTime = reportTime;
        this.reporterName = reporterName;
        this.reportImage = reportImage;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportLocation() {
        return reportLocation;
    }

    public void setReportLocation(String reportLocation) {
        this.reportLocation = reportLocation;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public String getReportTime() {
        return reportTime;
    }

    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReportImage() { return reportImage; }

    public void setReportImage(String reportImage) { this.reportImage = reportImage; }

    public static Comparator<Report> name = new Comparator<Report>() {
        @Override
        public int compare(Report report, Report t1) {
            return report.getReportName().compareTo(t1.getReportName());
        }
    };
}
