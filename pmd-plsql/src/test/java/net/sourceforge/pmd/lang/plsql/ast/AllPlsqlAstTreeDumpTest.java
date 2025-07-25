/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.plsql.ast;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    PlsqlTreeDumpTest.class,
    ParenthesisGroupTest.class,
    ExecuteImmediateBulkCollectTest.class,
    SelectIntoStatementTest.class,
    SelectExpressionsTest.class,
    SelectForUpdateTest.class,
    SelectHierarchicalTest.class,
    SelectIntoWithGroupByTest.class,
    WhereClauseTest.class,
    PLSQLParserTest.class,
    FunctionsTest.class,
    XmlDbTreeDumpTest.class
})
class AllPlsqlAstTreeDumpTest {

}
