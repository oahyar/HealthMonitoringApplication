package com.example.HealthMonitoringApp.listener;

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

    @Autowired
    private JobLogRepository jobLogRepository;

    private final Map<String, LocalDateTime> startTimes = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "jobLogger";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        String jobName = context.getJobDetail().getKey().getName();
        startTimes.put(jobName, LocalDateTime.now());
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        String jobName = context.getJobDetail().getKey().getName();
        LocalDateTime startTime = startTimes.getOrDefault(jobName, LocalDateTime.now());

        JobLog log = new JobLog();
        log.setJobName(jobName);
        log.setStartTime(startTime);
        log.setEndTime(LocalDateTime.now());

        if (jobException == null) {
            log.setStatus("SUCCESS");
            log.setMessage("Job completed successfully.");
        } else {
            log.setStatus("FAILED");
            log.setMessage(jobException.getMessage()); // 👈 includes retry message
        }

        jobLogRepository.save(log);
        startTimes.remove(jobName);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // Optional: log vetoed jobs
    }
}
