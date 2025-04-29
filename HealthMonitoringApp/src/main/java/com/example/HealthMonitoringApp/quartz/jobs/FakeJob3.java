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

    // Logger for recording execution steps and outcomes
    private static final Logger logger = LoggerFactory.getLogger(FakeJob3.class);

    // Inject the Quartz Scheduler to register and fire jobs programmatically
    @Autowired
    Scheduler scheduler;

    // Repository for persisting job status entries
    @Autowired
    JobLogRepository jobLogRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Name of the dependent job that this job will trigger
        String dependentJobName = "dependentJob3";

        // Capture the current timestamp
        LocalDateTime now = LocalDateTime.now();
        logger.info("Job3 executing at {}", now);

        try {
            // Step 1: Record a 'WAITING' state for the dependent job,
            // scheduled to run 10 minutes from now
            JobLog waitingLog = new JobLog();
            waitingLog.setJobName(dependentJobName);
            waitingLog.setStartTime(now.plusMinutes(10));
            waitingLog.setStatus("WAITING");
            waitingLog.setMessage("Job3 scheduled to run in 10 minutes");
            jobLogRepository.save(waitingLog);

            // Step 2: Ensure the dependent job is registered with the scheduler
            JobKey jobKey = new JobKey(dependentJobName);
            if (!scheduler.checkExists(jobKey)) {
                JobDetail dependentJob = JobBuilder.newJob(DependentJob3.class)
                        .withIdentity(jobKey)
                        .storeDurably()  // keep this job detail even if no trigger exists
                        .build();
                scheduler.addJob(dependentJob, false);
                logger.info("DependentJob3 registered.");
            }

            // Step 3: Create and schedule a one-off trigger to execute the dependent job immediately
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobKey)
                    .withIdentity("trigger_" + UUID.randomUUID())  // unique trigger name to avoid collisions
                    .startNow()
                    .build();
            scheduler.scheduleJob(trigger);
            logger.info("DependentJob3 triggered (will fail).");

        } catch (SchedulerException e) {
            // On scheduler errors, log the exception and signal Quartz of job failure
            logger.error("Failed to schedule DependentJob3", e);
            throw new JobExecutionException(e);
        }
    }
}
