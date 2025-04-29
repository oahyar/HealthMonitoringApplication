package com.example.HealthMonitoringApp.Repository;

import com.example.HealthMonitoringApp.Entity.JobLog;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobLogRepository extends JpaRepository<JobLog, Long> {

    // Finds all logs for a specific job name
    List<JobLog> findByJobName(String jobName);

    // Retrieves the most recent log entry for a job (based on start time)
    JobLog findTopByJobNameOrderByStartTimeDesc(String jobName);

    // Retrieves all logs for a job, sorted by descending start time (latest first)
    List<JobLog> findByJobNameOrderByStartTimeDesc(String jobName);

    // Deletes all logs older than a specific timestamp (used for cleanup)
    @Transactional
    @Modifying
    @Query("DELETE FROM JobLog j WHERE j.startTime < :cutoff")
    int deleteByStartTimeBefore(@Param("cutoff") LocalDateTime cutoff);

    // Returns a list of all unique job names that have logs
    @Query("SELECT DISTINCT j.jobName FROM JobLog j")
    List<String> findDistinctJobNames();

    // Checks whether any log exists for a specific job
    boolean existsByJobName(String jobName);

    // Retrieves both the main job logs and its retry attempts (e.g., myJob_retry_1)
    // Filters out WAITING statuses and includes only jobs that started before current time
    @Query("""
      SELECT j
        FROM JobLog j
       WHERE (j.jobName = :jobName
           OR j.jobName LIKE CONCAT(:jobName, '_retry_%'))
         AND j.startTime <= CURRENT_TIMESTAMP
         AND j.status <> 'WAITING'
    """)
    List<JobLog> findExecutedAndRetries(
            @Param("jobName") String jobName,
            Sort sort
    );
}
