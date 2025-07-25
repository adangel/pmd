/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.modelica.ast;

import net.sourceforge.pmd.lang.modelica.resolver.CompositeName;
import net.sourceforge.pmd.lang.modelica.resolver.InternalApiBridge;
import net.sourceforge.pmd.lang.modelica.resolver.ModelicaDeclaration;
import net.sourceforge.pmd.lang.modelica.resolver.ModelicaScope;
import net.sourceforge.pmd.lang.modelica.resolver.ResolutionResult;
import net.sourceforge.pmd.lang.modelica.resolver.internal.ResolutionContext;
import net.sourceforge.pmd.lang.modelica.resolver.internal.ResolutionState;
import net.sourceforge.pmd.lang.modelica.resolver.internal.Watchdog;

public final class ASTUnqualifiedImportClause extends AbstractModelicaImportClause {
    private ASTName importFromWhere;

    ASTUnqualifiedImportClause(int id) {
        super(id);
    }

    @Override
    protected <P, R> R acceptModelicaVisitor(ModelicaVisitor<? super P, ? extends R> visitor, P data) {
        return visitor.visit(this, data);
    }

    @Override
    public void jjtClose() {
        super.jjtClose();

        importFromWhere = firstChild(ASTName.class);
    }

    @Override
    protected ResolutionResult<ModelicaDeclaration> getCacheableImportSources(ResolutionState state, ModelicaScope scope) {
        return scope.safeResolveLexically(ModelicaDeclaration.class, state, importFromWhere.getCompositeName());
    }

    @Override
    protected void fetchImportedClassesFromSource(ResolutionContext result, ModelicaDeclaration source, String simpleName) throws Watchdog.CountdownException {
        result.watchdogTick();
        InternalApiBridge.resolveFurtherNameComponents(source, result, CompositeName.create(simpleName));
    }

    @Override
    public boolean isQualified() {
        return false;
    }
}
