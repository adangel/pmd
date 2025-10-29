/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.internal;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeakMemoryUsageMonitor implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(PeakMemoryUsageMonitor.class.getName());

    private ScheduledExecutorService scheduledExecutorService;

    public PeakMemoryUsageMonitor() {
        if (LOGGER.isLoggable(Level.FINE)) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    LOGGER.fine(PeakMemoryUsageMonitor.this.determinePeakMemory());
                }
            }, 0L, 1L, TimeUnit.SECONDS);
        }
    }

    private String determinePeakMemory() {
        long bytes = 0;
        for (MemoryPoolMXBean bean : ManagementFactory.getMemoryPoolMXBeans()) {
            bytes += bean.getPeakUsage().getUsed();
        }
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
        LOGGER.fine(determinePeakMemory());
    }

    @Override
    public void close() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            try {
                if (!scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOGGER.warning("Couldn't terminate scheduledExecutorService");
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
