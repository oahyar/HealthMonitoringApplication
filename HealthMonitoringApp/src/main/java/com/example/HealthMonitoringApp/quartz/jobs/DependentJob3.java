package com.example.HealthMonitoringApp.quartz.jobs;

import org.quartz.*;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Component
public class DependentJob3 implements Job {

    // Logger for recording execution details and issues
    private static final Logger logger = LoggerFactory.getLogger(DependentJob3.class);

    // Maximum number of retry attempts before giving up
    private static final int MAX_RETRIES = 3;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Merge data from JobDetail and any Trigger into a single map
        JobDataMap dataMap = context.getMergedJobDataMap();
        // Identify this job by its Quartz key name
        String jobName = context.getJobDetail().getKey().getName();

        // Retrieve current retry count, default to 0 if not present
        int retryCount = dataMap.containsKey("retryCount")
                ? dataMap.getInt("retryCount")
                : 0;

        // Log which attempt this is
        logger.info("Attempt {} for {}", retryCount + 1, jobName);

        // Decide randomly whether this execution should fail
        boolean shouldFail = shouldFail();

        if (shouldFail) {
            String message = "Simulated failure on attempt " + (retryCount + 1);

            if (retryCount < MAX_RETRIES - 1) {
                // Prepare to retry: increment retryCount in new JobDataMap (combine trigger and job detail)
                JobDataMap retryData = new JobDataMap();
                retryData.put("retryCount", retryCount + 1);

                // Derive a unique retry job name based on the original
                String baseJobName = jobName.split("_retry_")[0];
                String retryJobName = baseJobName + "_retry_" + (retryCount + 1);

                // Build a new JobDetail for the retry attempt
                JobDetail retryJob = JobBuilder.newJob(DependentJob3.class)
                        .withIdentity(retryJobName)
                        .usingJobData("retryCount", retryCount + 1)
                        .storeDurably()
                        .build();

                // Schedule the retry to run after a 5-second delay
                Trigger retryTrigger = TriggerBuilder.newTrigger()
                        .forJob(retryJob)
                        .withIdentity("trigger_retry_" + UUID.randomUUID())
                        .startAt(Date.from(Instant.now().plusSeconds(5)))
                        .build();

                try {
                    Scheduler scheduler = context.getScheduler();
                    // Add or replace the retry job in Quartz
                    scheduler.addJob(retryJob, true);
                    // Schedule the new trigger for the retry
                    scheduler.scheduleJob(retryTrigger);
                    logger.warn("{} — retrying... ({} left)", message, MAX_RETRIES - retryCount - 1);
                } catch (SchedulerException e) {
                    // If scheduling fails, wrap and rethrow as JobExecutionException
                    throw new JobExecutionException("Retry scheduling failed: " + e.getMessage(), e);
                }

                // Throw exception so that this execution is marked as FAILED
                throw new JobExecutionException(message);

            } else {
                // No retries left: log final failure and throw exception
                logger.error("{} — max retries reached", message);
                throw new JobExecutionException(message);
            }
        }

        // If no failure was simulated, log successful completion
        logger.info("DependentJob3 completed successfully.");
    }

    //Randomly decides whether this run should fail.
    //@return true ~50% of the time, false otherwise
    protected boolean shouldFail() {
        return Math.random() < 0.5;
    }
}
