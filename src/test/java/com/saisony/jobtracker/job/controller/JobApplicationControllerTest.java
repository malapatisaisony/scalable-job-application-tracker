package com.saisony.jobtracker.job.controller;

import com.saisony.jobtracker.job.dto.JobApplicationResponse;
import com.saisony.jobtracker.job.service.JobApplicationService;
import com.saisony.jobtracker.job.dto.CreateJobApplicationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.saisony.jobtracker.job.dto.UpdateJobApplicationRequest;
import com.saisony.jobtracker.job.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationControllerTest {

    @Mock
    private JobApplicationService jobApplicationService;

    @Mock
    private Jwt jwt;

    @Mock
    private JobApplicationResponse jobApplicationResponse;

    private JobApplicationController controller;

    @Mock
    private CreateJobApplicationRequest createRequest;

    @Mock
    private UpdateJobApplicationRequest updateRequest;

    @Mock
    private Page<JobApplicationResponse> pageResponse;

    @BeforeEach
    void setUp() {
        controller =
                new JobApplicationController(jobApplicationService);
    }

    @Test
    void getByIdReturnsJobApplicationForAuthenticatedUser() {

        when(jwt.getSubject())
                .thenReturn("test2@example.com");

        when(jobApplicationService.getById(
                1L,
                "test2@example.com"
        )).thenReturn(jobApplicationResponse);

        ResponseEntity<JobApplicationResponse> response =
                controller.getById(1L, jwt);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertSame(
                jobApplicationResponse,
                response.getBody()
        );

        verify(jobApplicationService)
                .getById(
                        1L,
                        "test2@example.com"
                );
    }
    @Test
    void deleteReturnsNoContentForAuthenticatedUser() {

        when(jwt.getSubject())
                .thenReturn("test2@example.com");

        ResponseEntity<Void> response =
                controller.delete(1L, jwt);

        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        verify(jobApplicationService)
                .delete(
                        1L,
                        "test2@example.com"
                );
    }
    @Test
    void createReturnsCreatedJobApplication() {

        when(jwt.getSubject())
                .thenReturn("test2@example.com");

        when(jobApplicationService.create(
                "test2@example.com",
                createRequest
        )).thenReturn(jobApplicationResponse);

        ResponseEntity<JobApplicationResponse> response =
                controller.create(jwt, createRequest);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertSame(
                jobApplicationResponse,
                response.getBody()
        );

        verify(jobApplicationService)
                .create(
                        "test2@example.com",
                        createRequest
                );
    }
    @Test
    void updateReturnsUpdatedJobApplication() {

        when(jwt.getSubject())
                .thenReturn("test2@example.com");

        when(jobApplicationService.update(
                1L,
                "test2@example.com",
                updateRequest
        )).thenReturn(jobApplicationResponse);

        ResponseEntity<JobApplicationResponse> response =
                controller.update(
                        1L,
                        jwt,
                        updateRequest
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertSame(
                jobApplicationResponse,
                response.getBody()
        );

        verify(jobApplicationService)
                .update(
                        1L,
                        "test2@example.com",
                        updateRequest
                );
    }
    @Test
    void getAllReturnsPaginatedJobApplications() {

        when(jwt.getSubject())
                .thenReturn("test2@example.com");

        when(jobApplicationService.getAllForUserPaginated(
                "test2@example.com",
                ApplicationStatus.INTERVIEW,
                "Amazon",
                0,
                10
        )).thenReturn(pageResponse);

        ResponseEntity<Page<JobApplicationResponse>> response =
                controller.getAll(
                        jwt,
                        ApplicationStatus.INTERVIEW,
                        "Amazon",
                        0,
                        10
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertSame(
                pageResponse,
                response.getBody()
        );

        verify(jobApplicationService)
                .getAllForUserPaginated(
                        "test2@example.com",
                        ApplicationStatus.INTERVIEW,
                        "Amazon",
                        0,
                        10
                );
    }
}