package com.example.HealthMonitoringApp.quartz.jobs;

import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import com.example.HealthMonitoringApp.Service.JobMonitorService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProcessingJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(JobLogCleanupJob.class);

    @Autowired
    private JobMonitorService jobMonitorService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("ProcessingJob running");
    }
}
