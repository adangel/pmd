/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.core {
    exports net.sourceforge.pmd;
    exports net.sourceforge.pmd.annotation;
    exports net.sourceforge.pmd.benchmark;
    //exports net.sourceforge.pmd.cache; // it's emtpy right now
    exports net.sourceforge.pmd.cpd;
    exports net.sourceforge.pmd.cpd.impl;
    exports net.sourceforge.pmd.lang;
    exports net.sourceforge.pmd.lang.ast;
    exports net.sourceforge.pmd.lang.ast.impl;
    exports net.sourceforge.pmd.lang.document;
    exports net.sourceforge.pmd.lang.impl;
    exports net.sourceforge.pmd.lang.metrics;
    exports net.sourceforge.pmd.lang.rule;
    exports net.sourceforge.pmd.lang.rule.impl;
    exports net.sourceforge.pmd.lang.rule.xpath;
    exports net.sourceforge.pmd.lang.rule.xpath.impl;
    exports net.sourceforge.pmd.lang.symboltable;
    exports net.sourceforge.pmd.properties;
    exports net.sourceforge.pmd.renderers;
    exports net.sourceforge.pmd.reporting;
    exports net.sourceforge.pmd.util;
    exports net.sourceforge.pmd.util.database;
    exports net.sourceforge.pmd.util.designerbindings;
    exports net.sourceforge.pmd.util.log;
    exports net.sourceforge.pmd.util.treeexport;

    requires org.slf4j;
    requires org.checkerframework.checker.qual;
    requires org.apache.commons.lang3;
    requires org.pcollections;
    requires org.antlr.antlr4.runtime;
    requires com.google.gson;
    requires org.objectweb.asm;

    requires java.desktop;
    requires java.sql;
    requires java.xml;

    // automatic names
    requires nice.xml.messages;
    requires jul.to.slf4j;
    requires Saxon.HE;
}
