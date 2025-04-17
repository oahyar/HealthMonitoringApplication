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
public class FakeJob2 implements Job {

    private static final Logger logger = LoggerFactory.getLogger(FakeJob2.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private JobLogRepository jobLogRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String dependentJobName = "dependentJob2";
        LocalDateTime now = LocalDateTime.now();

        logger.info("Job2 executing at {}", now);

        try {
            // Log WAITING for the next round
            JobLog waitingLog = new JobLog();
            waitingLog.setJobName(dependentJobName);
            waitingLog.setStartTime(now.plusMinutes(5));
            waitingLog.setStatus("WAITING");
            waitingLog.setMessage("Job2 scheduled to run in 5 minutes");
            jobLogRepository.save(waitingLog);

            // Create DependentJob2 if not exists
            JobKey jobKey = new JobKey(dependentJobName);
            if (!scheduler.checkExists(jobKey)) {
                JobDetail dependentJob = JobBuilder.newJob(DependentJob2.class)
                        .withIdentity(jobKey)
                        .storeDurably()
                        .build();
                scheduler.addJob(dependentJob, false);
                logger.info("DependentJob2 registered.");
            }

            // Trigger it now
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobKey)
                    .withIdentity("trigger_" + UUID.randomUUID())
                    .startNow()
                    .build();

            scheduler.scheduleJob(trigger);
            logger.info("DependentJob2 triggered.");

        } catch (SchedulerException e) {
            logger.error("Failed to run DependentJob2", e);
            throw new JobExecutionException(e);
        }
    }
}