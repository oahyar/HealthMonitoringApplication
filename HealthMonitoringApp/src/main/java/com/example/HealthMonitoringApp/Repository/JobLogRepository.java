package com.example.HealthMonitoringApp.Repository;

import com.example.HealthMonitoringApp.Entity.JobLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobLogRepository extends JpaRepository<JobLog, Long> {
    List<JobLog> findByJobName(String jobName);
    JobLog findTopByJobNameOrderByStartTimeDesc(String jobName); // Find latest run for each jobname
    List<JobLog> findByJobNameOrderByStartTimeDesc(String jobName); // Find history of job run for each jobname
}
