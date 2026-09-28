/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.dart {
    exports net.sourceforge.pmd.lang.dart;
    exports net.sourceforge.pmd.lang.dart.cpd;

    requires net.sourceforge.pmd.core;

    requires org.antlr.antlr4.runtime;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.dart.DartLanguageModule;
}
