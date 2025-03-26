package com.example.HealthMonitoringApp.Controller;

import static org.junit.jupiter.api.Assertions.*;

import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.TableSpace;
import com.example.HealthMonitoringApp.Repository.TableSpaceRepository;
import com.example.HealthMonitoringApp.Service.ServerMetricService;
import com.example.HealthMonitoringApp.Service.TableSpaceService;
import com.example.HealthMonitoringApp.dto.AggregatedSpaceMetrics;
import com.example.HealthMonitoringApp.dto.AggregatedTableSpaceMetrics;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Timestamp;
import java.util.List;
import java.time.LocalDateTime;


@SpringBootTest
@AutoConfigureMockMvc
class HealthMonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class MockServiceConfig {
        @Bean
        public ServerMetricService serverMetricService() {
            return Mockito.mock(ServerMetricService.class);
        }

        @Bean
        public TableSpaceService tableSpaceService() {
            return Mockito.mock(TableSpaceService.class);
        }
    }

    @Autowired
    private ServerMetricService serverMetricService;
    @Autowired
    private TableSpaceService tableSpaceService;

    @Test
    public void testShowDashboard_shouldReturnViewWithAttributes() throws Exception {
        // Setup mock data
        AggregatedSpaceMetrics space = new AggregatedSpaceMetrics("host1", 1000L, 300L, 700L, 70L);
        AggregatedTableSpaceMetrics table = new AggregatedTableSpaceMetrics("host1", "ORCL", 10000L, 4000L, 6000L, 60L);
        ServerDiskPartition partition = new ServerDiskPartition();
        partition.setHostname("host1");
        partition.setUsagePct(80);
        TableSpace ts = new TableSpace();
        ts.setSid("ORCL");
        ts.setUsagePct(90L);

        // Mock services
        when(serverMetricService.getAggregatedSpaceMetrics()).thenReturn(List.of(space));
        when(serverMetricService.getHighUsageFilesystems("host1")).thenReturn(List.of(partition));
        when(tableSpaceService.getAggregatedTableSpaceMetrics()).thenReturn(List.of(table));
        when(tableSpaceService.getHighUsageTablespaces("ORCL")).thenReturn(List.of(ts));
        when(serverMetricService.getHighUsageFilesystems(null)).thenReturn(List.of()); // for hostname param

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists(
                        "diskUsages",
                        "tableUsages",
                        "highUsageServers",
                        "highUsageFile",
                        "highUsageDb"
                ));
    }

    @Test
    public void testGetDetailsByHostname_returnsJsonList() throws Exception {
        String hostname = "server01";

        ServerDiskPartition partition = new ServerDiskPartition();
        partition.setHostname(hostname);
        partition.setMountedOn("/");
        partition.setUsagePct(75);

        when(serverMetricService.getDiskDetailByHostname(hostname))
                .thenReturn(List.of(partition));

        mockMvc.perform(get("/dashboard/getDetailsByHostname")
                        .param("hostname", hostname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname").value("server01"))
                .andExpect(jsonPath("$[0].mountedOn").value("/"))
                .andExpect(jsonPath("$[0].usagePct").value(75));
    }

    @Test
    public void testGetAggregatedSpaceMetrics_returnsListAsJson() throws Exception {
        AggregatedSpaceMetrics metrics = new AggregatedSpaceMetrics(
                "server01", 100000L, 40000L, 60000L, 60L
        );

        when(serverMetricService.getAggregatedSpaceMetrics())
                .thenReturn(List.of(metrics));

        mockMvc.perform(get("/aggregated-space"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname").value("server01"))
                .andExpect(jsonPath("$[0].totalDiskspace").value(100000))
                .andExpect(jsonPath("$[0].totalAvailableDisk").value(40000))
                .andExpect(jsonPath("$[0].totalUsedDisk").value(60000))
                .andExpect(jsonPath("$[0].usagePct").value(60));
    }

    @Test
    public void testGetLatestFilesystemByHostname() throws Exception {
        ServerDiskPartition partition = new ServerDiskPartition();
        partition.setHostname("server01");
        partition.setMountedOn("/");
        partition.setUsagePct(70);

        when(serverMetricService.getDiskDetailByHostname("server01"))
                .thenReturn(List.of(partition));

        mockMvc.perform(get("/latest-filesystem/server01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname").value("server01"))
                .andExpect(jsonPath("$[0].mountedOn").value("/"))
                .andExpect(jsonPath("$[0].usagePct").value(70));
    }

    @Test
    public void testGetHighUsageFilesystems() throws Exception {
        ServerDiskPartition disk = new ServerDiskPartition();
        disk.setHostname("server01");
        disk.setUsagePct(85);

        when(serverMetricService.getHighUsageFilesystems("server01"))
                .thenReturn(List.of(disk));

        mockMvc.perform(get("/high-usage-filesystems/server01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname").value("server01"))
                .andExpect(jsonPath("$[0].usagePct").value(85));
    }

    @Test
    public void testGetHighUsageTablespaces() throws Exception {
        TableSpace ts = new TableSpace();
        ts.setSid("ORCL");
        ts.setTablespaceName("SYSTEM");
        ts.setUsagePct(90L);

        when(tableSpaceService.getHighUsageTablespaces("ORCL"))
                .thenReturn(List.of(ts));

        mockMvc.perform(get("/high-usage-db/ORCL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sid").value("ORCL"))
                .andExpect(jsonPath("$[0].tablespaceName").value("SYSTEM"))
                .andExpect(jsonPath("$[0].usagePct").value(90));
    }

    @Test
    public void testGetLatestTableSpaceByHostname() throws Exception {
        TableSpace ts = new TableSpace();
        ts.setHostname("host1");
        ts.setSid("ORCL");
        ts.setTablespaceName("USERS");

        when(tableSpaceService.getLatestTableSpaceDetails("host1", "ORCL"))
                .thenReturn(List.of(ts));

        mockMvc.perform(get("/latest-tablespace/host1/ORCL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname").value("host1"))
                .andExpect(jsonPath("$[0].sid").value("ORCL"))
                .andExpect(jsonPath("$[0].tablespaceName").value("USERS"));
    }

    @Test
    public void testGetAggregatedTableSpaceMetrics() throws Exception {
        AggregatedTableSpaceMetrics metric = new AggregatedTableSpaceMetrics("host1", "ORCL", 10000L, 4000L, 6000L, 60L);

        when(tableSpaceService.getAggregatedTableSpaceMetrics()).thenReturn(List.of(metric));

        mockMvc.perform(get("/aggregated-tablespace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname").value("host1"))
                .andExpect(jsonPath("$[0].sid").value("ORCL"))
                .andExpect(jsonPath("$[0].totalTablespace").value(10000L));
    }

    @Test
    public void testShowDashboard_whenNoHighUsage_shouldNotAddToFilteredLists() throws Exception {
        // Aggregated data returns 1 server and 1 tablespace
        AggregatedSpaceMetrics server = new AggregatedSpaceMetrics("server01", 10000L, 4000L, 6000L, 60L);
        AggregatedTableSpaceMetrics table = new AggregatedTableSpaceMetrics("db-host", "ORCL", 10000L, 3000L, 7000L, 70L);

        when(serverMetricService.getAggregatedSpaceMetrics()).thenReturn(List.of(server));
        when(serverMetricService.getHighUsageFilesystems("server01")).thenReturn(List.of()); // Empty
        when(serverMetricService.getHighUsageFilesystems(null)).thenReturn(List.of());       // hostname param

        when(tableSpaceService.getAggregatedTableSpaceMetrics()).thenReturn(List.of(table));
        when(tableSpaceService.getHighUsageTablespaces("ORCL")).thenReturn(List.of());       // Empty

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("highUsageServers", "highUsageDb"))
                .andExpect(model().attribute("highUsageServers", Matchers.empty()))
                .andExpect(model().attribute("highUsageDb", Matchers.empty()));
    }


}