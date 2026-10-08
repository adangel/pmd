/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.html {
    exports net.sourceforge.pmd.lang.html;
    exports net.sourceforge.pmd.lang.html.ast;
    exports net.sourceforge.pmd.lang.html.cpd;
    exports net.sourceforge.pmd.lang.html.rule;
    exports net.sourceforge.pmd.lang.html.rule.bestpractices;

    // TODO: consider moving the ruleset files to /META-INF/pmd/category/<lang>/...
    opens category.html;

    requires net.sourceforge.pmd.core;

    requires org.jsoup; // provides a modules-info.java only for Java 11+

    requires org.checkerframework.checker.qual;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.html.HtmlLanguageModule;
}
