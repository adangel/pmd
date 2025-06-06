/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl.javacc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.lang.TokenManager;
import net.sourceforge.pmd.lang.ast.impl.SuppressionCommentImpl;
import net.sourceforge.pmd.reporting.ViolationSuppressor.SuppressionCommentWrapper;

/**
 * A base class for the token managers generated by JavaCC.
 */
public abstract class AbstractTokenManager implements TokenManager<JavaccToken> {

    private final List<SuppressionCommentWrapper> suppressionComments = new ArrayList<>();
    /**
     * @deprecated Since 7.14.0. Don't use this map directly anymore. Instead, use {@link #getSuppressionComments()}.
     */
    @Deprecated
    protected Map<Integer, String> suppressMap = new HashMap<>();
    protected String suppressMarker = PMDConfiguration.DEFAULT_SUPPRESS_MARKER;

    public void setSuppressMarker(String marker) {
        this.suppressMarker = marker;
    }

    /**
     * @deprecated since 7.14.0. Use {@link #getSuppressionComments()} instead.
     */
    @Deprecated
    public Map<Integer, String> getSuppressMap() {
        return suppressMap;
    }

    /**
     * @since 7.14.0
     */
    protected void processCommentForSuppression(JavaccToken token) {
        String suppressMarker = this.suppressMarker;
        int startOfNOPMD = token.getImageCs().indexOf(suppressMarker, 0);
        if (startOfNOPMD != -1) {
            String message = token.getImageCs().substring(startOfNOPMD + suppressMarker.length());
            suppressMap.put(token.getReportLocation().getStartLine(), message);
            suppressionComments.add(new SuppressionCommentImpl<>(token, message));
        }
    }

    /**
     * @since 7.14.0
     */
    public Collection<SuppressionCommentWrapper> getSuppressionComments() {
        return suppressionComments;
    }
}
