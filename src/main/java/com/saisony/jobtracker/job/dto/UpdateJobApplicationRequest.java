package com.saisony.jobtracker.job.dto;

import com.saisony.jobtracker.job.entity.ApplicationStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class UpdateJobApplicationRequest {

    @Pattern(
            regexp = ".*\\S.*",
            message = "Company name must not be blank"
    )
    @Size(
            max = 255,
            message = "Company name must not exceed 255 characters"
    )
    private String companyName;

    @Pattern(
            regexp = ".*\\S.*",
            message = "Job title must not be blank"
    )
    @Size(
            max = 255,
            message = "Job title must not exceed 255 characters"
    )
    private String jobTitle;

    private String jobUrl;

    @Size(
            max = 255,
            message = "Location must not exceed 255 characters"
    )
    private String location;

    private ApplicationStatus status;

    private OffsetDateTime appliedAt;

    public UpdateJobApplicationRequest() {
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public OffsetDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(OffsetDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}