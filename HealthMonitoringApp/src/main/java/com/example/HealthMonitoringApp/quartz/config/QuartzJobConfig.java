package com.example.HealthMonitoringApp.quartz.config;

import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import com.example.HealthMonitoringApp.quartz.jobs.FakeJob;
import com.example.HealthMonitoringApp.quartz.jobs.FakeJob2;
import com.example.HealthMonitoringApp.quartz.jobs.FakeJob3;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class QuartzJobConfig {


    @Autowired
    private JobLogRepository jobLogRepository;

    @Bean
    public JobDetail fakeJobDetail() {
        return JobBuilder.newJob(FakeJob.class)
                .withIdentity("fakeJob1")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger fakeJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(fakeJobDetail())
                .withIdentity("fakeTrigger1")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(15)  // every 15min
                        .repeatForever())
                .build();
    }

    @Bean
    public JobDetail fakeJob2Detail() {
        return JobBuilder.newJob(FakeJob2.class)
                .withIdentity("fakeJob2")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger fakeJob2Trigger() {
        return TriggerBuilder.newTrigger()
                .forJob(fakeJob2Detail())
                .withIdentity("fakeTrigger2")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(5)
                        .repeatForever())
                .build();
    }

    @Bean
    public JobDetail fakeJob3Detail() {
        return JobBuilder.newJob(FakeJob3.class)
                .withIdentity("fakeJob3")
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

}
