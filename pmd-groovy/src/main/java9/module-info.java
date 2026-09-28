/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.groovy {
    exports net.sourceforge.pmd.lang.groovy;
    exports net.sourceforge.pmd.lang.groovy.ast.impl.antlr4;
    exports net.sourceforge.pmd.lang.groovy.cpd;

    requires net.sourceforge.pmd.core;

    requires org.apache.groovy;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.groovy.GroovyLanguageModule;
}
