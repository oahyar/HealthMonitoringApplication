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

    private static final Logger logger = LoggerFactory.getLogger(FakeJob.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private JobLogRepository jobLogRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String dependentJobName = "dependentJob1";
        LocalDateTime now = LocalDateTime.now();

        logger.info("🟢 FakeJob1 executing at {}", now);

        try {
            // 📝 Step 1: Log the next WAITING state
            JobLog waitingLog = new JobLog();
            waitingLog.setJobName(dependentJobName);
            waitingLog.setStartTime(now.plusMinutes(15)); // Next scheduled time
            waitingLog.setStatus("WAITING");
            waitingLog.setMessage("Job1 scheduled to run in 15 minutes");
            jobLogRepository.save(waitingLog);

            // 🛠 Step 2: Ensure dependent job exists
            JobKey dependentJobKey = new JobKey(dependentJobName);
            if (!scheduler.checkExists(dependentJobKey)) {
                JobDetail dependentJob = JobBuilder.newJob(DependentJob.class)
                        .withIdentity(dependentJobKey)
                        .storeDurably()
                        .build();
                scheduler.addJob(dependentJob, false);
                logger.info("DependentJob1 registered.");
            }

            // 🚀 Step 3: Trigger the dependent job now
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(dependentJobKey)
                    .withIdentity("trigger_" + UUID.randomUUID())
                    .startNow()
                    .build();

            scheduler.scheduleJob(trigger);
            logger.info("DependentJob1 triggered.");

        } catch (SchedulerException e) {
            logger.error("Failed to run DependentJob1", e);
            throw new JobExecutionException(e);
        }
    }
}

