/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.internal;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeakMemoryUsageMonitor implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PeakMemoryUsageMonitor.class);

    private ScheduledExecutorService scheduledExecutorService;

    public PeakMemoryUsageMonitor() {
        if (LOGGER.isTraceEnabled()) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                LOGGER.trace(determinePeakMemory());
            }, 0L, 1L, TimeUnit.SECONDS);
        }
    }

    private String determinePeakMemory() {
        long bytes = ManagementFactory.getMemoryPoolMXBeans().stream()
                .map(MemoryPoolMXBean::getPeakUsage)
                .mapToLong(MemoryUsage::getUsed)
                .sum();
        String[] units = {"B", "KB", "MiB", "GiB"};
        double unitValue = bytes;
        int unitSelector = 0;
        while (unitValue > 1024 && unitSelector < units.length) {
            unitSelector++;
            unitValue = unitValue / 1024.0;
        }
        return String.format(Locale.ROOT, "Peak Memory Usage: %.2f%s (%dB)", unitValue, units[unitSelector], bytes);
    }

    public void logPeakMemory() {
        LOGGER.debug(determinePeakMemory());
    }

    @Override
    public void close() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            try {
                if (!scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOGGER.warn("Couldn't terminate scheduledExecutorService");
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
