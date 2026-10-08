/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.xml {
    exports net.sourceforge.pmd.lang.xml;
    exports net.sourceforge.pmd.lang.xml.antlr4;
    exports net.sourceforge.pmd.lang.xml.ast;
    exports net.sourceforge.pmd.lang.xml.cpd;
    exports net.sourceforge.pmd.lang.xml.pom;
    exports net.sourceforge.pmd.lang.xml.rule;
    exports net.sourceforge.pmd.lang.xml.wsdl;
    exports net.sourceforge.pmd.lang.xml.xsl;

    // TODO: consider moving the ruleset files to /META-INF/pmd/category/<lang>/...
    opens category.pom;
    opens category.wsdl;
    opens category.xml;
    opens category.xsl;

    requires net.sourceforge.pmd.core;

    requires org.antlr.antlr4.runtime;
    requires org.apache.commons.lang3;

    requires java.xml;

    // automatic names
    requires Saxon.HE;

    provides net.sourceforge.pmd.lang.Language with
            net.sourceforge.pmd.lang.xml.XmlLanguageModule,
            net.sourceforge.pmd.lang.xml.xsl.XslDialectModule,
            net.sourceforge.pmd.lang.xml.wsdl.WsdlDialectModule,
            net.sourceforge.pmd.lang.xml.pom.PomDialectModule;
}
