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
import java.util.ArrayList;
import java.util.List;

@Service
public class TableSpaceService {

    private static final Logger logger = LoggerFactory.getLogger(TableSpaceService.class);

    @Autowired
    private TableSpaceRepository tableSpaceRepository;

    public List<AggregatedTableSpaceMetrics> getAggregatedTableSpaceMetrics() {
        logger.info("Fetching aggregated tablespace metrics...");
        List<Object[]> results = tableSpaceRepository.findAggregatedTableSpaceMetrics();
        List<AggregatedTableSpaceMetrics> metrics = new ArrayList<>();

        for (Object[] row : results) {
            try {
                AggregatedTableSpaceMetrics metric = new AggregatedTableSpaceMetrics(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue(),
                        ((Number) row[5]).longValue()
                );
                metrics.add(metric);
            } catch (Exception e) {
                logger.error("Error processing aggregated row: {}", row, e);
            }
        }

        logger.info("Successfully retrieved {} aggregated tablespace metrics.", metrics.size());
        return metrics;
    }

    public List<TableSpace> getLatestTableSpaceDetails(String hostname, String sid) {
        logger.info("Fetching latest tablespace details for hostname: {} and SID: {}", hostname, sid);
        List<Object[]> results = tableSpaceRepository.findLatestTableSpaceDetails(hostname, sid);
        List<TableSpace> details = new ArrayList<>();

        for (Object[] row : results) {
            try {
                TableSpace detail = new TableSpace(
                        row[0] != null ? ((Number) row[0]).longValue() : null,
                        row[1] != null ? ((Timestamp) row[1]).toLocalDateTime() : null,
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
                logger.error("Error processing tablespace details for {} / {}: {}", hostname, sid, e.getMessage(), e);
            }
        }

        logger.info("Retrieved {} tablespace records for hostname: {} and SID: {}", details.size(), hostname, sid);
        return details;
    }

    public List<TableSpace> getHighUsageTablespaces(String sid) {
        logger.info("Fetching high usage tablespaces for SID: {}", sid);
        List<Object[]> results = tableSpaceRepository.findHighUsageTablespaces(sid);
        List<TableSpace> details = new ArrayList<>();

        for (Object[] row : results) {
            try {
                TableSpace detail = new TableSpace(
                        row[0] != null ? ((Number) row[0]).longValue() : null,
                        row[1] != null ? ((Timestamp) row[1]).toLocalDateTime() : null,
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
                logger.error("Error processing high-usage tablespace for SID {}: {}", sid, e.getMessage(), e);
            }
        }

        logger.info("Found {} high usage tablespaces for SID: {}", details.size(), sid);
        return details;
    }
}