package com.saisony.jobtracker.job.service;
import com.saisony.jobtracker.job.dto.UpdateJobApplicationRequest;
import com.saisony.jobtracker.job.dto.JobApplicationResponse;
import com.saisony.jobtracker.job.entity.ApplicationStatus;
import com.saisony.jobtracker.job.entity.JobApplication;
import com.saisony.jobtracker.job.repository.JobApplicationRepository;
import com.saisony.jobtracker.user.repository.UserRepository;
import com.saisony.jobtracker.exception.ResourceNotFoundException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.saisony.jobtracker.job.dto.UpdateJobApplicationRequest;
import java.util.Optional;
import com.saisony.jobtracker.job.dto.CreateJobApplicationRequest;
import com.saisony.jobtracker.user.entity.User;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobApplication jobApplication;

    private JobApplicationService service;

    @Mock
    private UpdateJobApplicationRequest updateRequest;

    @Mock
    private CreateJobApplicationRequest createRequest;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        service = new JobApplicationService(
                jobApplicationRepository,
                userRepository
        );
    }

    @Test
    void getByIdReturnsOwnedJobApplication() {

        when(jobApplicationRepository.findByIdAndUser_Email(
                1L,
                "test2@example.com"
        )).thenReturn(Optional.of(jobApplication));

        when(jobApplication.getId()).thenReturn(1L);
        when(jobApplication.getCompanyName()).thenReturn("Amazon");
        when(jobApplication.getJobTitle()).thenReturn("Software Engineer");
        when(jobApplication.getLocation()).thenReturn("New York");
        when(jobApplication.getStatus())
                .thenReturn(ApplicationStatus.INTERVIEW);

        JobApplicationResponse response =
                service.getById(
                        1L,
                        "test2@example.com"
                );

        assertEquals(1L, response.getId());
        assertEquals("Amazon", response.getCompanyName());
        assertEquals(
                "Software Engineer",
                response.getJobTitle()
        );
        assertEquals("New York", response.getLocation());
        assertEquals(
                ApplicationStatus.INTERVIEW,
                response.getStatus()
        );

        verify(jobApplicationRepository)
                .findByIdAndUser_Email(
                        1L,
                        "test2@example.com"
                );
    }
    @Test
    void getByIdThrowsNotFoundWhenApplicationDoesNotExistForUser() {

        when(jobApplicationRepository.findByIdAndUser_Email(
                999L,
                "test2@example.com"
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getById(
                        999L,
                        "test2@example.com"
                )
        );

        verify(jobApplicationRepository)
                .findByIdAndUser_Email(
                        999L,
                        "test2@example.com"
                );
    }
    @Test
    void deleteRemovesOwnedJobApplication() {

        when(jobApplicationRepository.findByIdAndUser_Email(
                1L,
                "test2@example.com"
        )).thenReturn(Optional.of(jobApplication));

        service.delete(
                1L,
                "test2@example.com"
        );

        verify(jobApplicationRepository)
                .findByIdAndUser_Email(
                        1L,
                        "test2@example.com"
                );

        verify(jobApplicationRepository)
                .delete(jobApplication);
    }
    @Test
    void updateChangesFieldsForOwnedJobApplication() {

        when(jobApplicationRepository.findByIdAndUser_Email(
                1L,
                "test2@example.com"
        )).thenReturn(Optional.of(jobApplication));

        when(updateRequest.getCompanyName())
                .thenReturn("Google");

        when(updateRequest.getStatus())
                .thenReturn(ApplicationStatus.OFFER);

        when(jobApplicationRepository.saveAndFlush(jobApplication))
                .thenReturn(jobApplication);

        when(jobApplication.getId()).thenReturn(1L);
        when(jobApplication.getCompanyName()).thenReturn("Google");
        when(jobApplication.getStatus())
                .thenReturn(ApplicationStatus.OFFER);

        JobApplicationResponse response =
                service.update(
                        1L,
                        "test2@example.com",
                        updateRequest
                );

        assertEquals(
                "Google",
                response.getCompanyName()
        );

        assertEquals(
                ApplicationStatus.OFFER,
                response.getStatus()
        );

        verify(jobApplication)
                .setCompanyName("Google");

        verify(jobApplication)
                .setStatus(ApplicationStatus.OFFER);

        verify(jobApplicationRepository)
                .saveAndFlush(jobApplication);
    }
    @Test
    void createSavesJobApplicationWithDefaultSavedStatus() {

        when(userRepository.findByEmail(
                "test2@example.com"
        )).thenReturn(Optional.of(user));

        when(createRequest.getCompanyName())
                .thenReturn("Amazon");

        when(createRequest.getJobTitle())
                .thenReturn("Software Engineer");

        when(createRequest.getLocation())
                .thenReturn("New York");

        when(createRequest.getStatus())
                .thenReturn(null);

        when(jobApplicationRepository.saveAndFlush(
                any(JobApplication.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationResponse response =
                service.create(
                        "test2@example.com",
                        createRequest
                );

        assertEquals(
                "Amazon",
                response.getCompanyName()
        );

        assertEquals(
                "Software Engineer",
                response.getJobTitle()
        );

        assertEquals(
                ApplicationStatus.SAVED,
                response.getStatus()
        );

        ArgumentCaptor<JobApplication> captor =
                ArgumentCaptor.forClass(JobApplication.class);

        verify(jobApplicationRepository)
                .saveAndFlush(captor.capture());

        JobApplication savedApplication =
                captor.getValue();

        assertSame(
                user,
                savedApplication.getUser()
        );

        assertEquals(
                ApplicationStatus.SAVED,
                savedApplication.getStatus()
        );
    }
    @Test
    void getAllForUserPaginatedReturnsFilteredPage() {

        JobApplication application = new JobApplication();
        application.setCompanyName("Amazon");
        application.setJobTitle("Software Engineer");
        application.setStatus(ApplicationStatus.INTERVIEW);

        PageImpl<JobApplication> applicationPage =
                new PageImpl<>(List.of(application));

        when(jobApplicationRepository.findFiltered(
                org.mockito.ArgumentMatchers.eq("test2@example.com"),
                org.mockito.ArgumentMatchers.eq(ApplicationStatus.INTERVIEW),
                org.mockito.ArgumentMatchers.eq("Amazon"),
                any(Pageable.class)
        )).thenReturn(applicationPage);

        var response =
                service.getAllForUserPaginated(
                        "test2@example.com",
                        ApplicationStatus.INTERVIEW,
                        "Amazon",
                        0,
                        10
                );

        assertEquals(1, response.getTotalElements());
        assertEquals(
                "Amazon",
                response.getContent().get(0).getCompanyName()
        );

        assertEquals(
                ApplicationStatus.INTERVIEW,
                response.getContent().get(0).getStatus()
        );

        verify(jobApplicationRepository)
                .findFiltered(
                        org.mockito.ArgumentMatchers.eq("test2@example.com"),
                        org.mockito.ArgumentMatchers.eq(ApplicationStatus.INTERVIEW),
                        org.mockito.ArgumentMatchers.eq("Amazon"),
                        any(Pageable.class)
                );
    }

}