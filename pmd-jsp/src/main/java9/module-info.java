/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.jsp {
    exports net.sourceforge.pmd.lang.jsp;
    exports net.sourceforge.pmd.lang.jsp.ast;
    exports net.sourceforge.pmd.lang.jsp.cpd;
    exports net.sourceforge.pmd.lang.jsp.rule;
    exports net.sourceforge.pmd.lang.jsp.rule.codestyle;
    exports net.sourceforge.pmd.lang.jsp.rule.design;
    exports net.sourceforge.pmd.lang.jsp.rule.security;

    requires net.sourceforge.pmd.core;

    requires org.apache.commons.lang3;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.jsp.JspLanguageModule;
}
