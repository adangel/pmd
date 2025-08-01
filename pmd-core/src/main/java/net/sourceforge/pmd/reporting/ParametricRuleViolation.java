/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.reporting;

import java.util.Collections;
import java.util.Map;

import net.sourceforge.pmd.lang.document.FileLocation;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.util.AssertionUtil;

/**
 * @apiNote This is internal API. Clients should exclusively use {@link RuleViolation}.
 */
class ParametricRuleViolation implements RuleViolation {
    protected final Rule rule;
    protected final String description;

    private final FileLocation location;

    private final Map<String, String> additionalInfo;

    ParametricRuleViolation(Rule theRule, Reportable node, String message) {
        this(theRule, node.getReportLocation(), message, Collections.emptyMap());
    }

    ParametricRuleViolation(Rule theRule, FileLocation location, String message, Map<String, String> additionalInfo) {
        this.rule = AssertionUtil.requireParamNotNull("rule", theRule);
        this.description = AssertionUtil.requireParamNotNull("message", message);
        this.location = location;

        if (!additionalInfo.isEmpty()) {
            this.additionalInfo = Collections.unmodifiableMap(additionalInfo);
        } else {
            this.additionalInfo = Collections.emptyMap();
        }
    }

    @Override
    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public FileLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return getLocation().startPosToStringWithFile() + ':' + getRule() + ':' + getDescription();
    }
}
