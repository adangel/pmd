/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.objectivec {
    exports net.sourceforge.pmd.lang.objectivec;
    exports net.sourceforge.pmd.lang.objectivec.cpd;

    requires net.sourceforge.pmd.core;

    requires org.checkerframework.checker.qual;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.objectivec.ObjectiveCLanguageModule;
}
