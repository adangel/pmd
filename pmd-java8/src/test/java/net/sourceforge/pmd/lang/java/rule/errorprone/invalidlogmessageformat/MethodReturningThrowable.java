/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package net.sourceforge.pmd.lang.java.rule.errorprone.invalidlogmessageformat;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodReturningThrowable {
    private static final Logger LOG = LoggerFactory.getLogger(MethodReturningThrowable.class);

    public static class Event {
        private Throwable lastThrowable;
        public Throwable getLastThrowable() {
            return lastThrowable;
        }
    }

    public void bar1(Event event) {
        LOG.warn("Failed on {}", 1, event.getLastThrowable());
    }

    public void bar2(Event event) {
        Optional.of(event).ifPresent(e -> LOG.warn(
            "Failed on {}",
            1,
            (Throwable) e.getLastThrowable()));
    }
}
