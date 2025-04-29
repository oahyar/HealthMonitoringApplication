package com.example.HealthMonitoringApp.quartz.listener;

import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobLoggerListener implements JobListener {

    // Injects the JobLogRepository so logs can be saved to the database
    @Autowired
    private JobLogRepository jobLogRepository;

    // Stores job start times (used to calculate duration later)
    private final Map<String, LocalDateTime> startTimes = new ConcurrentHashMap<>();

    // This name identifies the listener in the Quartz framework
    @Override
    public String getName() {
        return "jobLogger";
    }

    // Called BEFORE the job is executed — saves the start time
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        String jobName = context.getJobDetail().getKey().getName();
        startTimes.put(jobName, LocalDateTime.now());
    }

    // Called AFTER the job finishes — records job status and duration
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        String jobName = context.getJobDetail().getKey().getName();
        LocalDateTime startTime = startTimes.getOrDefault(jobName, LocalDateTime.now());

        JobLog log = new JobLog();
        log.setJobName(jobName);
        log.setStartTime(startTime); // previously recorded
        log.setEndTime(LocalDateTime.now()); // now is end time

        if (jobException == null) {
            log.setStatus("SUCCESS");
            log.setMessage("Job completed successfully.");
        } else {
            log.setStatus("FAILED");
            log.setMessage(jobException.getMessage()); // can include retry message
        }

        // Save job run details to DB
        jobLogRepository.save(log);

        // Clean up memory
        startTimes.remove(jobName);
    }

    // Optional: Called if a trigger vetoes job execution (e.g., blocked by logic or conditions)
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // Could log or handle vetoed executions here
    }
}
