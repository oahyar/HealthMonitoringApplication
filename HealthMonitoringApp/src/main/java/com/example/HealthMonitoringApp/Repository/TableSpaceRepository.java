package com.example.HealthMonitoringApp.Repository;

import com.example.HealthMonitoringApp.Entity.TableSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableSpaceRepository extends JpaRepository<TableSpace, Long> {

    // Retrieves aggregated tablespace metrics for each hostname and SID (System Identifier),
    // calculating total tablespace, available space, used space, and usage percentage.
    @Query(value = """
            SELECT 
                ts.hostname,
                ts.sid,
                SUM(ts.total_space_mb) AS totalTablespace,
                SUM(ts.free_space_mb)  AS totalAvailableTablespace,
                SUM(ts.used_space_mb)  AS totalUsedTablespace,
                CAST((SUM(ts.used_space_mb) * 100) / NULLIF(SUM(ts.total_space_mb),0) AS BIGINT)
                  AS usagePct
            FROM db.database_tablespace ts
            INNER JOIN (
              SELECT hostname, sid, MAX(timestamp) AS latest_timestamp
              FROM db.database_tablespace
              GROUP BY hostname, sid
            ) latest 
              ON ts.hostname = latest.hostname
             AND ts.sid      = latest.sid
             AND ts.timestamp= latest.latest_timestamp
            GROUP BY ts.hostname, ts.sid
            ORDER BY usagePct DESC
            """, nativeQuery = true)
    List<Object[]> findAggregatedTableSpaceMetrics();


    // Retrieves detailed tablespace information for a given hostname and SID.
    @Query(value = """
            SELECT ts.id, ts.timestamp, CAST(ts.hostname AS TEXT), CAST(ts.sid AS TEXT),
                   CAST(ts.tablespace_name AS TEXT), ts.free_space_mb, ts.used_space_mb,
                   ts.total_space_mb, 
                   CAST((ts.used_space_mb * 100) / NULLIF(ts.total_space_mb, 0) AS BIGINT) AS usagePct
            FROM db.database_tablespace ts
            INNER JOIN (
                -- Retrieves the latest timestamp for the specified hostname and SID
                SELECT hostname, sid, MAX(timestamp) AS latest_timestamp
                FROM db.database_tablespace
                WHERE hostname = :hostname AND sid = :sid  -- Ensures filtering by hostname and SID
                GROUP BY hostname, sid
            ) latest ON ts.hostname = latest.hostname 
                    AND ts.sid = latest.sid 
                    AND ts.timestamp = latest.latest_timestamp
            ORDER BY ts.usage_pct DESC;
            """, nativeQuery = true)
    List<Object[]> findLatestTableSpaceDetails(@Param("hostname") String hostname, @Param("sid") String sid);

    // Retrieves tablespaces where usage is above 80% for a given SID, ordered by highest usage percentage.
    @Query(value = """
            SELECT ts.id, ts.timestamp, ts.hostname, ts.sid, ts.tablespace_name, 
                   ts.free_space_mb, ts.used_space_mb, ts.total_space_mb, ts.usage_pct
            FROM db.database_tablespace ts
            INNER JOIN (
                -- Retrieves the latest timestamp for each tablespace within the specified SID
                SELECT sid, tablespace_name, MAX(timestamp) AS latest_timestamp
                FROM db.database_tablespace
                WHERE sid = :sid
                GROUP BY sid, tablespace_name
            ) latest ON ts.sid = latest.sid
                     AND ts.tablespace_name = latest.tablespace_name
                     AND ts.timestamp = latest.latest_timestamp
            WHERE ts.sid = :sid
            AND ts.usage_pct > 80  -- Filter for tablespaces exceeding 80% usage
            ORDER BY ts.usage_pct DESC;
            """, nativeQuery = true)
    List<Object[]> findHighUsageTablespaces(@Param("sid") String sid);
}
