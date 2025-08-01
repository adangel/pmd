/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.lang.document.Chars;

/**
 * The null literal.
 *
 * <pre class="grammar">
 *
 * NullLiteral ::= "null"
 *
 * </pre>
 */
public final class ASTNullLiteral extends AbstractLiteral implements ASTLiteral {
    ASTNullLiteral(int id) {
        super(id);
    }

    @Override
    public boolean isCompileTimeConstant() {
        return false;
    }

    @Override
    public <P, R> R acceptVisitor(JavaVisitor<? super P, ? extends R> visitor, P data) {
        return visitor.visit(this, data);
    }

    @Override
    public Chars getLiteralText() {
        return super.getLiteralText();
    }
}
