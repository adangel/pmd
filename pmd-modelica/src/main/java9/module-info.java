/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.modelica {
    exports net.sourceforge.pmd.lang.modelica;
    exports net.sourceforge.pmd.lang.modelica.ast;
    exports net.sourceforge.pmd.lang.modelica.cpd;
    exports net.sourceforge.pmd.lang.modelica.resolver;
    exports net.sourceforge.pmd.lang.modelica.rule;
    exports net.sourceforge.pmd.lang.modelica.rule.bestpractices;

    requires net.sourceforge.pmd.core;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.modelica.ModelicaLanguageModule;
}
