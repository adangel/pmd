/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.test {
    exports net.sourceforge.pmd.lang.test;

    requires net.sourceforge.pmd.core;
    requires org.apache.commons.lang3;
    requires org.apache.commons.io;
    requires org.jetbrains.annotations;
    requires org.hamcrest;
    requires kotlin.stdlib;
    requires kotlin.reflect;
    requires kotlin.test.junit5;
    requires org.junit.jupiter.api;
    requires org.junit.platform.commons;

    // automatic names
    requires kotest.assertions.core.jvm;
    requires kotest.property.jvm;
    requires kotest.runner.junit5.jvm;
    requires tree.matchers;
    requires tree.printers;
}
