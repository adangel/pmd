/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.velocity {
    exports net.sourceforge.pmd.lang.velocity;
    exports net.sourceforge.pmd.lang.velocity.ast;
    exports net.sourceforge.pmd.lang.velocity.cpd;
    exports net.sourceforge.pmd.lang.velocity.rule;
    exports net.sourceforge.pmd.lang.velocity.rule.bestpractices;
    exports net.sourceforge.pmd.lang.velocity.rule.design;
    exports net.sourceforge.pmd.lang.velocity.rule.errorprone;

    // TODO: consider moving the ruleset files to /META-INF/pmd/category/<lang>/...
    opens category.velocity;

    requires net.sourceforge.pmd.core;

    requires org.apache.commons.lang3;
    requires org.checkerframework.checker.qual;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.velocity.VtlLanguageModule;
}
