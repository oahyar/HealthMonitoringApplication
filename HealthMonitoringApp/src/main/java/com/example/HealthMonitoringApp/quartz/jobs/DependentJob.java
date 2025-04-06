package com.example.HealthMonitoringApp.quartz.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Component
public class DependentJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(DependentJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        logger.info("DependentJob1 triggered after Job1 at: {} ", LocalDateTime.now());
    }
}
