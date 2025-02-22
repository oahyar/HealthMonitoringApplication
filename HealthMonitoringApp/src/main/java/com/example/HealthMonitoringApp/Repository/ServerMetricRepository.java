package com.example.HealthMonitoringApp.Repository;

import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.ServerMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerMetricRepository extends JpaRepository<ServerMetric, Long> {

    List<ServerMetric> findByHostname(String hostname);
    @Query(value = "SELECT * FROM diskspace.server_metrics", nativeQuery = true)
    List<ServerMetric> findAllMetrics();

    @Query(value = """
    SELECT 
        sm.hostname,
        SUM(sm.total_diskspace_mb) AS totalDiskspace,
        SUM(sm.available_diskspace_mb) AS totalAvailableDisk,
        SUM(sm.used_diskspace_mb) AS totalUsedDisk,
        CAST((SUM(sm.used_diskspace_mb) * 100) / NULLIF(SUM(sm.total_diskspace_mb), 0) AS BIGINT) AS usagePct
    FROM diskspace.server_metrics sm
    INNER JOIN (
        SELECT hostname, MAX(timestamp) AS latest_timestamp 
        FROM diskspace.server_metrics 
        GROUP BY hostname
    ) latest ON sm.hostname = latest.hostname AND sm.timestamp = latest.latest_timestamp
    GROUP BY sm.hostname;
    """, nativeQuery = true)
    List<Object[]> findAggregatedSpaceMetrics();

    // Retrieve only the latest record per hostname
    @Query(value = """
    SELECT DISTINCT ON (sm.filesystem) sm.id, sm.hostname, sm.timestamp, sm.size_mb, 
           sm.available_mb, sm.used_mb, sm.usage_pct, sm.mounted_on, sm.filesystem
    FROM diskspace.server_disk_partitions sm
    WHERE sm.hostname = :hostname
    ORDER BY sm.filesystem, sm.timestamp DESC
    """, nativeQuery = true)
    List<ServerDiskPartition> findLatestFilesystemByHostname(@Param("hostname") String hostname);





}
