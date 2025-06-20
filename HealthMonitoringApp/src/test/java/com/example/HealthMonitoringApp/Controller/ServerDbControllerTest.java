package com.example.HealthMonitoringApp.Controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;

import com.example.HealthMonitoringApp.Entity.ApiStatusLog;
import com.example.HealthMonitoringApp.Entity.JobLog;
import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.TableSpace;
import com.example.HealthMonitoringApp.Repository.ApiStatusLogRepository;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import com.example.HealthMonitoringApp.Service.ApiHealthService;
import com.example.HealthMonitoringApp.Service.JobMonitorService;
import com.example.HealthMonitoringApp.Service.ServerMetricService;
import com.example.HealthMonitoringApp.Service.TableSpaceService;
import com.example.HealthMonitoringApp.Wiremock.properties.MonitorProperties;
import com.example.HealthMonitoringApp.dto.AggregatedSpaceMetrics;
import com.example.HealthMonitoringApp.dto.AggregatedTableSpaceMetrics;
import com.example.HealthMonitoringApp.dto.JobDependencyDTO;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.time.LocalDateTime;


@SpringBootTest
@AutoConfigureMockMvc
class ServerDbControllerTest {

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
    private ApiHealthService apiHealthService;
    @MockBean
    private JobLogRepository jobLogRepository;
    @MockBean
    private ApiStatusLogRepository apiStatusLogRepository;
    @MockBean
    private MonitorProperties monitorProperties;
    @InjectMocks
    private ServerDbController serverDbController;
    @InjectMocks
    private JobController jobController;
    @Mock
    private Scheduler scheduler;

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

    @Test
    void testGetNextFireTime_ReturnsCorrectTime() throws SchedulerException {
        String jobName = "MyJob";

        Trigger trigger = Mockito.mock(Trigger.class);
        Date futureDate = Date.from(Instant.parse("2025-04-24T10:00:00Z"));
        Mockito.when(trigger.getNextFireTime()).thenReturn(futureDate);

        Mockito.doReturn(List.of(trigger))
                .when(scheduler).getTriggersOfJob(new JobKey(jobName));

        LocalDateTime result = jobController.getNextFireTime(jobName);

        assertEquals(LocalDateTime.ofInstant(futureDate.toInstant(), ZoneId.systemDefault()), result);
    }

    @Test
    void testGetNextFireTime_NoTriggers_ReturnsNull() throws SchedulerException {
        String jobName = "EmptyJob";

        Mockito.when(scheduler.getTriggersOfJob(new JobKey(jobName)))
                .thenReturn(Collections.emptyList());

        LocalDateTime result = jobController.getNextFireTime(jobName);

        assertNull(result);
    }

    @Test
    void testGetNextFireTime_NullNextFireTime_ReturnsNull() throws SchedulerException {
        String jobName = "NullTriggerJob";

        Trigger trigger = Mockito.mock(Trigger.class);
        Mockito.when(trigger.getNextFireTime()).thenReturn(null);

        Mockito.doReturn(List.of(trigger))
                .when(scheduler).getTriggersOfJob(new JobKey(jobName));

        LocalDateTime result = jobController.getNextFireTime(jobName);

        assertNull(result);
    }

