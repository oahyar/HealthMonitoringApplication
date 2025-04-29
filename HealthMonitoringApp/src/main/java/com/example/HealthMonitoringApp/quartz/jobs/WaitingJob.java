package com.example.HealthMonitoringApp.quartz.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WaitingJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(ProcessingJob.class);
    @Override
    public void execute(JobExecutionContext context) {
        logger.info("WaitingJob is running... but should not happen immediately.");
    }
}