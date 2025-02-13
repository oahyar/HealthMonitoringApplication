package com.example.HealthMonitoringApp.Repository;

import com.example.HealthMonitoringApp.Entity.ServerMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerMetricRepository extends JpaRepository<ServerMetric, Long> {

    @Query(value = "SELECT * FROM diskspace.server_metrics", nativeQuery = true)
    List<ServerMetric> findAllMetrics();
}
