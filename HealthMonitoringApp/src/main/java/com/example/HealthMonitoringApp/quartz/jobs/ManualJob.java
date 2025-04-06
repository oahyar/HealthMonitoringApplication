package com.example.HealthMonitoringApp.quartz.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ManualJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
        System.out.println("ManualJob manually triggered at: " + LocalDateTime.now());
    }
}