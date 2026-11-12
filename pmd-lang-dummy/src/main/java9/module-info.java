/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.dummy {
    exports net.sourceforge.pmd.lang.dummy;
    // rulesets are loaded by pmd-core via the classloader
    opens rulesets.dummy;

    requires net.sourceforge.pmd.core;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.dummy.DummyLanguageModule;
}
