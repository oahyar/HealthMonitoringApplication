package com.example.HealthMonitoringApp;

import com.sun.management.OperatingSystemMXBean;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CpuDuringLoadTest {

    private static final OperatingSystemMXBean osBean =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public static void startCpuLogging(int durationSeconds, String logFilePath) {
        OperatingSystemMXBean osBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        List<Double> cpuUsageList = new ArrayList<>();

        System.out.println("⏱️ CPU logging started for " + durationSeconds + " seconds...");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath))) {
            writer.write("timestamp,cpu_usage_percent\n");

            for (int i = 0; i < durationSeconds; i++) {
                double cpuLoad = osBean.getSystemCpuLoad() * 100;
                if (cpuLoad >= 0) {
                    cpuUsageList.add(cpuLoad);
                    String timestamp = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    writer.write(timestamp + "," + String.format("%.2f", cpuLoad) + "\n");
                }
                Thread.sleep(1000); // log every second
            }

            double max = Collections.max(cpuUsageList);
            double min = Collections.min(cpuUsageList);
            double avg = cpuUsageList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            writer.write("\nSUMMARY\n");
            writer.write("Max CPU Usage: " + String.format("%.2f", max) + "%\n");
            writer.write("Min CPU Usage: " + String.format("%.2f", min) + "%\n");
            writer.write("Avg CPU Usage: " + String.format("%.2f", avg) + "%\n");

            System.out.println("✅ CPU logging complete.");
            System.out.println("Max: " + max + "%, Min: " + min + "%, Avg: " + avg + "%");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

