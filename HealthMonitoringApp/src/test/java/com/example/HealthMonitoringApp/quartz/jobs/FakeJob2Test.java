package com.example.HealthMonitoringApp.quartz.jobs;

import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class FakeJob2Test {

    private FakeJob2 job;
    private Scheduler scheduler;
    private JobLogRepository jobLogRepository;
    private JobExecutionContext context;

    @BeforeEach
    void setUp() {
        scheduler = mock(Scheduler.class);
        jobLogRepository = mock(JobLogRepository.class);
        context = mock(JobExecutionContext.class);

        job = new FakeJob2();
        job.scheduler = scheduler;
        job.jobLogRepository = jobLogRepository;
    }

    @Test
    void testExecute_success_shouldLogAndTriggerJob() throws Exception {
        // Setup
        JobKey jobKey = new JobKey("dependentJob2");
        when(scheduler.checkExists(jobKey)).thenReturn(false);
        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());

        // Act
        job.execute(context);

        // Assert log is saved
        ArgumentCaptor<JobLog> logCaptor = ArgumentCaptor.forClass(JobLog.class);
        verify(jobLogRepository).save(logCaptor.capture());

        JobLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getJobName()).isEqualTo("dependentJob2");
        assertThat(savedLog.getStatus()).isEqualTo("WAITING");
        assertThat(savedLog.getMessage()).contains("scheduled to run in 5 minutes");

        // Assert addJob and scheduleJob were called
        verify(scheduler).addJob(any(JobDetail.class), eq(false));
        verify(scheduler).scheduleJob(any(Trigger.class));
    }

    @Test
    void testExecute_whenSchedulerFails_shouldThrowJobExecutionException() throws Exception {
        // Setup
        JobKey jobKey = new JobKey("dependentJob2");
        when(scheduler.checkExists(jobKey)).thenReturn(false);
        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());

        // Simulate failure on scheduler
        doThrow(new SchedulerException("Simulated scheduling failure"))
                .when(scheduler).scheduleJob(any(Trigger.class));

        // Act & Assert
        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOf(JobExecutionException.class)
                .hasCauseInstanceOf(SchedulerException.class)
                .hasMessageContaining("Simulated scheduling failure");

        // Ensure log is still saved before failure
        verify(jobLogRepository, times(1)).save(any(JobLog.class));
    }

    @Test
    void testExecute_whenJobKeyDoesNotExist_shouldAddJob() throws Exception {
        // Arrange
        JobKey jobKey = new JobKey("dependentJob2");
        when(scheduler.checkExists(jobKey)).thenReturn(false);
        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());

        // Act
        job.execute(context);

        // Assert: job should be added and scheduled
        verify(scheduler, times(1)).addJob(any(JobDetail.class), eq(false));
        verify(scheduler, times(1)).scheduleJob(any(Trigger.class));
    }

    @Test
    void testExecute_whenJobKeyExists_shouldNotCallAddJob() throws Exception {
        // Arrange
        JobKey jobKey = new JobKey("dependentJob2");
        when(scheduler.checkExists(jobKey)).thenReturn(true);
        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());

        // Act
        job.execute(context);

        // Assert: skip addJob, but still schedule
        verify(scheduler, never()).addJob(any(JobDetail.class), anyBoolean());
        verify(scheduler, times(1)).scheduleJob(any(Trigger.class));
        verify(jobLogRepository, times(1)).save(any(JobLog.class));
    }

}
