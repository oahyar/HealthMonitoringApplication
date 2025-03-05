package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.TableSpace;
import com.example.HealthMonitoringApp.Repository.TableSpaceRepository;
import com.example.HealthMonitoringApp.dto.AggregatedTableSpaceMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TableSpaceService {

    @Autowired
    private TableSpaceRepository tableSpaceRepository;

    /**
     * Retrieves aggregated tablespace metrics, including total, available, and used space,
     * along with usage percentage for each hostname and SID.
     * @return List of AggregatedTableSpaceMetrics objects with summarized tablespace data.
     */
    public List<AggregatedTableSpaceMetrics> getAggregatedTableSpaceMetrics() {
        List<Object[]> results = tableSpaceRepository.findAggregatedTableSpaceMetrics();
        List<AggregatedTableSpaceMetrics> metrics = new ArrayList<>();

        // Convert query result objects into AggregatedTableSpaceMetrics
        for (Object[] row : results) {
            AggregatedTableSpaceMetrics metric = new AggregatedTableSpaceMetrics(
                    (String) row[0], // Hostname
                    (String) row[1], // SID
                    ((Number) row[2]).longValue(), // Total Tablespace (MB)
                    ((Number) row[3]).longValue(), // Available Tablespace (MB)
                    ((Number) row[4]).longValue(), // Used Tablespace (MB)
                    ((Number) row[5]).longValue()  // Usage Percentage
            );
            metrics.add(metric);
        }
        return metrics;
    }

    /**
     * Retrieves detailed tablespace information for a given hostname and SID.
     * @param hostname The server's hostname.
     * @param sid The database SID.
     * @return List of TableSpace objects with the latest tablespace details.
     */
    public List<TableSpace> getLatestTableSpaceDetails(String hostname, String sid) {
        List<Object[]> results = tableSpaceRepository.findLatestTableSpaceDetails(hostname, sid);
        List<TableSpace> details = new ArrayList<>();

        // Convert query results into TableSpace objects
        for (Object[] row : results) {
            TableSpace detail = new TableSpace(
                    row[0] != null ? ((Number) row[0]).longValue() : null, // ID
                    row[1] != null ? parseTimestamp(row[1]) : null, // Timestamp
                    row[2] != null ? row[2].toString() : null, // Hostname
                    row[3] != null ? row[3].toString() : null, // SID
                    row[4] != null ? row[4].toString() : null, // Tablespace Name
                    row[5] != null ? ((Number) row[5]).longValue() : null, // Free Space (MB)
                    row[6] != null ? ((Number) row[6]).longValue() : null, // Used Space (MB)
                    row[7] != null ? ((Number) row[7]).longValue() : null, // Total Space (MB)
                    row[8] != null ? ((Number) row[8]).longValue() : null  // Usage Percentage
            );
            details.add(detail);
        }
        return details;
    }

    /**
     * Parses an Object timestamp into LocalDateTime.
     * Handles different timestamp formats (SQL Timestamp, String).
     * @param timestamp The timestamp object from the database.
     * @return Parsed LocalDateTime or null if conversion fails.
     */
    private LocalDateTime parseTimestamp(Object timestamp) {
        if (timestamp == null) {
            return null; // Return null if the timestamp is missing
        }

        try {
            if (timestamp instanceof Timestamp) {
                return ((Timestamp) timestamp).toLocalDateTime(); // Convert SQL Timestamp
            } else if (timestamp instanceof String) {
                // Attempt to parse timestamp if it's a valid string
                System.out.println("Parsing timestamp: " + timestamp); // Debugging log
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse((String) timestamp, formatter);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error parsing timestamp: " + timestamp + " | Error: " + e.getMessage());
        }

        return null; // Return null if parsing fails
    }

    /**
     * Retrieves tablespaces with high usage (≥ 70%) for a given SID.
     * @param sid The database SID.
     * @return List of TableSpace objects where usage exceeds 70%.
     */
    public List<TableSpace> getHighUsageTablespaces(String sid) {
        List<Object[]> results = tableSpaceRepository.findHighUsageTablespaces(sid);
        List<TableSpace> details = new ArrayList<>();

        // Convert query results into TableSpace objects
        for (Object[] row : results) {
            TableSpace detail = new TableSpace(
                    row[0] != null ? ((Number) row[0]).longValue() : null, // ID
                    row[1] != null ? parseTimestamp(row[1]) : null, // Timestamp
                    row[2] != null ? row[2].toString() : null, // Hostname
                    row[3] != null ? row[3].toString() : null, // SID
                    row[4] != null ? row[4].toString() : null, // Tablespace Name
                    row[5] != null ? ((Number) row[5]).longValue() : null, // Free Space (MB)
                    row[6] != null ? ((Number) row[6]).longValue() : null, // Used Space (MB)
                    row[7] != null ? ((Number) row[7]).longValue() : null, // Total Space (MB)
                    row[8] != null ? ((Number) row[8]).longValue() : null  // Usage Percentage
            );
            details.add(detail);
        }
        return details;
    }

    /**
     * Retrieves a list of SIDs where at least one tablespace has usage ≥ 70%.
     * Helps in identifying databases at risk of high storage consumption.
     * @return List of TableSpace objects meeting the high-usage threshold.
     */
    public List<TableSpace> getSIDsWithHighUsageThreshold() {
        List<TableSpace> allSIDs = tableSpaceRepository.findAll();
        List<TableSpace> filteredSIDs = new ArrayList<>();

        for (TableSpace tablespace : allSIDs) {
            // Fetch tablespaces where usage is above 70% for the current SID
            List<TableSpace> highUsageTablespaces = tableSpaceRepository.findBySidAndUsageAboveThreshold(tablespace.getSid(), 70);
            if (!highUsageTablespaces.isEmpty()) {
                filteredSIDs.add(tablespace); // Add only if usage ≥ 70%
            }
        }
        return filteredSIDs;
    }
}
