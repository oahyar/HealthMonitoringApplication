package com.example.HealthMonitoringApp.Controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.TableSpace;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import com.example.HealthMonitoringApp.Repository.TableSpaceRepository;
import com.example.HealthMonitoringApp.Service.JobMonitorService;
import com.example.HealthMonitoringApp.Service.ServerMetricService;
import com.example.HealthMonitoringApp.Service.TableSpaceService;
import com.example.HealthMonitoringApp.dto.AggregatedSpaceMetrics;
import com.example.HealthMonitoringApp.dto.AggregatedTableSpaceMetrics;
import com.example.HealthMonitoringApp.dto.JobDependencyDTO;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
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

        @Bean
        public JobMonitorService jobMonitorService() {
            return Mockito.mock(JobMonitorService.class);
        }
    }

    @Autowired
    private ServerMetricService serverMetricService;
    @Autowired
    private TableSpaceService tableSpaceService;
    @MockBean
    private JobMonitorService jobMonitorService;
    @MockBean
    private JobLogRepository jobLogRepository;

    @AfterEach
    void resetMocks() {
        reset(jobMonitorService);
    }

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

        mockMvc.perform(get("/disk"))
                .andExpect(status().isOk())
                .andExpect(view().name("disk_status"))
                .andExpect(model().attributeExists(
                        "diskUsages",
                        "tableUsages",
                        "highUsageServers",
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

        mockMvc.perform(get("/disk/getDetailsByHostname")
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

        mockMvc.perform(get("/disk"))
                .andExpect(status().isOk())
                .andExpect(view().name("disk_status"))
                .andExpect(model().attributeExists("highUsageServers", "highUsageDb"))
                .andExpect(model().attribute("highUsageServers", Matchers.empty()))
                .andExpect(model().attribute("highUsageDb", Matchers.empty()));
    }

    @Test
    void testGetJobStatus_success() throws Exception {
        when(jobMonitorService.getJobStatus("myJob", "DEFAULT")).thenReturn("RUNNING");

        mockMvc.perform(get("/myJob/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("RUNNING"));
    }

    @Test
    void testGetJobStatus_schedulerException() throws Exception {
        when(jobMonitorService.getJobStatus("myJob", "DEFAULT"))
                .thenThrow(new SchedulerException("Simulated failure"));

        mockMvc.perform(get("/myJob/status"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error: Simulated failure"));
    }

    @Test
    void testGetLatestStatus_returnsLatestLog() throws Exception {
        JobLog mockLog = new JobLog();
        mockLog.setJobName("testJob");
        mockLog.setStatus("SUCCESS");
        mockLog.setMessage("Done");

        when(jobLogRepository.findTopByJobNameOrderByStartTimeDesc("testJob")).thenReturn(mockLog);

        mockMvc.perform(get("/status/testJob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobName").value("testJob"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Done"));
    }

    @Test
    void testGetLogs_returnsAllLogs() throws Exception {
        List<JobLog> logs = List.of(new JobLog(), new JobLog());
        when(jobLogRepository.findByJobName("myJob")).thenReturn(logs);

        mockMvc.perform(get("/myJob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

//    @Test
//    void testGetJobHistory_shouldReturnListOfJobLogs() throws Exception {
//        JobLog log1 = new JobLog();
//        log1.setId(1L);
//        log1.setJobName("JobA");
//        log1.setStatus("SUCCESS");
//        log1.setMessage("First run");
//
//        JobLog log2 = new JobLog();
//        log2.setId(2L);
//        log2.setJobName("JobA");
//        log2.setStatus("FAILED");
//        log2.setMessage("Second run");
//
//        List<JobLog> mockLogs = List.of(log1, log2);
//
//        when(jobLogRepository.findByJobNameOrderByStartTimeDesc("JobA")).thenReturn(mockLogs);
//
//        mockMvc.perform(get("/history/JobA"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(2))
//                .andExpect(jsonPath("$[0].jobName").value("JobA"))
//                .andExpect(jsonPath("$[0].status").value("SUCCESS"))
//                .andExpect(jsonPath("$[1].status").value("FAILED"));
//    }
    @Test
    void testGetJobGraph_returnsDependencyGraph() throws Exception {
        JobDependencyDTO node = new JobDependencyDTO();
        JobDependencyDTO.Data data = new JobDependencyDTO.Data();
        data.setId("JobA");
        node.setData(data);

        when(jobMonitorService.getJobGraphElements("JobA")).thenReturn(List.of(node));

        mockMvc.perform(get("/api/job-graph").param("jobName", "JobA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].data.id").value("JobA"));
    }
    @Test
    void testGetJobHistory_shouldReturnJobLogs() throws Exception {
        // Arrange
        String jobName = "JobA";
        JobLog log1 = new JobLog();
        log1.setJobName("JobA");
        log1.setStatus("SUCCESS");

        JobLog log2 = new JobLog();
        log2.setJobName("dependentJobA");
        log2.setStatus("RETRY");

        List<JobLog> logs = List.of(log1, log2);
        when(jobMonitorService.getHistoryIncludingDependent(jobName)).thenReturn(logs);

        // Act & Assert
        mockMvc.perform(get("/history/{jobName}", jobName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].jobName").value("JobA"))
                .andExpect(jsonPath("$[1].jobName").value("dependentJobA"));
    }

    @Test
    void testDownloadLogs_shouldReturnMergedLogsAsTextFile() throws Exception {
        String jobName = "Job3";

        List<JobLog> logs = List.of(
                createLog("Job3", "SUCCESS", "Main job done", LocalDateTime.now().minusMinutes(1)),
                createLog("dependentJob3", "FAILED", "Dependent failed", LocalDateTime.now())
        );

        when(jobMonitorService.getHistoryIncludingDependent(eq(jobName))).thenReturn(logs);

        mockMvc.perform(get("/logs/download/" + jobName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=" + jobName + "_logs.txt"))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string(containsString("Job3 SUCCESS: Main job done")))
                .andExpect(content().string(containsString("dependentJob3 FAILED: Dependent failed")));
    }

    @Test
    void downloadLogs_shouldHandleNullAndNonNullEndTime() throws Exception {
        // Log with end time
        JobLog completedLog = new JobLog();
        completedLog.setJobName("JobX");
        completedLog.setStatus("SUCCESS");
        completedLog.setMessage("Completed successfully.");
        completedLog.setStartTime(LocalDateTime.of(2025, 4, 24, 10, 0));
        completedLog.setEndTime(LocalDateTime.of(2025, 4, 24, 10, 30));

        // Log without end time (null)
        JobLog runningLog = new JobLog();
        runningLog.setJobName("JobX");
        runningLog.setStatus("RUNNING");
        runningLog.setMessage("Still running");
        runningLog.setStartTime(LocalDateTime.of(2025, 4, 24, 11, 0));
        runningLog.setEndTime(null);

        when(jobMonitorService.getHistoryIncludingDependent("JobX"))
                .thenReturn(List.of(completedLog, runningLog));

        mockMvc.perform(get("/logs/download/JobX"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string(containsString("Completed successfully.")))
                .andExpect(content().string(containsString("Still running")))
                .andExpect(content().string(containsString(" - 2025-04-24T10:30")))  // ✅ with endTime
                .andExpect(content().string(containsString(" - -")));               // ✅ without endTime (null branch)
    }

    private JobLog createLog(String name, String status, String message, LocalDateTime start) {
        JobLog log = new JobLog();
        log.setJobName(name);
        log.setStatus(status);
        log.setMessage(message);
        log.setStartTime(start);
        log.setEndTime(start.plusSeconds(5));
        return log;
    }


}