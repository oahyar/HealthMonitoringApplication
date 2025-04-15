package com.example.HealthMonitoringApp.quartz.jobs;

import com.example.HealthMonitoringApp.Service.JobMonitorService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobLogCleanupJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(JobLogCleanupJob.class);

    @Autowired
    private JobMonitorService jobMonitorService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int deletedCount = jobMonitorService.cleanOldLogs();
        logger.info("Cleaned {} old job logs.", deletedCount);
    }
}