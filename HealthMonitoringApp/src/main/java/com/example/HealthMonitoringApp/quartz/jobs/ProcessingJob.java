package com.example.HealthMonitoringApp.quartz.jobs;

import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
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
    private static final Logger logger = LoggerFactory.getLogger(ProcessingJob.class);
    @Autowired
    private JobLogRepository jobLogRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String input = dataMap.getString("input");

        LocalDateTime startTime = LocalDateTime.now();
        String output;
        String status;
        String message;
        try {
            // Simulate processing
            output = input.toUpperCase();
            status = "SUCCESS";
            message = "Input: " + input + " | Output: " + output;
        } catch (Exception e) {
            output = "";
            status = "FAILED";
            message = "Error processing input: " + input;
            logger.error("ProcessingJob failed for input {}: {}", input, e.getMessage(), e);
        }

        LocalDateTime endTime = LocalDateTime.now();

        JobLog log = new JobLog();
        log.setJobName("processingJob");
        log.setStartTime(startTime);
        log.setEndTime(endTime);
        log.setStatus(status);
        log.setMessage(message);

        jobLogRepository.save(log);
        logger.debug("JobLog saved: {}", log);
    }
}