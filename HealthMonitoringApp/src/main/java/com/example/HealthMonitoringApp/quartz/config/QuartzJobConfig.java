package com.example.HealthMonitoringApp.quartz.config;

import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import com.example.HealthMonitoringApp.quartz.jobs.*;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Configuration
public class QuartzJobConfig {

    // Spring Data repository for persisting job logs
    @Autowired
    private JobLogRepository jobLogRepository;

    // Define a durable JobDetail for FakeJob
    @Bean
    public JobDetail fakeJobDetail() {
        return JobBuilder.newJob(FakeJob.class)
                .withIdentity("Job1")      // give the job a unique name
                .storeDurably()            // keep the job even if no triggers point to it
                .build();
    }

    // Define a trigger that fires FakeJob every 15 minutes
    @Bean
    public Trigger fakeJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(fakeJobDetail())    // link to the JobDetail above
                .withIdentity("fakeTrigger1")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(15)  // interval between executions
                        .repeatForever())           // repeat indefinitely
                .build();
    }

    // Second fake job, similar pattern but with different interval
    @Bean
    public JobDetail fakeJob2Detail() {
        return JobBuilder.newJob(FakeJob2.class)
                .withIdentity("Job2")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger fakeJob2Trigger() {
        return TriggerBuilder.newTrigger()
                .forJob(fakeJob2Detail())
                .withIdentity("fakeTrigger2")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(5)   // every 5 minutes
                        .repeatForever())
                .build();
    }

    // Third fake job with retry function if job fail
    @Bean
    public JobDetail fakeJob3Detail() {
        return JobBuilder.newJob(FakeJob3.class)
                .withIdentity("Job3")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger fakeJob3Trigger() {
        return TriggerBuilder.newTrigger()
                .forJob(fakeJob3Detail())
                .withIdentity("fakeTrigger3")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(10)
                        .repeatForever())
                .build();
    }

    // Failure Job
    @Bean
    public JobDetail fakeJob4Detail() {
        return JobBuilder.newJob(FakeJob4.class)
                .withIdentity("Job4")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger fakeJob4Trigger() {
        return TriggerBuilder.newTrigger()
                .forJob(fakeJob4Detail())
                .withIdentity("fakeTrigger4")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(2)
                        .repeatForever())
                .build();
    }

    // ProcessingJob with input and output
    @Bean
    public JobDetail processingJobDetail() {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("input", "hello world"); // custom input data for the job

        return JobBuilder.newJob(ProcessingJob.class)
                .withIdentity("ProcessingJob")
                .usingJobData(dataMap)      // attach the data map
                .storeDurably()
                .build();
    }

    // ProcessingJob with input and output
    @Bean
    public Trigger processingJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(processingJobDetail())
                .withIdentity("processingTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(15)
                        .repeatForever())
                .build();
    }

    // AlternateJob for success and failure
    @Bean
    public JobDetail alternatingJobDetail() {
        return JobBuilder.newJob(AlternateJob.class)
                .withIdentity("AlternateJob")
                .storeDurably()
                .build();
    }

    // AlternateJob for success and failure
    @Bean
    public Trigger alternatingJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(alternatingJobDetail())
                .withIdentity("AlternateTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(30)
                        .repeatForever())
                .build();
    }

    // Waiting Status Job
    @Bean
    public JobDetail waitingJobDetail() {
        return JobBuilder.newJob(WaitingJob.class)
                .withIdentity("WaitingJob")
                .storeDurably()
                .build();
    }

    // Waiting Status Job
    @Bean
    public Trigger waitingJobTrigger() {
        // If no log exists, create an initial 'waiting' record
        if (!jobLogRepository.existsByJobName("WaitingJob")) {
            JobLog log = new JobLog();
            log.setJobName("WaitingJob");
            log.setStatus("WAITING");
            log.setStartTime(LocalDateTime.now());
            log.setMessage("Scheduled to run in 1 year.");
            jobLogRepository.save(log);
        }

        // Schedule the actual job start date one year ahead
        return TriggerBuilder.newTrigger()
                .forJob(waitingJobDetail())
                .withIdentity("WaitingTrigger")
                .startAt(Date.from(
                        LocalDateTime.now()
                                .plusYears(1)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()))
                .build();
    }

    // Job for cleaning up old JobLog entries
    @Bean
    public JobDetail cleanupJobDetail() {
        return JobBuilder.newJob(JobLogCleanupJob.class)
                .withIdentity("jobLogCleanup")
                .storeDurably()
                .build();
    }

    // Trigger to run the cleanup job every 14 days
    @Bean
    public Trigger cleanupJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(cleanupJobDetail())
                .withIdentity("jobLogCleanupTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInHours(24 * 14) // calculate 14 days in hours
                        .repeatForever())
                .build();
    }

}
