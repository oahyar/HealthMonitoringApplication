package com.example.HealthMonitoringApp.quartz.jobs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.quartz.*;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class DependentJob3Test {

    private DependentJob3 job;
    private JobExecutionContext context;
    private JobDetail jobDetail;
    private JobDataMap dataMap;
    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        job = Mockito.spy(new DependentJob3());
        context = mock(JobExecutionContext.class);
        jobDetail = mock(JobDetail.class);
        scheduler = mock(Scheduler.class);
        dataMap = new JobDataMap();
    }

    @Test
    void testExecute_success_shouldNotThrow() throws Exception {
        when(context.getMergedJobDataMap()).thenReturn(dataMap);
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(new JobKey("TestJob"));
        when(context.getScheduler()).thenReturn(scheduler);

        doReturn(false).when(job).shouldFail(); // simulate success

        job.execute(context);

        verify(scheduler, never()).scheduleJob(any(Trigger.class));
    }

    @Test
    void testExecute_failWithRetry_shouldScheduleRetry() throws Exception {
        dataMap.put("retryCount", 1);
        when(context.getMergedJobDataMap()).thenReturn(dataMap);
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(new JobKey("MyJob"));
        when(context.getScheduler()).thenReturn(scheduler);

        doReturn(true).when(job).shouldFail(); // simulate failure

        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOf(JobExecutionException.class)
                .hasMessageContaining("Simulated failure");

        verify(scheduler).scheduleJob(any(Trigger.class));
    }

    @Test
    void testExecute_failAtMaxRetry_shouldNotReschedule() throws Exception {
        dataMap.put("retryCount", 2); // last allowed retry
        when(context.getMergedJobDataMap()).thenReturn(dataMap);
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(new JobKey("MyJob_retry_2"));
        when(context.getScheduler()).thenReturn(scheduler);

        doReturn(true).when(job).shouldFail(); // simulate failure

        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOf(JobExecutionException.class)
                .hasMessageContaining("Simulated failure");

        verify(scheduler, never()).scheduleJob(any(Trigger.class));
    }

    @Test
    void testExecute_whenSchedulerFails_shouldThrowWrappedException() throws Exception {
        // Arrange
        JobDataMap retryDataMap = new JobDataMap();
        retryDataMap.put("retryCount", 0);

        JobExecutionContext context = mock(JobExecutionContext.class);
        JobDetail jobDetail = mock(JobDetail.class);
        Scheduler scheduler = mock(Scheduler.class);

        when(context.getMergedJobDataMap()).thenReturn(retryDataMap);
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(new JobKey("MyJob"));
        when(context.getScheduler()).thenReturn(scheduler);
        // Force failure on retry
        doReturn(true).when(job).shouldFail();

        // Stub addJob() to succeed
        doNothing().when(scheduler).addJob(any(JobDetail.class), eq(true));
        // Force scheduleJob to throw exception
        doThrow(new SchedulerException("Simulated scheduler failure"))
                .when(scheduler).scheduleJob(any(Trigger.class));

        // Act + Assert
        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOf(JobExecutionException.class)
                .hasMessageContaining("Retry scheduling failed")
                .hasCauseInstanceOf(SchedulerException.class);
    }

}
