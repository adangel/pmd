/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.cpp {
    exports net.sourceforge.pmd.lang.cpp;
    exports net.sourceforge.pmd.lang.cpp.cpd;

    requires net.sourceforge.pmd.core;

    requires org.apache.commons.lang3;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.cpp.CppLanguageModule;
}
