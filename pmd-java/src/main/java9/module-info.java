/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.java {
    exports net.sourceforge.pmd.lang.java;
    exports net.sourceforge.pmd.lang.java.ast;
    exports net.sourceforge.pmd.lang.java.cpd;
    exports net.sourceforge.pmd.lang.java.javadoc;
    exports net.sourceforge.pmd.lang.java.metrics;
    exports net.sourceforge.pmd.lang.java.rule;
    exports net.sourceforge.pmd.lang.java.rule.bestpractices;
    exports net.sourceforge.pmd.lang.java.rule.codestyle;
    exports net.sourceforge.pmd.lang.java.rule.design;
    exports net.sourceforge.pmd.lang.java.rule.documentation;
    exports net.sourceforge.pmd.lang.java.rule.errorprone;
    exports net.sourceforge.pmd.lang.java.rule.multithreading;
    exports net.sourceforge.pmd.lang.java.rule.performance;
    exports net.sourceforge.pmd.lang.java.rule.security;
    // exports net.sourceforge.pmd.lang.java.rule.xpath; // currently empty
    exports net.sourceforge.pmd.lang.java.symbols;
    exports net.sourceforge.pmd.lang.java.symbols.table;
    exports net.sourceforge.pmd.lang.java.symbols.table.coreimpl;
    exports net.sourceforge.pmd.lang.java.types;
    exports net.sourceforge.pmd.lang.java.types.ast;

    // TODO: consider moving the ruleset files to /META-INF/pmd/category/<lang>/...
    opens category.java;
    opens rulesets.java;

    requires net.sourceforge.pmd.core;

    requires org.objectweb.asm;
    requires org.apache.commons.lang3;
    requires org.checkerframework.checker.qual;
    requires org.pcollections;

    requires java.sql;

    // automatic names
    requires Saxon.HE;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.java.JavaLanguageModule;
}
