/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.test {
    exports net.sourceforge.pmd.test;
    exports net.sourceforge.pmd.test.lang.rule;

    requires net.sourceforge.pmd.core;
    requires net.sourceforge.pmd.ant;
    requires net.sourceforge.pmd.test.schema;

    requires org.hamcrest;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.junit.platform.launcher;
    requires org.slf4j.simple;
    requires org.apache.commons.lang3;

    requires java.xml;

    // automatic names
    requires system.lambda;
    requires ant;
}
