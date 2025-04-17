package com.example.HealthMonitoringApp.quartz.jobs;

import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AlternateJob implements Job {

    private static boolean lastRunSuccess = false;

    @Autowired
    private JobLogRepository jobLogRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime start = LocalDateTime.now();
        String jobName = "AlternateJob";

        String status;
        String message;

        // Alternate status
        if (lastRunSuccess) {
            status = "FAILED";
            message = "Simulated failure for alternating job.";
        } else {
            status = "SUCCESS";
            message = "Simulated success for alternating job.";
        }

        lastRunSuccess = !lastRunSuccess;

        JobLog log = new JobLog();
        log.setJobName(jobName);
        log.setStartTime(start);
        log.setEndTime(LocalDateTime.now());
        log.setStatus(status);
        log.setMessage(message);

        jobLogRepository.save(log);

        if (status.equals("FAILED")) {
            throw new JobExecutionException("Simulated failure.");
        }
    }
}
