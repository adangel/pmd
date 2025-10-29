/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd.internal;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.pmd.cpd.CPDListener;

public class CPDLoggingListener implements CPDListener {
    private static final Logger LOGGER = Logger.getLogger(CPDLoggingListener.class.getName());

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
            LOGGER.log(Level.FINE, "Phase: INIT");
            break;
        case CPDListener.HASH:
            LOGGER.log(Level.FINE, "Phase: HASH");
            break;
        case CPDListener.MATCH:
            LOGGER.log(Level.FINE, "Phase: MATCH");
            break;
        case CPDListener.GROUPING:
            LOGGER.log(Level.FINE, "Phase: GROUPING");
            break;
        case CPDListener.DONE:
            LOGGER.log(Level.FINE, "Phase: Done");
            break;
        default:
            LOGGER.log(Level.FINE, "Unknown Phase: {}", phase);
            break;
        }
    }
}
