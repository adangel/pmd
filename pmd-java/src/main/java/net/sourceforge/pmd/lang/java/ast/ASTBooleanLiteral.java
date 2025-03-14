/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.document.Chars;

/**
 * The boolean literal, either "true" or "false".
 */
public final class ASTBooleanLiteral extends AbstractLiteral implements ASTLiteral {

    private boolean isTrue;


    ASTBooleanLiteral(int id) {
        super(id);
    }


    void setTrue() {
        isTrue = true;
    }

    public boolean isTrue() {
        return this.isTrue;
    }

    @Override
    public @Nullable Boolean getConstValue() {
        return (Boolean) super.getConstValue();
    }

    @Override
    protected @NonNull ConstResult buildConstValue() {
        return isTrue ? ConstResult.BOOL_TRUE : ConstResult.BOOL_FALSE;
    }

    @Override
    public Chars getLiteralText() {
        return super.getLiteralText();
    }

    @Override
    protected <P, R> R acceptVisitor(JavaVisitor<? super P, ? extends R> visitor, P data) {
        return visitor.visit(this, data);
    }
}
