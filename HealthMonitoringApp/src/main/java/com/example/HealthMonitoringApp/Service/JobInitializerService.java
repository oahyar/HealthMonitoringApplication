package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component  // Marks this class as a Spring-managed component so it gets instantiated at startup
public class JobInitializerService {

    @Autowired  // Injects the JobLogRepository bean to interact with the job_logs table
    private JobLogRepository jobLogRepository;

    // This method runs automatically once the application context is initialized
    @PostConstruct
    public void insertWaitingJobLogIfMissing() {
        String jobName = "WaitingJob";

        // Check if a job log with the name "WaitingJob" already exists in the database
        boolean exists = jobLogRepository.existsByJobName(jobName);

        // If it doesn't exist, insert a new entry with status "WAITING"
        if (!exists) {
            JobLog waitingLog = new JobLog();
            waitingLog.setJobName(jobName);  // Set job name to "WaitingJob"
            waitingLog.setStatus("WAITING"); // Set status to WAITING
            waitingLog.setStartTime(LocalDateTime.now()); // Set the start time to current timestamp
            waitingLog.setMessage("Scheduled but not yet executed"); // Optional user-friendly message

            // Save the new job log entry to the database
            jobLogRepository.save(waitingLog);

            // Console log for debug visibility
            System.out.println("🕒 Inserted WAITING log for WaitingJob.");
        }
    }
}