    @Test
    void testRecentChecks_ReturnsTop10Logs() throws Exception {
        ApiStatusLog log1 = new ApiStatusLog();
        log1.setId(1L);
        log1.setApiName("API1");
        log1.setUp(true);

        ApiStatusLog log2 = new ApiStatusLog();
        log2.setId(2L);
        log2.setApiName("API2");
        log2.setUp(false);

        List<ApiStatusLog> mockLogs = List.of(log1, log2);

        Mockito.when(apiStatusLogRepository.findTop10ByOrderByTimestampDesc()).thenReturn(mockLogs);

        mockMvc.perform(get("/api/status/recent"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(mockLogs.size()));
    }

    @Test
    void testApiSummaryPage_WithApiNameParam() throws Exception {
        // Mock config values
        Mockito.when(monitorProperties.getWarnThreshold()).thenReturn(200L);
        Mockito.when(monitorProperties.getCritThreshold()).thenReturn(500L);

        mockMvc.perform(get("/api-summary?apiName=orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("api_status"))
                .andExpect(model().attribute("warnThreshold", 200L))
                .andExpect(model().attribute("critThreshold", 500L))
                .andExpect(model().attribute("apiName", "orders"));
    }

    @Test
    void testApiSummaryPage_WithoutApiNameParam() throws Exception {
        // Mock config values
        Mockito.when(monitorProperties.getWarnThreshold()).thenReturn(210L);
        Mockito.when(monitorProperties.getCritThreshold()).thenReturn(510L);

        mockMvc.perform(get("/api-summary"))
                .andExpect(status().isOk())
                .andExpect(view().name("api_status"))
                .andExpect(model().attribute("warnThreshold", 210L))
                .andExpect(model().attribute("critThreshold", 510L))
                .andExpect(model().attribute("apiName", nullValue()));
    }

    @Test
    void testDashboardPageLoadsWithApiStatuses() throws Exception {
        ApiStatusLog log1 = new ApiStatusLog();
        log1.setId(1L);
        log1.setApiName("API1");
        log1.setUp(true);
        log1.setMessage(null);

        ApiStatusLog log2 = new ApiStatusLog();
        log2.setId(2L);
        log2.setApiName("API2");
        log2.setUp(false);
        log2.setMessage("500 error");

        List<ApiStatusLog> mockLogs = List.of(log1, log2);

        Mockito.when(apiStatusLogRepository.findLatestPerApi()).thenReturn(mockLogs);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("apiStatuses"))
                .andExpect(model().attribute("apiStatuses", mockLogs));
    }

    @Test
    void testGetHighUsageServers() throws Exception {
        // Sample host metrics
        AggregatedSpaceMetrics server1 = new AggregatedSpaceMetrics("host1", 200L, 20L,180L,90L);
        AggregatedSpaceMetrics server2 = new AggregatedSpaceMetrics("host2", 200L, 180L,20L,10L);
        List<AggregatedSpaceMetrics> allServers = List.of(server1, server2);

        ServerDiskPartition mockPartition = Mockito.mock(ServerDiskPartition.class);

        // Mock service returns
        Mockito.when(serverMetricService.getAggregatedSpaceMetrics()).thenReturn(allServers);
        Mockito.when(serverMetricService.getHighUsageFilesystems("host1"))
                .thenReturn(List.of(mockPartition));  // Host1 has high usage
        Mockito.when(serverMetricService.getHighUsageFilesystems("host2"))
                .thenReturn(Collections.emptyList());  // Host2 doesn't

        mockMvc.perform(get("/dashboard/server"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(1))  // Only one host returned
                .andExpect(jsonPath("$[0].hostname").value("host1"));
    }

    @Test
    void testGetHighUsageDbs() throws Exception {
        AggregatedTableSpaceMetrics ts1 = new AggregatedTableSpaceMetrics("H1","SID1",200L,20L,180L,90L);

        AggregatedTableSpaceMetrics ts2 = new AggregatedTableSpaceMetrics("H2","SID2",200L,180L,20L,10L);

        List<AggregatedTableSpaceMetrics> allMetrics = List.of(ts1, ts2);

        TableSpace mockTablespace = new TableSpace(); // or mock(TableSpace.class)

        // Mock the service calls
        Mockito.when(tableSpaceService.getAggregatedTableSpaceMetrics())
                .thenReturn(allMetrics);

        Mockito.when(tableSpaceService.getHighUsageTablespaces("SID1"))
                .thenReturn(List.of(mockTablespace)); // SID1 has usage

        Mockito.when(tableSpaceService.getHighUsageTablespaces("SID2"))
                .thenReturn(Collections.emptyList()); // SID2 is ignored

        mockMvc.perform(get("/dashboard/db"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sid").value("SID1"));
    }

    @Test
    void testGetJobStatusCounts() throws Exception {
        // Step 1: mock distinct job names
        List<String> jobNames = List.of("JobA", "JobB", "JobC", "JobD");
        Mockito.when(jobLogRepository.findDistinctJobNames()).thenReturn(jobNames);

        // Step 2: mock latest JobLog for each
        JobLog jobA = new JobLog(); jobA.setStatus("SUCCESS");
        JobLog jobB = new JobLog(); jobB.setStatus("FAILED");
        JobLog jobC = new JobLog(); jobC.setStatus("SUCCESS");
        JobLog jobD = new JobLog(); jobD.setStatus("SUCCESS");

        Mockito.when(jobLogRepository.findTopByJobNameOrderByStartTimeDesc("JobA")).thenReturn(jobA);
        Mockito.when(jobLogRepository.findTopByJobNameOrderByStartTimeDesc("JobB")).thenReturn(jobB);
        Mockito.when(jobLogRepository.findTopByJobNameOrderByStartTimeDesc("JobC")).thenReturn(jobC);
        Mockito.when(jobLogRepository.findTopByJobNameOrderByStartTimeDesc("JobD")).thenReturn(jobD);

        // Step 3: perform the GET request
        mockMvc.perform(get("/dashboard/jobcounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(3))
                .andExpect(jsonPath("$.failCount").value(1))
                .andExpect(jsonPath("$.waitCount").value(0));
    }

    @Test
    void testGetJobStatusCounts_withFiltering() throws Exception {
        // Contains: valid name, empty name, lowercase name
        List<String> jobNames = List.of("JobA", "", "jobb");

        // Only "JobA" should be considered
        JobLog jobA = new JobLog(); jobA.setStatus("SUCCESS");

        Mockito.when(jobLogRepository.findDistinctJobNames()).thenReturn(jobNames);
        Mockito.when(jobLogRepository.findTopByJobNameOrderByStartTimeDesc("JobA")).thenReturn(jobA);

        // These are ignored (but safe to mock as null or not at all)
        Mockito.when(jobLogRepository.findTopByJobNameOrderByStartTimeDesc("")).thenReturn(null);
        Mockito.when(jobLogRepository.findTopByJobNameOrderByStartTimeDesc("jobb")).thenReturn(null);

        mockMvc.perform(get("/dashboard/jobcounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.failCount").value(0))
                .andExpect(jsonPath("$.waitCount").value(0));
    }

    @Test
    void testGetJobStatusCounts_withUnknownStatus() throws Exception {
        List<String> jobNames = List.of("JobB");

        Mockito.when(jobLogRepository.findDistinctJobNames()).thenReturn(jobNames);
        Mockito.when(jobLogRepository.findTopByJobNameOrderByStartTimeDesc("JobB")).thenReturn(null); // Triggers UNKNOWN

        mockMvc.perform(get("/dashboard/jobcounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(0))
                .andExpect(jsonPath("$.failCount").value(0))
                .andExpect(jsonPath("$.waitCount").value(0));
    }

    @Test
    void testGetApiMetrics_withApiName() throws Exception {
        ApiStatusLog log1 = new ApiStatusLog();
        log1.setApiName("users");
        log1.setTimestamp(Instant.now().minusSeconds(30));
        log1.setLatencyMillis(120L);
        log1.setUp(true);

        ApiStatusLog log2 = new ApiStatusLog();
        log2.setApiName("users");
        log2.setTimestamp(Instant.now());
        log2.setLatencyMillis(200L);
        log2.setUp(false);

        Mockito.when(apiStatusLogRepository.findTop20ByApiNameOrderByTimestampDesc("users"))
                .thenReturn(new ArrayList<>(List.of(log2, log1))); // simulate newest to oldest

        mockMvc.perform(get("/dashboard/apimetrics")
                        .param("apiName", "users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").isArray())
                .andExpect(jsonPath("$.latencySeries[0]").value(120))
                .andExpect(jsonPath("$.latencySeries[1]").value(200))
                .andExpect(jsonPath("$.uptimeSeries[0]").value(1))
                .andExpect(jsonPath("$.uptimeSeries[1]").value(0));

        mockMvc.perform(get("/dashboard/apimetrics?apiName=myApi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").exists());

        mockMvc.perform(get("/dashboard/apimetrics?apiName="))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").exists());

        mockMvc.perform(get("/dashboard/apimetrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").exists());

    }

    @Test
    void testGetApiMetrics_withoutApiName() throws Exception {
        ApiStatusLog log = new ApiStatusLog();
        log.setApiName("orders");
        log.setTimestamp(Instant.now());
        log.setLatencyMillis(300L);
        log.setUp(true);

        Mockito.when(apiStatusLogRepository.findTop20ByOrderByTimestampDesc())
                .thenReturn(List.of(log));

        mockMvc.perform(get("/dashboard/apimetrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels[0]").exists())
                .andExpect(jsonPath("$.latencySeries[0]").value(300))
                .andExpect(jsonPath("$.uptimeSeries[0]").value(1));
    }

    @Test
    void testLatestPerApi() throws Exception {
        ApiStatusLog log = new ApiStatusLog();
        log.setApiName("UserAPI");
        log.setTimestamp(Instant.now());
        log.setLatencyMillis(150L);
        log.setUp(true);

        List<ApiStatusLog> logs = List.of(log);
        Mockito.when(apiStatusLogRepository.findLatestPerApi()).thenReturn(logs);

        mockMvc.perform(get("/dashboard/apistatus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].apiName").value("UserAPI"))
                .andExpect(jsonPath("$[0].latencyMillis").value(150))
                .andExpect(jsonPath("$[0].up").value(true));
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