package com.example.HealthMonitoringApp.Repository;

import com.example.HealthMonitoringApp.Entity.ApiStatusLog;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ApiStatusLogRepository extends JpaRepository<ApiStatusLog, Long> {
    List<ApiStatusLog> findTop10ByOrderByTimestampDesc();
    List<ApiStatusLog> findTop20ByOrderByTimestampDesc();
    List<ApiStatusLog> findTop20ByApiNameOrderByTimestampDesc(String apiName);

    @Query("""
    SELECT a 
      FROM ApiStatusLog a 
     WHERE a.timestamp = (
       SELECT MAX(b.timestamp)
         FROM ApiStatusLog b 
        WHERE b.apiName = a.apiName
     )
  """)
    List<ApiStatusLog> findLatestPerApi();

    @Transactional
    @Modifying
    @Query("DELETE FROM ApiStatusLog a WHERE a.timestamp < :cutoff")
    int deleteByTimestampBefore(@Param("cutoff") Instant cutoff);
}

