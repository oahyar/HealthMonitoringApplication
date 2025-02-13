package com.example.HealthMonitoringApp;

import com.example.HealthMonitoringApp.Entity.ServerMetric;
import com.example.HealthMonitoringApp.Service.ServerMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class HealthMonitoringAppApplication implements CommandLineRunner {

	@Autowired
	private ServerMetricService serverMetricService;

	public static void main(String[] args) {
		SpringApplication.run(HealthMonitoringAppApplication.class, args);
		System.out.println("✅ Health Monitoring App is running...");
	}
	@Override
	public void run(String... args) throws Exception {
		System.out.println("Fetching server metrics...");

		List<ServerMetric> metrics = serverMetricService.getAllMetrics();
		if (metrics.isEmpty()) {
			System.out.println("⚠ No data found in server_metrics table.");
		} else {
			metrics.forEach(System.out::println);
		}
	}


}
