package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.TableSpace;
import com.example.HealthMonitoringApp.Repository.TableSpaceRepository;
import com.example.HealthMonitoringApp.dto.AggregatedTableSpaceMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TableSpaceService {

    @Autowired
    private TableSpaceRepository tableSpaceRepository;


    public List<AggregatedTableSpaceMetrics> getAggregatedTableSpaceMetrics() {
        List<Object[]> results = tableSpaceRepository.findAggregatedTableSpaceMetrics();
        List<AggregatedTableSpaceMetrics> metrics = new ArrayList<>();

        for (Object[] row : results) {
            AggregatedTableSpaceMetrics metric = new AggregatedTableSpaceMetrics(
                    (String) row[0], // Hostname
                    (String) row[1], // Sid
                    ((Number) row[2]).longValue(), // Total Tablespace
                    ((Number) row[3]).longValue(), // Available Tablespace
                    ((Number) row[4]).longValue(), // Used Tablespace
                    ((Number) row[5]).longValue()  // Usage Percentage
            );
            metrics.add(metric);
        }
        return metrics;
    }

    public List<TableSpace> getLatestTableSpaceDetails(String hostname, String sid) {
        List<Object[]> results = tableSpaceRepository.findLatestTableSpaceDetails(hostname, sid);
        List<TableSpace> details = new ArrayList<>();
        for (Object[] row : results) {
            TableSpace detail = new TableSpace(
                    row[0] != null ? ((Number) row[0]).longValue() : null, // ID
                    row[1] != null ? row[1].toString() : null, // Timestamp
                    row[2] != null ? row[2].toString() : null, // Hostname
                    row[3] != null ? row[3].toString() : null, // SID
                    row[4] != null ? row[4].toString() : null, // Tablespace Name
                    row[5] != null ? ((Number) row[5]).longValue() : null, // Free Space MB
                    row[6] != null ? ((Number) row[6]).longValue() : null, // Used Space MB
                    row[7] != null ? ((Number) row[7]).longValue() : null, // Total Space MB
                    row[8] != null ? ((Number) row[8]).longValue() : null  // Usage Percentage
            );
            details.add(detail);
        }
        return details;
    }
}
