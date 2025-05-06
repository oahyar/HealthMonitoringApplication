package com.example.HealthMonitoringApp.Wiremock.config;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

@Configuration
@Profile("dev")
public class WireMockConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer wireMockServer() {
        WireMockServer server = new WireMockServer(
                WireMockConfiguration.options().port(8089)
        );

        // stub a healthy endpoint
        server.stubFor(get("/users/health")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(150)     // simulate 150 ms latency
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"status\": \"UP\" }")
                )
        );

        // stub a failing endpoint
        server.stubFor(get("/orders/health")
                .willReturn(aResponse()
                        .withStatus(500)
                        .withFixedDelay(0)
                )
        );

        // stub a missing endpoint
        server.stubFor(get("/unknown/health")
                .willReturn(aResponse().withStatus(404))
        );

        // stub a no response endpoint
        server.stubFor(get("/slow/health")
                .willReturn(aResponse()
                        .withFixedDelay(10_000) // longer than your 5 s timeout
                )
        );

        // stub a flip between health and unhealth endpoint
        server.stubFor(get("/flaky/health")
                        .inScenario("FlakyScenario")
                        .whenScenarioStateIs(STARTED)
                        .willReturn(aResponse().withStatus(500))
                        .willSetStateTo("RECOVERED")
                );

        server.stubFor(get("/flaky/health")
                        .inScenario("FlakyScenario")
                        .whenScenarioStateIs("RECOVERED")
                        .willReturn(aResponse().withStatus(200))
                );

        return server;
    }

    // Override your RestTemplateBuilder if needed, or just let your existing
    // builder pick up the URLs from MonitorProperties.
}