package com.example.HealthMonitoringApp.quartz.jobs;

import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;


@Component
public class FakeJob4 implements Job {

    private static final Logger logger = LoggerFactory.getLogger(FakeJob4.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.warn("FakeJob4 encountered a setup failure, aborting...");

        throw new JobExecutionException("Simulated failure in FakeJob4 (prevents triggering dependent job)");
    }
}