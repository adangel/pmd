/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.ant {
    exports net.sourceforge.pmd.ant;

    requires net.sourceforge.pmd.core;

    requires org.apache.commons.lang3;
    requires org.checkerframework.checker.qual;

    // automatic names
    requires ant;
}
