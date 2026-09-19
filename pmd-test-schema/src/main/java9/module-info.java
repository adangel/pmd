/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.test.schema {
    exports net.sourceforge.pmd.test.schema;

    requires net.sourceforge.pmd.core;
    requires java.xml;

    requires org.apache.commons.lang3;

    // automatic names
    requires nice.xml.messages;
}
