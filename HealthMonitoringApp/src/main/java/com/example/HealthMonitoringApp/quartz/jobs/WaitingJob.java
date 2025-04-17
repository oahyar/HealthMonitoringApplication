package com.example.HealthMonitoringApp.quartz.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
public class WaitingJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
        System.out.println("WaitingJob is running... but should not happen immediately.");
    }
}