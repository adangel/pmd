/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd.internal;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.cpd.CPDListener;

public class CPDLoggingListener implements CPDListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CPDLoggingListener.class);

    private final CPDListener delegate;

    public CPDLoggingListener(CPDListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addedFile(int fileCount, File file) {
        delegate.addedFile(fileCount, file);
    }

    @Override
    public void phaseUpdate(int phase) {
        delegate.phaseUpdate(phase);
        switch (phase) {
        case CPDListener.INIT:
            LOGGER.debug("Phase: INIT");
            break;
        case CPDListener.HASH:
            LOGGER.debug("Phase: HASH");
            break;
        case CPDListener.MATCH:
            LOGGER.debug("Phase: MATCH");
            break;
        case CPDListener.GROUPING:
            LOGGER.debug("Phase: GROUPING");
            break;
        case CPDListener.DONE:
            LOGGER.debug("Phase: Done");
            break;
        default:
            LOGGER.debug("Unknown Phase: {}", phase);
            break;
        }
    }
}
