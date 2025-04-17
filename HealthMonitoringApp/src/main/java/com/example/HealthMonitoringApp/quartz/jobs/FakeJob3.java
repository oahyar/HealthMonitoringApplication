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
public class FakeJob3 implements Job {

    private static final Logger logger = LoggerFactory.getLogger(FakeJob3.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private JobLogRepository jobLogRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String dependentJobName = "dependentJob3";
        LocalDateTime now = LocalDateTime.now();

        logger.info("Job3 executing at {}", now);

        try {
            // Log WAITING for next run
            JobLog log = new JobLog();
            log.setJobName(dependentJobName);
            log.setStartTime(now.plusMinutes(10));
            log.setStatus("WAITING");
            log.setMessage("Job3 scheduled to run in 10 minutes");
            jobLogRepository.save(log);

            // Register DependentJob3 if not exists
            JobKey jobKey = new JobKey(dependentJobName);
            if (!scheduler.checkExists(jobKey)) {
                JobDetail dependentJob = JobBuilder.newJob(DependentJob3.class)
                        .withIdentity(jobKey)
                        .storeDurably()
                        .build();
                scheduler.addJob(dependentJob, false);
            }

            // Trigger immediately
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobKey)
                    .withIdentity("trigger_" + UUID.randomUUID())
                    .startNow()
                    .build();

            scheduler.scheduleJob(trigger);
            logger.info("DependentJob3 triggered (will fail).");

        } catch (SchedulerException e) {
            logger.error("Failed to schedule DependentJob3", e);
            throw new JobExecutionException(e);
        }
    }
}