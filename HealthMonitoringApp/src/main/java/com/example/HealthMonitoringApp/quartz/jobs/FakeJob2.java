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

    // Logger for recording execution steps and potential errors
    private static final Logger logger = LoggerFactory.getLogger(FakeJob2.class);

    // Inject Quartz Scheduler to programmatically register and trigger jobs
    @Autowired
    Scheduler scheduler;

    // Repository for saving job status entries
    @Autowired
    JobLogRepository jobLogRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Define the name of the dependent job
        String dependentJobName = "dependentJob2";

        // Capture the current timestamp
        LocalDateTime now = LocalDateTime.now();
        logger.info("Job2 executing at {}", now);

        try {
            // Step 1: Record a 'WAITING' entry for the dependent job,
            // scheduled to run 5 minutes from now
            JobLog waitingLog = new JobLog();
            waitingLog.setJobName(dependentJobName);
            waitingLog.setStartTime(now.plusMinutes(5));
            waitingLog.setStatus("WAITING");
            waitingLog.setMessage("Job2 scheduled to run in 5 minutes");
            jobLogRepository.save(waitingLog);

            // Step 2: Ensure the dependent job is registered with the scheduler
            JobKey jobKey = new JobKey(dependentJobName);
            if (!scheduler.checkExists(jobKey)) {
                JobDetail dependentJob = JobBuilder.newJob(DependentJob2.class)
                        .withIdentity(jobKey)
                        .storeDurably()  // Keep this job definition even if no trigger exists
                        .build();
                scheduler.addJob(dependentJob, false);
                logger.info("DependentJob2 registered.");
            }

            // Step 3: Create and schedule a one-off trigger to fire the dependent job immediately
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobKey)
                    .withIdentity("trigger_" + UUID.randomUUID())  // Unique trigger name to avoid collisions
                    .startNow()
                    .build();
            scheduler.scheduleJob(trigger);
            logger.info("DependentJob2 triggered.");

        } catch (SchedulerException e) {
            // Log any scheduler errors and wrap them in a JobExecutionException
            logger.error("Failed to run DependentJob2", e);
            throw new JobExecutionException(e);
        }
    }
}
