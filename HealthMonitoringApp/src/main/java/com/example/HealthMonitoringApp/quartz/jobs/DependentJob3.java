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

    private static final Logger logger = LoggerFactory.getLogger(DependentJob3.class);

    private static final int MAX_RETRIES = 3;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String jobName = context.getJobDetail().getKey().getName();

        int retryCount = dataMap.containsKey("retryCount")
                ? dataMap.getInt("retryCount")
                : 0;

        logger.info("Attempt {} for {}", retryCount + 1, jobName);

        // Simulate failure 50% of the time
        boolean shouldFail = Math.random() < 0.5;

        if (shouldFail) {
            String message = "Simulated failure on attempt " + (retryCount + 1);

            if (retryCount < MAX_RETRIES - 1) {
                // Retry logic: reschedule the job immediately with retryCount++
                JobDataMap retryData = new JobDataMap();
                retryData.put("retryCount", retryCount + 1);
                String baseJobName = jobName.split("_retry_")[0]; // original job name
                String retryJobName = baseJobName + "_retry_" + (retryCount + 1);

                JobDetail retryJob = JobBuilder.newJob(DependentJob3.class)
                        .withIdentity(retryJobName)
                        .usingJobData("retryCount", retryCount + 1)
                        .storeDurably()
                        .build();

                Trigger retryTrigger = TriggerBuilder.newTrigger()
                        .forJob(retryJob)
                        .withIdentity("trigger_retry_" + UUID.randomUUID())
                        .startAt(Date.from(Instant.now().plusSeconds(5))) // ⏱ 5-second delay
                        .build();

                try {
                    Scheduler scheduler = context.getScheduler();
                    scheduler.addJob(retryJob, true); // Replace if same ID
                    scheduler.scheduleJob(retryTrigger);
                    logger.warn("{} — retrying... ({} left)", message, MAX_RETRIES - retryCount - 1);
                } catch (SchedulerException e) {
                    throw new JobExecutionException("Retry scheduling failed: " + e.getMessage(), e);
                }

                // Throw exception so current attempt logs as failed
                throw new JobExecutionException(message);

            } else {
                logger.error("{} — max retries reached", message);
                throw new JobExecutionException(message); // Final failure
            }
        }

        // If it succeeded
        logger.info("DependentJob3 completed successfully.");
    }
}

