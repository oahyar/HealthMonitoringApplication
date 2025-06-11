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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class JobMonitorService {

    @Autowired
    JobLogRepository jobLogRepository;

    // Injects the Scheduler (Quartz) instance using ObjectProvider
    private final ObjectProvider<Scheduler> schedulerProvider;

    public JobMonitorService(ObjectProvider<Scheduler> schedulerProvider) {
        this.schedulerProvider = schedulerProvider;
    }

    // Returns the current status of a Quartz job
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

    // Deletes job logs older than 14 days from the database
    public int cleanOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(14);
        return jobLogRepository.deleteByStartTimeBefore(cutoff);
    }

    // Builds a dependency graph for jobs, returning both job nodes and edge relationships
    public List<JobDependencyDTO> getJobGraphElements(String rootJobName) {
        List<JobDependencyDTO> elements = new ArrayList<>();

        // Hardcoded dependency relationships between jobs
        Map<String, List<String>> dependencyMap = Map.of(
                "Job1", List.of("dependentJob1"),
                "Job2", List.of("dependentJob2"),
                "Job3", List.of("dependentJob3"),
                "ProcessingJob", List.of("InputFile"),
                "ResultData", List.of("ProcessingJob"),
                "AlternatingJob", List.of("InputFile"),
                "WaitingJob", List.of("InputFile")
        );

        // Identify relevant jobs to include in the graph
        Set<String> involvedJobs = new HashSet<>();
        involvedJobs.add(rootJobName);

        for (Map.Entry<String, List<String>> entry : dependencyMap.entrySet()) {
            if (entry.getKey().equals(rootJobName) || entry.getValue().contains(rootJobName)) {
                involvedJobs.add(entry.getKey());
                involvedJobs.addAll(entry.getValue());
            }
        }

        // Create job nodes with their latest statuses and messages
        for (String jobName : involvedJobs) {
            JobDependencyDTO.Data nodeData = new JobDependencyDTO.Data();
            nodeData.setId(jobName);
            nodeData.setLabel(jobName);

            if (jobName.equals("InputFile")) {
                nodeData.setStatus("SUCCESS");
                nodeData.setMessage("Supplied input to processing jobs");
            } else if (jobName.equals("ResultData")) {
                nodeData.setStatus("SUCCESS");
                nodeData.setMessage("Final result after processing");
            } else {
                JobLog latest = jobLogRepository.findTopByJobNameOrderByStartTimeDesc(jobName);
                nodeData.setStatus(latest != null ? latest.getStatus() : "UNKNOWN");
                nodeData.setMessage(latest != null ? latest.getMessage() : "No recent message");
            }

            JobDependencyDTO node = new JobDependencyDTO();
            node.setData(nodeData);
            elements.add(node);
        }

        // Create edges (relationships) between nodes
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

    // Helper method to create a dependency edge
    private JobDependencyDTO makeEdge(String dependent, String job) {
        JobDependencyDTO.Data edgeData = new JobDependencyDTO.Data();
        edgeData.setSource(dependent);
        edgeData.setTarget(job);

        JobDependencyDTO edge = new JobDependencyDTO();
        edge.setData(edgeData);

        return edge;
    }

    // Combines job history for the main job and its retries or dependent jobs
    public List<JobLog> getHistoryIncludingDependent(String jobName) {
        // Main job’s history
        List<JobLog> main = jobLogRepository.findByJobNameOrderByStartTimeDesc(jobName);

        // Dependent job’s history if any exist
        String depName = "dependent" + jobName;
        List<JobLog> dependent = Collections.emptyList();

        if (!jobLogRepository.findByJobNameOrderByStartTimeDesc(depName).isEmpty()) {
            dependent = jobLogRepository.findExecutedAndRetries(
                    depName,
                    Sort.by(Sort.Direction.DESC, "startTime")
            );
        }

        // Merge both and sort by start time (descending)
        return Stream
                .concat(main.stream(), dependent.stream())
                .sorted(Comparator.comparing(JobLog::getStartTime).reversed())
                .collect(Collectors.toList());
    }
}


