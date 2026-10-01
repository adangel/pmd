/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.swift {
    exports net.sourceforge.pmd.lang.swift;
    exports net.sourceforge.pmd.lang.swift.ast;
    exports net.sourceforge.pmd.lang.swift.cpd;
    exports net.sourceforge.pmd.lang.swift.rule;
    exports net.sourceforge.pmd.lang.swift.rule.bestpractices;

    requires net.sourceforge.pmd.core;

    requires org.antlr.antlr4.runtime;
    requires org.apache.commons.lang3;
    requires org.checkerframework.checker.qual;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.swift.SwiftLanguageModule;
}
