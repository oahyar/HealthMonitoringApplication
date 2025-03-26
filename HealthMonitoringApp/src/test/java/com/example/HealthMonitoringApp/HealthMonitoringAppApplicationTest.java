package com.example.HealthMonitoringApp;

import com.example.HealthMonitoringApp.Service.ServerMetricService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HealthMonitoringAppApplicationTest {

    @Autowired
    private ServerMetricService serverMetricService;

    @Test
    public void contextLoads() {
        // Check that the application context loads and ServerMetricService is injected
        assertThat(serverMetricService).isNotNull();
    }

}