/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.kotlin {
    exports net.sourceforge.pmd.lang.kotlin;
    exports net.sourceforge.pmd.lang.kotlin.ast;
    exports net.sourceforge.pmd.lang.kotlin.cpd;
    exports net.sourceforge.pmd.lang.kotlin.rule.bestpractices;
    exports net.sourceforge.pmd.lang.kotlin.rule.errorprone;
    exports net.sourceforge.pmd.lang.kotlin.types;

    // TODO: consider moving the ruleset files to /META-INF/pmd/category/<lang>/...
    opens category.kotlin;

    requires net.sourceforge.pmd.core;

    requires org.antlr.antlr4.runtime;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;
    requires org.apache.commons.lang3;

    // automatic names
    requires kotlin.type.mapper.model;
    requires kotlin.type.mapper.analyzer;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.kotlin.KotlinLanguageModule;
}
