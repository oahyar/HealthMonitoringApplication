package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.TableSpace;
import com.example.HealthMonitoringApp.Repository.TableSpaceRepository;
import com.example.HealthMonitoringApp.dto.AggregatedTableSpaceMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TableSpaceService {

    // Logger for recording service operations and errors
    private static final Logger logger = LoggerFactory.getLogger(TableSpaceService.class);

    // Repository for querying tablespace data
    @Autowired
    private TableSpaceRepository tableSpaceRepository;

    /**
     * Fetches aggregated tablespace metrics across hosts and SIDs.
     * Each row is mapped into an AggregatedTableSpaceMetrics DTO.
     */
    public List<AggregatedTableSpaceMetrics> getAggregatedTableSpaceMetrics() {
        logger.info("Fetching aggregated tablespace metrics...");
        // Execute the repository query returning raw Object[] rows
        List<Object[]> results = tableSpaceRepository.findAggregatedTableSpaceMetrics();
        List<AggregatedTableSpaceMetrics> metrics = new ArrayList<>();

        // Convert each raw row into the DTO
        for (Object[] row : results) {
            try {
                AggregatedTableSpaceMetrics metric = new AggregatedTableSpaceMetrics(
                        (String) row[0],                    // hostname
                        (String) row[1],                    // SID
                        ((Number) row[2]).longValue(),      // total tablespace size
                        ((Number) row[3]).longValue(),      // total used space
                        ((Number) row[4]).longValue(),      // total free space
                        ((Number) row[5]).longValue()       // usage percentage
                );
                metrics.add(metric);
            } catch (Exception e) {
                // Log conversion errors but continue processing other rows
                logger.error("Error processing aggregated row: {}", row, e);
            }
        }

        logger.info("Successfully retrieved {} aggregated tablespace metrics.", metrics.size());
        return metrics;
    }

    /**
     * Retrieves the latest tablespace details for a specific hostname and SID.
     * Maps each raw row into a TableSpace entity.
     */
    public List<TableSpace> getLatestTableSpaceDetails(String hostname, String sid) {
        logger.info("Fetching latest tablespace details for hostname: {} and SID: {}", hostname, sid);
        // Query returns raw Object[] rows
        List<Object[]> results = tableSpaceRepository.findLatestTableSpaceDetails(hostname, sid);
        List<TableSpace> details = new ArrayList<>();

        // Convert each raw row into a TableSpace instance
        for (Object[] row : results) {
            try {
                TableSpace detail = new TableSpace(
                        row[0] != null ? ((Number) row[0]).longValue() : null,      // tablespace ID
                        row[1] != null ? parseTimestamp(row[1]) : null,            // timestamp
                        row[2] != null ? row[2].toString() : null,                  // tablespace name
                        row[3] != null ? row[3].toString() : null,                  // file name
                        row[4] != null ? row[4].toString() : null,                  // file path
                        row[5] != null ? ((Number) row[5]).longValue() : null,      // total size
                        row[6] != null ? ((Number) row[6]).longValue() : null,      // used size
                        row[7] != null ? ((Number) row[7]).longValue() : null,      // free size
                        row[8] != null ? ((Number) row[8]).longValue() : null       // usage percentage
                );
                details.add(detail);
            } catch (Exception e) {
                // Log row-level errors without aborting the loop
                logger.error("Error processing tablespace details for {} / {}: {}", hostname, sid, e.getMessage(), e);
            }
        }

        logger.info("Retrieved {} tablespace records for hostname: {} and SID: {}", details.size(), hostname, sid);
        return details;
    }

    /**
     * Parses a timestamp value returned from the database into a LocalDateTime.
     * Supports java.sql.Timestamp and String representations.
     */
    LocalDateTime parseTimestamp(Object timestamp) {
        if (timestamp == null) {
            logger.warn("Received null timestamp for parsing.");
            return null;
        }

        try {
            if (timestamp instanceof Timestamp) {
                // Direct conversion for JDBC Timestamp
                return ((Timestamp) timestamp).toLocalDateTime();
            } else if (timestamp instanceof String) {
                // Parse string using the expected pattern
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse((String) timestamp, formatter);
            }
        } catch (Exception e) {
            // Log parsing errors and return null
            logger.error("Error parsing timestamp: {} | {}", timestamp, e.getMessage(), e);
        }

        return null;
    }

    /**
     * Finds tablespaces for the given SID that exceed the usage threshold.
     * Returns those high-usage records for further action or alerting.
     */
    public List<TableSpace> getHighUsageTablespaces(String sid) {
        logger.info("Fetching high usage tablespaces for SID: {}", sid);
        // Execute the repository query for high-usage partitions
        List<Object[]> results = tableSpaceRepository.findHighUsageTablespaces(sid);
        List<TableSpace> details = new ArrayList<>();

        // Convert each raw row into a TableSpace instance
        for (Object[] row : results) {
            try {
                TableSpace detail = new TableSpace(
                        row[0] != null ? ((Number) row[0]).longValue() : null,
                        row[1] != null ? parseTimestamp(row[1]) : null,
                        row[2] != null ? row[2].toString() : null,
                        row[3] != null ? row[3].toString() : null,
                        row[4] != null ? row[4].toString() : null,
                        row[5] != null ? ((Number) row[5]).longValue() : null,
                        row[6] != null ? ((Number) row[6]).longValue() : null,
                        row[7] != null ? ((Number) row[7]).longValue() : null,
                        row[8] != null ? ((Number) row[8]).longValue() : null
                );
                details.add(detail);
            } catch (Exception e) {
                // Log row-level errors but continue processing
                logger.error("Error processing high-usage tablespace for SID {}: {}", sid, e.getMessage(), e);
            }
        }

        logger.info("Found {} high usage tablespaces for SID: {}", details.size(), sid);
        return details;
    }
}
