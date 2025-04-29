//package com.example.HealthMonitoringApp.quartz.jobs;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import com.example.HealthMonitoringApp.Entity.JobLog;
//import com.example.HealthMonitoringApp.Repository.JobLogRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.quartz.JobDataMap;
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;
//
//import java.time.LocalDateTime;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//public class ProcessingJobTest {
//
//    private ProcessingJob job;
//    private JobLogRepository jobLogRepository;
//    private JobExecutionContext context;
//
//    @BeforeEach
//    void setUp() {
//        jobLogRepository = mock(JobLogRepository.class);
//        context = mock(JobExecutionContext.class);
//
//        job = new ProcessingJob();
//        job.setJobLogRepository(jobLogRepository);
//    }
//
//    @Test
//    void testExecute_success_shouldLogSuccess() throws JobExecutionException {
//        // Arrange
//        JobDataMap dataMap = new JobDataMap();
//        dataMap.put("input", "hello");
//        when(context.getMergedJobDataMap()).thenReturn(dataMap);
//
//        // Act
//        job.execute(context);
//
//        // Assert saved log
//        ArgumentCaptor<JobLog> logCaptor = ArgumentCaptor.forClass(JobLog.class);
//        verify(jobLogRepository).save(logCaptor.capture());
//
//        JobLog savedLog = logCaptor.getValue();
//        assertThat(savedLog.getStatus()).isEqualTo("SUCCESS");
//        assertThat(savedLog.getMessage()).contains("Input: hello").contains("Output: HELLO");
//        assertThat(savedLog.getStartTime()).isNotNull();
//        assertThat(savedLog.getEndTime()).isNotNull();
//    }
//
//    @Test
//    void testExecute_whenExceptionThrown_shouldLogFailure() throws JobExecutionException {
//        // Arrange
//        JobDataMap dataMap = spy(new JobDataMap());
//        when(context.getMergedJobDataMap()).thenReturn(dataMap);
//        // Simulate exception on input retrieval
//        doThrow(new RuntimeException("Map failed")).when(dataMap).getString("input");
//
//        // Act
//        job.execute(context);
//
//        // Assert saved log
//        ArgumentCaptor<JobLog> logCaptor = ArgumentCaptor.forClass(JobLog.class);
//        verify(jobLogRepository).save(logCaptor.capture());
//
//        JobLog savedLog = logCaptor.getValue();
//        assertThat(savedLog.getStatus()).isEqualTo("FAILED");
//        assertThat(savedLog.getMessage()).contains("Error processing input");
//        assertThat(savedLog.getStartTime()).isNotNull();
//        assertThat(savedLog.getEndTime()).isNotNull();
//    }
//}