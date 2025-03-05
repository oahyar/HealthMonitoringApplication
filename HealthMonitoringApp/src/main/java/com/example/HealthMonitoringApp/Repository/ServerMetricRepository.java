package com.example.HealthMonitoringApp.Repository;

import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.ServerMetric;
import com.example.HealthMonitoringApp.Entity.TableSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerMetricRepository extends JpaRepository<ServerMetric, Long> {

    // Retrieves all ServerMetric records associated with a given hostname
    List<ServerMetric> findByHostname(String hostname);

    // Retrieves all server metrics from the `server_metrics` table
    @Query(value = "SELECT * FROM diskspace.server_metrics", nativeQuery = true)
    List<ServerMetric> findAllMetrics();

    // Aggregates disk space metrics for each hostname, calculating total size, available space, used space, and usage percentage.
    @Query(value = """
            SELECT 
                sm.hostname,
                SUM(sm.size_mb) AS total_space_mb,
                SUM(sm.available_mb) AS total_available_space_mb,
                SUM(sm.used_mb) AS total_used_space_mb,
                CAST((SUM(sm.used_mb) * 100) / NULLIF(SUM(sm.size_mb), 0) AS BIGINT) AS usage_pct
            FROM diskspace.server_disk_partitions sm
            INNER JOIN (
                -- Retrieves the latest timestamp per hostname and filesystem
                SELECT hostname, filesystem, MAX(timestamp) AS latest_timestamp 
                FROM diskspace.server_disk_partitions 
                GROUP BY hostname, filesystem
            ) latest ON sm.hostname = latest.hostname 
                    AND sm.filesystem = latest.filesystem 
                    AND sm.timestamp = latest.latest_timestamp
            GROUP BY sm.hostname;
            """, nativeQuery = true)
    List<Object[]> findAggregatedSpaceMetrics();

    // Retrieves only the latest disk partition records for a given hostname, sorted by highest usage percentage
    @Query(value = """
            SELECT sm.id, sm.hostname, sm.timestamp, sm.size_mb, 
                   sm.available_mb, sm.used_mb, sm.usage_pct, sm.mounted_on, sm.filesystem
            FROM diskspace.server_disk_partitions sm
            INNER JOIN (
                -- Retrieves the latest timestamp for each filesystem within the specified hostname
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

    // Retrieves disk partitions for a given hostname where the disk usage exceeds 70%, sorted by usage percentage
    @Query(value = """
            SELECT sm.id, sm.timestamp, sm.hostname, sm.size_mb, 
                   sm.available_mb, sm.used_mb, sm.usage_pct, sm.mounted_on, sm.filesystem
            FROM diskspace.server_disk_partitions sm
            INNER JOIN (
                -- Retrieves the latest timestamp per hostname and filesystem
                SELECT hostname, filesystem, MAX(timestamp) AS latest_timestamp
                FROM diskspace.server_disk_partitions
                WHERE hostname = :hostname
                GROUP BY hostname, filesystem
            ) latest ON sm.hostname = latest.hostname 
                     AND sm.filesystem = latest.filesystem 
                     AND sm.timestamp = latest.latest_timestamp
            WHERE sm.hostname = :hostname 
            AND sm.usage_pct > 70  -- Filter to include only partitions with usage greater than 70%
            ORDER BY sm.usage_pct DESC;
            """, nativeQuery = true)
    List<ServerDiskPartition> findHighUsageFilesystems(@Param("hostname") String hostname);

    // Retrieves all filesystems for a given hostname where usage percentage is above the specified threshold
    @Query("SELECT s FROM ServerDiskPartition s WHERE s.hostname = :hostname AND s.usagePct >= :threshold")
    List<ServerDiskPartition> findByHostnameAndUsageAboveThreshold(@Param("hostname") String hostname, @Param("threshold") double threshold);
}
