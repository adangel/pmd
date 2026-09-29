/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.javascript {
    exports net.sourceforge.pmd.lang.ecmascript;
    exports net.sourceforge.pmd.lang.ecmascript.ast;
    exports net.sourceforge.pmd.lang.ecmascript.cpd;
    exports net.sourceforge.pmd.lang.ecmascript.rule;
    exports net.sourceforge.pmd.lang.ecmascript.rule.bestpractices;

    exports net.sourceforge.pmd.lang.typescript;
    exports net.sourceforge.pmd.lang.typescript.ast;
    exports net.sourceforge.pmd.lang.typescript.cpd;

    requires net.sourceforge.pmd.core;

    requires org.antlr.antlr4.runtime;
    requires org.mozilla.rhino;
    requires org.checkerframework.checker.qual;

    provides net.sourceforge.pmd.lang.Language with
            net.sourceforge.pmd.lang.ecmascript.EcmascriptLanguageModule,
            net.sourceforge.pmd.lang.typescript.TsLanguageModule;
}
