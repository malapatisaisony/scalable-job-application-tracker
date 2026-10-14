package com.saisony.jobtracker.job.controller;

import com.saisony.jobtracker.job.dto.CreateJobApplicationRequest;
import com.saisony.jobtracker.job.dto.JobApplicationResponse;
import com.saisony.jobtracker.job.dto.UpdateJobApplicationRequest;
import com.saisony.jobtracker.job.entity.ApplicationStatus;
import com.saisony.jobtracker.job.service.JobApplicationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/job-applications")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(
            JobApplicationService jobApplicationService) {

        this.jobApplicationService = jobApplicationService;
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateJobApplicationRequest request) {

        JobApplicationResponse response =
                jobApplicationService.create(
                        jwt.getSubject(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<JobApplicationResponse>> getAll(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be 0 or greater"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Size must be between 1 and 100"
            );
        }

        Page<JobApplicationResponse> response =
                jobApplicationService.getAllForUserPaginated(
                        jwt.getSubject(),
                        status,
                        search,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        JobApplicationResponse response =
                jobApplicationService.getById(
                        id,
                        jwt.getSubject()
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> update(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateJobApplicationRequest request) {

        JobApplicationResponse response =
                jobApplicationService.update(
                        id,
                        jwt.getSubject(),
                        request
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        jobApplicationService.delete(
                id,
                jwt.getSubject()
        );

        return ResponseEntity.noContent().build();
    }
}