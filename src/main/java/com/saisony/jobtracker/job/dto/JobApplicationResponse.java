package com.saisony.jobtracker.job.dto;

import com.saisony.jobtracker.job.entity.ApplicationStatus;

import java.time.OffsetDateTime;

import java.io.Serializable;

public class JobApplicationResponse implements Serializable {

    private Long id;
    private String companyName;
    private String jobTitle;
    private String jobUrl;
    private String location;
    private ApplicationStatus status;
    private OffsetDateTime appliedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public JobApplicationResponse() {
    }

    public JobApplicationResponse(
            Long id,
            String companyName,
            String jobTitle,
            String jobUrl,
            String location,
            ApplicationStatus status,
            OffsetDateTime appliedAt,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {

        this.id = id;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.jobUrl = jobUrl;
        this.location = location;
        this.status = status;
        this.appliedAt = appliedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public String getLocation() {
        return location;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public OffsetDateTime getAppliedAt() {
        return appliedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
