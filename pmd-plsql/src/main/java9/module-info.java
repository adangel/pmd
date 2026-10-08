/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.plsql {
    exports net.sourceforge.pmd.lang.plsql;
    exports net.sourceforge.pmd.lang.plsql.ast;
    exports net.sourceforge.pmd.lang.plsql.cpd;
    exports net.sourceforge.pmd.lang.plsql.metrics;
    exports net.sourceforge.pmd.lang.plsql.rule;
    exports net.sourceforge.pmd.lang.plsql.rule.codestyle;
    exports net.sourceforge.pmd.lang.plsql.rule.design;
    exports net.sourceforge.pmd.lang.plsql.symboltable;

    // TODO: consider moving the ruleset files to /META-INF/pmd/category/<lang>/...
    opens category.plsql;

    requires net.sourceforge.pmd.core;

    requires org.apache.commons.lang3;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.plsql.PLSQLLanguageModule;
}
