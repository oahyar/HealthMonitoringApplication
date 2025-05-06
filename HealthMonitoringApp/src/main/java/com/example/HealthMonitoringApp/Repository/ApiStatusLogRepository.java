package com.example.HealthMonitoringApp.Repository;

import com.example.HealthMonitoringApp.Entity.ApiStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApiStatusLogRepository extends JpaRepository<ApiStatusLog, Long> {
    List<ApiStatusLog> findTop10ByOrderByTimestampDesc();
}

