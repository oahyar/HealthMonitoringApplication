package com.example.HealthMonitoringApp.Repository;

import com.example.HealthMonitoringApp.Entity.JobLog;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobLogRepository extends JpaRepository<JobLog, Long> {
    List<JobLog> findByJobName(String jobName);
    JobLog findTopByJobNameOrderByStartTimeDesc(String jobName); // Find latest run for each jobname
    List<JobLog> findByJobNameOrderByStartTimeDesc(String jobName); // Find history of job run for each jobname

    @Transactional
    @Modifying
    @Query("DELETE FROM JobLog j WHERE j.startTime < :cutoff")
    int deleteByStartTimeBefore(@Param("cutoff") LocalDateTime cutoff);
}
