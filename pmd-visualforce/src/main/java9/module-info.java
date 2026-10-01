/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.visualforce {
    exports net.sourceforge.pmd.lang.visualforce;
    exports net.sourceforge.pmd.lang.visualforce.ast;
    exports net.sourceforge.pmd.lang.visualforce.cpd;
    exports net.sourceforge.pmd.lang.visualforce.rule;
    exports net.sourceforge.pmd.lang.visualforce.rule.security;

    requires net.sourceforge.pmd.core;
    requires net.sourceforge.pmd.lang.apex;

    requires org.apache.commons.lang3;
    requires org.checkerframework.checker.qual;
    requires com.google.common;

    requires java.xml;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.visualforce.VfLanguageModule;
}
