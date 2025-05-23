package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.TableSpace;
import com.example.HealthMonitoringApp.Repository.TableSpaceRepository;
import com.example.HealthMonitoringApp.dto.AggregatedTableSpaceMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.List;
import java.time.LocalDateTime;

class TableSpaceServiceTest {

    @Mock
    private TableSpaceRepository tableSpaceRepository;

    @InjectMocks
    private TableSpaceService tableSpaceService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAggregatedTableSpaceMetrics_withInvalidRow_shouldSkipAndContinue() {
        // One valid row, one invalid row where type casting or null will break
        Object[] validRow = new Object[] {
                "db-server01", "ORCL1", 10000L, 4000L, 6000L, 60L
        };
        Object[] invalidRow = new Object[] {
                null, "SID", "badNumber", null, "oops", 90L
        };

        when(tableSpaceRepository.findAggregatedTableSpaceMetrics())
                .thenReturn(List.of(validRow, invalidRow));

        List<AggregatedTableSpaceMetrics> result = tableSpaceService.getAggregatedTableSpaceMetrics();

        // Only the valid row should be processed
        assertThat(result).hasSize(1);
        AggregatedTableSpaceMetrics metric = result.get(0);
        assertThat(metric.getHostname()).isEqualTo("db-server01");
        assertThat(metric.getSid()).isEqualTo("ORCL1");

        verify(tableSpaceRepository).findAggregatedTableSpaceMetrics();
    }

    @Test
    public void testGetLatestTableSpaceDetails_withValidAndInvalidRows_shouldSkipInvalid() {
        String hostname = "db-server01";
        String sid = "ORCL1";

        // Valid row: all fields correct
        Object[] validRow = new Object[] {
                1L,
                java.sql.Timestamp.valueOf(LocalDateTime.now()),
                "db-server01",
                "ORCL1",
                "USERS",
                1000L,
                3000L,
                4000L,
                75L
        };

        // Invalid row: wrong type for one of the numeric fields
        Object[] invalidRow = new Object[] {
                2L,
                java.sql.Timestamp.valueOf(LocalDateTime.now()),
                "db-server02",
                "ORCL2",
                "SYSTEM",
                "NotANumber",  // This will throw during ((Number) row[5]).longValue()
                2000L,
                3000L,
                66L
        };

        when(tableSpaceRepository.findLatestTableSpaceDetails(hostname, sid))
                .thenReturn(List.of(validRow, invalidRow));

        List<TableSpace> result = tableSpaceService.getLatestTableSpaceDetails(hostname, sid);

        // ✅ Only the valid row should be parsed
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTablespaceName()).isEqualTo("USERS");
        assertThat(result.get(0).getHostname()).isEqualTo("db-server01");

        // ✅ Repository call should be verified
        verify(tableSpaceRepository).findLatestTableSpaceDetails(hostname, sid);
    }

    @Test
    public void testGetHighUsageTablespaces_withValidAndInvalidRows_shouldSkipInvalid() {
        String sid = "ORCL1";

        // Valid row
        Object[] validRow = new Object[] {
                1L,
                Timestamp.valueOf("2024-03-26 12:00:00"),
                "db-server01",
                sid,
                "USERS",
                1000L,
                3000L,
                4000L,
                75L
        };

        // Invalid row: bad value for row[5]
        Object[] invalidRow = new Object[] {
                2L,
                Timestamp.valueOf("2024-03-26 13:00:00"),
                "db-server02",
                sid,
                "SYSTEM",
                "oops",  // not a Number
                1000L,
                2000L,
                66L
        };

        when(tableSpaceRepository.findHighUsageTablespaces(sid))
                .thenReturn(List.of(validRow, invalidRow));

        List<TableSpace> result = tableSpaceService.getHighUsageTablespaces(sid);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTablespaceName()).isEqualTo("USERS");
        assertThat(result.get(0).getSid()).isEqualTo("ORCL1");

        verify(tableSpaceRepository).findHighUsageTablespaces(sid);
    }

    @Test
    public void testGetLatestTableSpaceDetails_withNullFields_shouldParseSuccessfully() {
        String hostname = "db-server01";
        String sid = "ORCL1";

        Object[] rowWithNulls = new Object[] {
                null,                                      // id
                null,                                      // timestamp
                null,
                null,
                null,
                null,
                null,                                      // usedSpace
                null,
                null
        };

        when(tableSpaceRepository.findLatestTableSpaceDetails(hostname, sid))
                .thenReturn(List.<Object[]>of(rowWithNulls));

        List<TableSpace> result = tableSpaceService.getLatestTableSpaceDetails(hostname, sid);

        assertThat(result).hasSize(1);
        TableSpace ts = result.get(0);
        assertThat(ts.getId()).isNull();
        assertThat(ts.getTimestamp()).isNull();
        assertThat(ts.getHostname()).isNull();
        assertThat(ts.getSid()).isNull();
        assertThat(ts.getTablespaceName()).isNull();
        assertThat(ts.getFreeSpaceMb()).isNull();
        assertThat(ts.getUsedSpaceMb()).isNull(); // row[6] was null
        assertThat(ts.getTotalSpaceMb()).isNull();
        assertThat(ts.getUsagePct()).isNull();
    }

    @Test
    public void testGetHighUsageTablespaces_withNullFields_shouldParseSuccessfully() {
        String sid = "ORCL1";

        Object[] rowWithNulls = new Object[] {
                null,                                      // id
                null,                                      // timestamp
                null,                             // hostname
                null,                                       // sid
                null,                                  // tablespaceName
                null,                                     // free space
                null,                                      // used space
                null,                                     // total space
                null                                        // usagePct
        };

        when(tableSpaceRepository.findHighUsageTablespaces(sid))
                .thenReturn(List.<Object[]>of(rowWithNulls));

        List<TableSpace> result = tableSpaceService.getHighUsageTablespaces(sid);

        assertThat(result).hasSize(1);
        TableSpace ts = result.get(0);

        assertThat(ts.getId()).isNull();
        assertThat(ts.getTimestamp()).isNull();
        assertThat(ts.getHostname()).isNull();
        assertThat(ts.getSid()).isNull();
        assertThat(ts.getTablespaceName()).isNull();
        assertThat(ts.getFreeSpaceMb()).isNull();
        assertThat(ts.getUsedSpaceMb()).isNull(); // important for this test!
        assertThat(ts.getTotalSpaceMb()).isNull();
        assertThat(ts.getUsagePct()).isNull();

        verify(tableSpaceRepository).findHighUsageTablespaces(sid);
    }


}