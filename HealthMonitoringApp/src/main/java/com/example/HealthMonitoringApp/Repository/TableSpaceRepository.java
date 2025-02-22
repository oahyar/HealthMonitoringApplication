package com.example.HealthMonitoringApp.Repository;

import com.example.HealthMonitoringApp.Entity.ServerMetric;
import com.example.HealthMonitoringApp.Entity.TableSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableSpaceRepository extends JpaRepository<TableSpace, Long> {

    // Retrieve summary of tablespace
    @Query(value = """
    SELECT 
        ts.hostname,
        ts.sid,
        SUM(ts.total_space_mb) AS totalTablespace,
        SUM(ts.free_space_mb) AS totalAvailableTablespace,
        SUM(ts.used_space_mb) AS totalUsedTablespace,
        CAST((SUM(ts.used_space_mb) * 100) / NULLIF(SUM(ts.total_space_mb), 0) AS BIGINT) AS usagePct
    FROM db.database_tablespace ts
    INNER JOIN (
        SELECT hostname, sid, MAX(timestamp) AS latest_timestamp 
        FROM db.database_tablespace 
        GROUP BY hostname, sid
    ) latest ON ts.hostname = latest.hostname 
            AND ts.sid = latest.sid 
            AND ts.timestamp = latest.latest_timestamp
    GROUP BY ts.hostname, ts.sid;
    """, nativeQuery = true)
    List<Object[]> findAggregatedTableSpaceMetrics();

    // Retrieve more details of tablespace
    @Query(value = """
    SELECT ts.id, ts.timestamp, CAST(ts.hostname AS TEXT), CAST(ts.sid AS TEXT),
           CAST(ts.tablespace_name AS TEXT), ts.free_space_mb, ts.used_space_mb,
           ts.total_space_mb, 
           CAST((ts.used_space_mb * 100) / NULLIF(ts.total_space_mb, 0) AS BIGINT) AS usagePct
    FROM db.database_tablespace ts
    INNER JOIN (
        SELECT hostname, sid, MAX(timestamp) AS latest_timestamp
        FROM db.database_tablespace
        WHERE hostname = :hostname AND sid = :sid  -- ✅ Fix: Ensure variables are properly treated
        GROUP BY hostname, sid
    ) latest ON ts.hostname = latest.hostname 
            AND ts.sid = latest.sid 
            AND ts.timestamp = latest.latest_timestamp
    ORDER BY ts.tablespace_name;
    """, nativeQuery = true)
    List<Object[]> findLatestTableSpaceDetails(@Param("hostname") String hostname, @Param("sid") String sid);








}

