package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.quartz.listener.JobLoggerListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers the JobLoggerListener with the Quartz Scheduler after Spring Boot initializes it.
 * This allows Quartz to notify the listener when any job is executed.
 */
@Component
public class SchedulerListenerRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerListenerRegistrar.class);

    @Autowired
    Scheduler scheduler;

    @Autowired
    JobLoggerListener jobLoggerListener;

    /**
     * This method runs after all dependencies are injected.
     * It manually registers the job listener with Quartz so it can start listening to job events.
     */
    @PostConstruct
    public void registerListener() {
        try {
            scheduler.getListenerManager().addJobListener(jobLoggerListener);
            logger.info("JobLoggerListener registered with scheduler.");
        } catch (SchedulerException e) {
            logger.error("Failed to register JobLoggerListener", e);
            throw new RuntimeException("Failed to register JobLoggerListener", e);
        }
    }
}
