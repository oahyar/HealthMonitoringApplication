package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.quartz.listener.JobLoggerListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class SchedulerListenerRegistrarTest {

    private SchedulerListenerRegistrar registrar;
    private Scheduler scheduler;
    private JobLoggerListener jobLoggerListener;

    @BeforeEach
    public void setUp() {
        scheduler = mock(Scheduler.class);
        jobLoggerListener = mock(JobLoggerListener.class);
        registrar = new SchedulerListenerRegistrar();

        // Use reflection or constructor injection if needed
        registrar.scheduler = scheduler;
        registrar.jobLoggerListener = jobLoggerListener;
    }

    @Test
    public void testRegisterListener_whenSchedulerThrows_shouldThrowRuntimeException() throws SchedulerException {
        when(scheduler.getListenerManager()).thenThrow(new SchedulerException("Mock failure"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            registrar.registerListener();
        });

        verify(scheduler, times(1)).getListenerManager();
        assert exception.getMessage().contains("Failed to register JobLoggerListener");
    }
}