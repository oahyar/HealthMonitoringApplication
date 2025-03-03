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
        SUM(sm.size_mb) AS total_space_mb,
        SUM(sm.available_mb) AS total_available_space_mb,
        SUM(sm.used_mb) AS total_used_space_mb,
        CAST((SUM(sm.used_mb) * 100) / NULLIF(SUM(sm.size_mb), 0) AS BIGINT) AS usage_pct
    FROM diskspace.server_disk_partitions sm
    INNER JOIN (
        SELECT hostname, filesystem, MAX(timestamp) AS latest_timestamp 
        FROM diskspace.server_disk_partitions 
        GROUP BY hostname, filesystem
    ) latest ON sm.hostname = latest.hostname 
            AND sm.filesystem = latest.filesystem 
            AND sm.timestamp = latest.latest_timestamp
    GROUP BY sm.hostname;
    """, nativeQuery = true)
    List<Object[]> findAggregatedSpaceMetrics();

    // Retrieve only the latest record per hostname
    @Query(value = """
    SELECT sm.id, sm.hostname, sm.timestamp, sm.size_mb, 
           sm.available_mb, sm.used_mb, sm.usage_pct, sm.mounted_on, sm.filesystem
    FROM diskspace.server_disk_partitions sm
    INNER JOIN (
        SELECT hostname, filesystem, MAX(timestamp) AS latest_timestamp
        FROM diskspace.server_disk_partitions
        WHERE hostname = :hostname
        GROUP BY hostname, filesystem
    ) latest ON sm.hostname = latest.hostname 
             AND sm.filesystem = latest.filesystem 
             AND sm.timestamp = latest.latest_timestamp
    WHERE sm.hostname = :hostname
    ORDER BY sm.usage_pct DESC;
    """, nativeQuery = true)
    List<ServerDiskPartition> findLatestFilesystemByHostname(@Param("hostname") String hostname);

    @Query(value = """
    SELECT sm.id, sm.hostname, sm.timestamp, sm.size_mb, 
           sm.available_mb, sm.used_mb, sm.usage_pct, sm.mounted_on, sm.filesystem
    FROM diskspace.server_disk_partitions sm
    INNER JOIN (
        SELECT hostname, filesystem, MAX(timestamp) AS latest_timestamp
        FROM diskspace.server_disk_partitions
        WHERE hostname = :hostname
        GROUP BY hostname, filesystem
    ) latest ON sm.hostname = latest.hostname 
             AND sm.filesystem = latest.filesystem 
             AND sm.timestamp = latest.latest_timestamp
    WHERE sm.hostname = :hostname 
    AND sm.usage_pct > 70  -- Added filter
    ORDER BY sm.usage_pct DESC;
    """, nativeQuery = true)
    List<ServerDiskPartition> findHighUsageFilesystems(@Param("hostname") String hostname);








}
