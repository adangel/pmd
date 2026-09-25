/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.apex {
    exports net.sourceforge.pmd.lang.apex;
    exports net.sourceforge.pmd.lang.apex.ast;
    exports net.sourceforge.pmd.lang.apex.cpd;
    exports net.sourceforge.pmd.lang.apex.metrics;
    exports net.sourceforge.pmd.lang.apex.multifile;
    exports net.sourceforge.pmd.lang.apex.rule;
    exports net.sourceforge.pmd.lang.apex.rule.bestpractices;
    exports net.sourceforge.pmd.lang.apex.rule.codestyle;
    exports net.sourceforge.pmd.lang.apex.rule.design;
    exports net.sourceforge.pmd.lang.apex.rule.documentation;
    exports net.sourceforge.pmd.lang.apex.rule.errorprone;
    exports net.sourceforge.pmd.lang.apex.rule.performance;
    exports net.sourceforge.pmd.lang.apex.rule.security;

    requires net.sourceforge.pmd.core;
    requires com.google.common; // guava
    requires kotlin.stdlib;
    requires org.apache.commons.lang3;
    requires org.antlr.antlr4.runtime;
    requires org.slf4j;
    requires org.checkerframework.checker.qual;

    requires java.xml;

    // automatic names
    requires apex.parser;
    requires summit.ast;
    // requires apex.ls; // apex-ls_2.13-6.2.0.jar
    requires vf.parser;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.apex.ApexLanguageModule;
}
