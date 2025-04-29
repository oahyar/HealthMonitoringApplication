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
public class FakeJob implements Job {

    // Logger for recording execution steps and any errors
    private static final Logger logger = LoggerFactory.getLogger(FakeJob.class);

    // Inject the Quartz Scheduler to register and trigger jobs programmatically
    @Autowired
    Scheduler scheduler;

    // Repository for persisting job status logs
    @Autowired
    JobLogRepository jobLogRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Name of the dependent job to be managed
        String dependentJobName = "dependentJob1";

        // Capture the current time for logging and scheduling
        LocalDateTime now = LocalDateTime.now();
        logger.info("Job1 executing at {}", now);

        try {
            // Step 1: Log a 'WAITING' state for the dependent job,
            // indicating when it will next be scheduled (15 minutes from now)
            JobLog waitingLog = new JobLog();
            waitingLog.setJobName(dependentJobName);
            waitingLog.setStartTime(now.plusMinutes(15));
            waitingLog.setStatus("WAITING");
            waitingLog.setMessage("Job1 scheduled to run in 15 minutes");
            jobLogRepository.save(waitingLog);

            // Step 2: Ensure the dependent job is registered in the scheduler
            JobKey dependentJobKey = new JobKey(dependentJobName);
            if (!scheduler.checkExists(dependentJobKey)) {
                JobDetail dependentJob = JobBuilder.newJob(DependentJob.class)
                        .withIdentity(dependentJobKey)
                        .storeDurably()  // keep this job definition even without triggers
                        .build();
                scheduler.addJob(dependentJob, false);
                logger.info("DependentJob1 registered.");
            }

            // Step 3: Create and fire a one-off trigger to run the dependent job immediately
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(dependentJobKey)
                    .withIdentity("trigger_" + UUID.randomUUID())  // unique trigger name
                    .startNow()
                    .build();
            scheduler.scheduleJob(trigger);
            logger.info("DependentJob1 triggered.");

        } catch (SchedulerException e) {
            // On any scheduler failure, log the error and signal Quartz of job failure
            logger.error("Failed to run DependentJob1", e);
            throw new JobExecutionException(e);
        }
    }
}
