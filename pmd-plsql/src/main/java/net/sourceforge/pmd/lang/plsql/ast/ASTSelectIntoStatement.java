/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.plsql.ast;

public final class ASTSelectIntoStatement extends AbstractSelectStatement {

    ASTSelectIntoStatement(int id) {
        super(id);
    }

    @Override
    protected <P, R> R acceptPlsqlVisitor(PlsqlVisitor<? super P, ? extends R> visitor, P data) {
        return visitor.visit(this, data);
    }

    @Override
    public boolean isDistinct() {
        return super.isDistinct();
    }

    @Override
    public boolean isUnique() {
        return super.isUnique();
    }

    @Override
    public boolean isAll() {
        return super.isAll();
    }
}
