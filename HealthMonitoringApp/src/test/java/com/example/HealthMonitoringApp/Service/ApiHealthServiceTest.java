package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.ApiStatusLog;
import com.example.HealthMonitoringApp.Repository.ApiStatusLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiHealthServiceTest {

    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApiStatusLogRepository apiStatusLogRepository;

    private ApiHealthService apiHealthService;

    @BeforeEach
    void setup() {
        // Chain mock builder methods properly
        when(builder.setConnectTimeout(any())).thenReturn(builder);
        when(builder.setReadTimeout(any())).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        // Manual construction using the configured mock builder
        apiHealthService = new ApiHealthService(builder, apiStatusLogRepository);
    }
        @Test
        void testCheck_ApiIsUp() {
            // Mock 200 OK response
            when(restTemplate.getForEntity(eq("http://example.com"), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

            apiHealthService.check("TestAPI", "http://example.com");

            verify(apiStatusLogRepository, times(1)).save(argThat(log ->
                    log.getApiName().equals("TestAPI") &&
                            log.isUp() &&
                            log.getLatencyMillis() >= 0 &&
                            log.getMessage() == null
            ));
        }

        @Test
        void testCheck_ApiReturns500() {
            // Mock 500 Internal Server Error
            when(restTemplate.getForEntity(eq("http://example.com"), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR));

            apiHealthService.check("TestAPI", "http://example.com");

            verify(apiStatusLogRepository, times(1)).save(argThat(log ->
                    log.getApiName().equals("TestAPI") &&
                            !log.isUp() &&
                            log.getLatencyMillis() >= 0 &&
                            log.getMessage().contains("500")
            ));
        }

        @Test
        void testCheck_ApiThrowsException() {
            // Simulate a timeout or DNS failure
            when(restTemplate.getForEntity(eq("http://example.com"), eq(String.class)))
                    .thenThrow(new RestClientException("Connection timed out"));

            apiHealthService.check("TestAPI", "http://example.com");

            verify(apiStatusLogRepository, times(1)).save(argThat(log ->
                    log.getApiName().equals("TestAPI") &&
                            !log.isUp() &&
                            log.getLatencyMillis() >= 0 &&
                            log.getMessage().contains("Connection timed out")
            ));
        }
    }