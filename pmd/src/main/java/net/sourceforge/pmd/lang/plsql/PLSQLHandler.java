/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package net.sourceforge.pmd.lang.plsql;

import java.io.Writer;

import net.sf.saxon.sxpath.IndependentContext;
import net.sourceforge.pmd.lang.*;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.xpath.AbstractASTXPathHandler;
import net.sourceforge.pmd.lang.dfa.DFAGraphRule;
import net.sourceforge.pmd.lang.plsql.ast.ASTInput;
import net.sourceforge.pmd.lang.plsql.ast.DumpFacade;
import net.sourceforge.pmd.lang.plsql.ast.PLSQLNode;
import net.sourceforge.pmd.lang.plsql.dfa.DFAPLSQLGraphRule;
import net.sourceforge.pmd.lang.plsql.dfa.DataFlowFacade;
import net.sourceforge.pmd.lang.plsql.rule.PLSQLRuleViolationFactory;
import net.sourceforge.pmd.lang.plsql.symboltable.SymbolFacade;
import net.sourceforge.pmd.lang.rule.RuleViolationFactory;

/**
 * Implementation of LanguageVersionHandler for the PLSQL AST. It uses anonymous classes
 * as adapters of the visitors to the VisitorStarter interface.
 *
 * @author sturton - PLDoc - pldoc.sourceforge.net
 */
public class PLSQLHandler extends AbstractLanguageVersionHandler {

	
    public Parser getParser(ParserOptions parserOptions) {
        return new PLSQLParser(parserOptions);
    }

    public RuleViolationFactory getRuleViolationFactory() {
	return PLSQLRuleViolationFactory.INSTANCE;
    }

    @Override
    public XPathHandler getXPathHandler() {
        return new AbstractASTXPathHandler() {
            public void initialize() {
            }

            public void initialize(IndependentContext context) {
            }
        };
    }
    @Override
    public DFAGraphRule getDFAGraphRule() {
        return new DFAPLSQLGraphRule();
    }

    @Override
    public DataFlowHandler getDataFlowHandler() {
	return new PLSQLDataFlowHandler();
    }

    @Override
    public VisitorStarter getDataFlowFacade() {
	return new VisitorStarter() {
	    public void start(Node rootNode) {
		new DataFlowFacade().initializeWith(getDataFlowHandler(), (ASTInput) rootNode);
	    }
	};
    }

    @Override
    public VisitorStarter getSymbolFacade() {
	return new VisitorStarter() {
	    public void start(Node rootNode) {
		new SymbolFacade().initializeWith((ASTInput) rootNode);
	    }
	};
    }

    @Override
    public VisitorStarter getDumpFacade(final Writer writer, final String prefix, final boolean recurse) {
	return new VisitorStarter() {
	    public void start(Node rootNode) {
		new DumpFacade().initializeWith(writer, prefix, recurse, (PLSQLNode) rootNode);
	    }
	};
    }
}
