package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import com.example.HealthMonitoringApp.dto.JobDependencyDTO;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class JobMonitorService {

    @Autowired
    private JobLogRepository jobLogRepository;

    private final ObjectProvider<Scheduler> schedulerProvider;

    public JobMonitorService(ObjectProvider<Scheduler> schedulerProvider) {
        this.schedulerProvider = schedulerProvider;
    }

    public String getJobStatus(String jobName, String group) throws SchedulerException {
        Scheduler scheduler = schedulerProvider.getIfAvailable();

        if (scheduler == null) {
            return "SCHEDULER_UNAVAILABLE";
        }

        JobKey jobKey = new JobKey(jobName, group);

        if (!scheduler.checkExists(jobKey)) {
            return "NOT_FOUND";
        }

        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        if (triggers.isEmpty()) {
            return "NO_TRIGGER";
        }

        Trigger.TriggerState triggerState = scheduler.getTriggerState(triggers.get(0).getKey());
        return triggerState.name();
    }

    // Clean up job db every 14 days
    public int cleanOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(14);
        return jobLogRepository.deleteByStartTimeBefore(cutoff);
    }

    //Dependency Graph
    public List<JobDependencyDTO> getJobGraphElements(String rootJobName) {
        List<JobDependencyDTO> elements = new ArrayList<>();

        // Define job names and edges manually (could be refactored to be dynamic later)
        Map<String, List<String>> dependencyMap = Map.of(
                "Job1", List.of("dependentJob1"),
                "Job2", List.of("dependentJob2"),
                "Job3", List.of("dependentJob3"),
                "ProcessingJob", List.of("InputFile"),
                "ResultData", List.of("ProcessingJob"),
                "AlternatingJob", List.of("InputFile"),
                "WaitingJob", List.of("InputFile")
        );

        Set<String> involvedJobs = new HashSet<>();
        involvedJobs.add(rootJobName);

        // Include parent dependencies
        for (Map.Entry<String, List<String>> entry : dependencyMap.entrySet()) {
            if (entry.getKey().equals(rootJobName) || entry.getValue().contains(rootJobName)) {
                involvedJobs.add(entry.getKey());
                involvedJobs.addAll(entry.getValue());
            }
        }

        // Add nodes
        for (String jobName : involvedJobs) {
            JobLog latest = jobLogRepository.findTopByJobNameOrderByStartTimeDesc(jobName);
            String status = (latest != null) ? latest.getStatus() : "UNKNOWN";

            JobDependencyDTO.Data nodeData = new JobDependencyDTO.Data();
            nodeData.setId(jobName);
            nodeData.setLabel(jobName);
            nodeData.setStatus(status);
            nodeData.setMessage(latest != null ? latest.getMessage() : "No recent message");

            JobDependencyDTO node = new JobDependencyDTO();
            node.setData(nodeData);
            elements.add(node);
        }

        // Add matching edges
        for (Map.Entry<String, List<String>> entry : dependencyMap.entrySet()) {
            String target = entry.getKey();
            for (String source : entry.getValue()) {
                if (involvedJobs.contains(source) && involvedJobs.contains(target)) {
                    elements.add(makeEdge(source, target));
                }
            }
        }

        return elements;
    }

    private JobDependencyDTO makeEdge(String dependent, String job) {
        JobDependencyDTO.Data edgeData = new JobDependencyDTO.Data();
        edgeData.setSource(dependent);
        edgeData.setTarget(job);

        JobDependencyDTO edge = new JobDependencyDTO();
        edge.setData(edgeData);

        return edge;
    }
}

