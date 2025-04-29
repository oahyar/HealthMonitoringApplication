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

    // Keeps track of whether the last run was successful
    // Used to alternate between SUCCESS and FAILURE each time the job is triggered
    private static boolean lastRunSuccess = false;

    @Autowired
    private JobLogRepository jobLogRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Record the start time of the job execution
        LocalDateTime start = LocalDateTime.now();
        String jobName = "AlternateJob";

        String status;
        String message;

        // Alternate job status: if the last run succeeded, this one fails, and vice versa
        if (lastRunSuccess) {
            status = "FAILED";
            message = "Simulated failure for alternating job.";
        } else {
            status = "SUCCESS";
            message = "Simulated success for alternating job.";
        }

        // Flip the flag for the next run
        lastRunSuccess = !lastRunSuccess;

        // If status is FAILED, simulate a failure by throwing an exception
        if (status.equals("FAILED")) {
            throw new JobExecutionException("Simulated failure.");
        }

    }
}
