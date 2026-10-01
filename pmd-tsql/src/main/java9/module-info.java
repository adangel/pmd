/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.tsql {
    exports net.sourceforge.pmd.lang.tsql;
    exports net.sourceforge.pmd.lang.tsql.ast; // note: this is currently empty...
    exports net.sourceforge.pmd.lang.tsql.cpd;

    requires net.sourceforge.pmd.core;

    requires org.antlr.antlr4.runtime;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.tsql.TSqlLanguageModule;
}
