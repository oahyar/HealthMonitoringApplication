package com.example.HealthMonitoringApp.Service;

import static org.mockito.Mockito.*;
import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import com.example.HealthMonitoringApp.dto.JobDependencyDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Sort;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class JobMonitorServiceTest {

    @Mock
    private JobLogRepository jobLogRepository;
    @InjectMocks
    private JobMonitorService jobMonitorService;

    private Scheduler scheduler;



    @Test
    void testGetJobStatus_schedulerUnavailable_shouldReturnSchedulerUnavailable() throws SchedulerException {
        ObjectProvider<Scheduler> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);

        JobMonitorService service = new JobMonitorService(provider);
        String result = service.getJobStatus("myJob", "DEFAULT");

        assertEquals("SCHEDULER_UNAVAILABLE", result);
    }

    @Test
    void testGetJobStatus_jobNotFound_shouldReturnNotFound() throws SchedulerException {
        Scheduler scheduler = mock(Scheduler.class);
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        ObjectProvider<Scheduler> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(scheduler);

        JobMonitorService service = new JobMonitorService(provider);
        String result = service.getJobStatus("myJob", "DEFAULT");

        assertEquals("NOT_FOUND", result);
    }

    @Test
    void testGetJobStatus_noTrigger_shouldReturnNoTrigger() throws SchedulerException {
        // Arrange
        Scheduler scheduler = mock(Scheduler.class);
        ObjectProvider<Scheduler> provider =  mock(ObjectProvider.class);;
        JobKey jobKey = new JobKey("myJob", "DEFAULT");

        when(provider.getIfAvailable()).thenReturn(scheduler);
        when(scheduler.checkExists(eq(jobKey))).thenReturn(true);
        when(scheduler.getTriggersOfJob(eq(jobKey))).thenReturn(Collections.emptyList());

        JobMonitorService service = new JobMonitorService(provider);

        // Act
        String result = service.getJobStatus("myJob", "DEFAULT");

        // Assert
        assertEquals("NO_TRIGGER", result);
    }
    @Test
    @SuppressWarnings("unchecked")
    void testGetJobStatus_triggerExists_shouldReturnTriggerState() throws SchedulerException {
        // Arrange
        Scheduler scheduler = mock(Scheduler.class);
        ObjectProvider<Scheduler> provider = mock(ObjectProvider.class);
        Trigger trigger = mock(Trigger.class);
        TriggerKey triggerKey = new TriggerKey("trigger1");
        JobKey jobKey = new JobKey("myJob", "DEFAULT");

        when(provider.getIfAvailable()).thenReturn(scheduler);
        when(scheduler.checkExists(eq(jobKey))).thenReturn(true);
        List<Trigger> triggerList = List.of(trigger);
        when(scheduler.getTriggersOfJob(eq(jobKey))).thenReturn((List) triggerList);
        when(trigger.getKey()).thenReturn(triggerKey);
        when(scheduler.getTriggerState(triggerKey)).thenReturn(Trigger.TriggerState.NORMAL);

        JobMonitorService service = new JobMonitorService(provider);

        // Act
        String result = service.getJobStatus("myJob", "DEFAULT");

        // Assert
        assertEquals("NORMAL", result);
    }

    @Test
    void testGetJobGraphElements_returnsCorrectNodesAndEdges() {
        // Arrange
        JobLogRepository mockRepo = mock(JobLogRepository.class);
        ObjectProvider schedulerProvider = mock(ObjectProvider.class);
        JobMonitorService service = new JobMonitorService(schedulerProvider);
        service = spy(service); // for accessing private if needed

        JobLog log = new JobLog();
        log.setStatus("SUCCESS");
        log.setMessage("Completed successfully");
        log.setStartTime(LocalDateTime.now());

        when(mockRepo.findTopByJobNameOrderByStartTimeDesc("Job1")).thenReturn(log);
        when(mockRepo.findTopByJobNameOrderByStartTimeDesc("dependentJob1")).thenReturn(null); // no log, should show UNKNOWN

        // Inject mock repository
        var field = JobMonitorService.class.getDeclaredFields()[0]; // jobLogRepository
        field.setAccessible(true);
        try {
            field.set(service, mockRepo);
        } catch (Exception e) {
            fail("Failed to inject mock repository");
        }

        // Act
        List<JobDependencyDTO> graph = service.getJobGraphElements("Job1");

        // Assert
        assertEquals(3, graph.size()); // 2 nodes + 1 edge
        assertTrue(graph.stream().anyMatch(d -> d.getData().getId().equals("Job1")));
        assertTrue(graph.stream().anyMatch(d -> d.getData().getId().equals("dependentJob1")));
        assertTrue(graph.stream().anyMatch(d -> d.getData().getSource() != null)); // edge

        JobDependencyDTO.Data job1Node = graph.stream()
                .map(JobDependencyDTO::getData)
                .filter(d -> "Job1".equals(d.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(job1Node);
        assertEquals("SUCCESS", job1Node.getStatus());
        assertEquals("Completed successfully", job1Node.getMessage());

        JobDependencyDTO.Data depNode = graph.stream()
                .map(JobDependencyDTO::getData)
                .filter(d -> "dependentJob1".equals(d.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(depNode);
        assertEquals("UNKNOWN", depNode.getStatus());
        assertEquals("No recent message", depNode.getMessage());
    }

    @Test
    void testGetJobGraphElements_shouldIncludeVirtualNodes() {
        JobLogRepository mockRepo = mock(JobLogRepository.class);
        ObjectProvider<Scheduler> schedulerProvider = mock(ObjectProvider.class);
        JobMonitorService service = new JobMonitorService(schedulerProvider);

        // Inject mock repo
        var field = JobMonitorService.class.getDeclaredFields()[0];
        field.setAccessible(true);
        try {
            field.set(service, mockRepo);
        } catch (Exception e) {
            fail("Mock injection failed");
        }

        // Call the method with a virtual node
        List<JobDependencyDTO> result = service.getJobGraphElements("ProcessingJob");

        assertTrue(result.stream().anyMatch(d -> "InputFile".equals(d.getData().getId())));
        assertTrue(result.stream().anyMatch(d -> "ResultData".equals(d.getData().getId())));

        JobDependencyDTO.Data inputFileNode = result.stream()
                .map(JobDependencyDTO::getData)
                .filter(d -> "InputFile".equals(d.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(inputFileNode);
        assertEquals("SUCCESS", inputFileNode.getStatus());
        assertEquals("Supplied input to processing jobs", inputFileNode.getMessage());
    }

}