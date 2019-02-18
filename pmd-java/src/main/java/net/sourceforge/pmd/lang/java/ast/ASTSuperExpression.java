/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTNullLiteral.java */

package net.sourceforge.pmd.lang.java.ast;

import java.util.Optional;


/**
 * The "super" reference. Technically not an expression but it's easier to analyse that way.
 *
 * <pre>
 *
 * SuperExpression ::= "super"
 *  *                | {@link ASTClassOrInterfaceType TypeName} "." "super"
 *
 * </pre>
 */
public final class ASTSuperExpression extends AbstractLateInitNode implements ASTPrimaryExpression {
    ASTSuperExpression(int id) {
        super(id);
    }


    ASTSuperExpression(JavaParser p, int id) {
        super(p, id);
    }


    public Optional<ASTClassOrInterfaceType> getQualifier() {
        return jjtGetNumChildren() > 0 ? Optional.of((ASTClassOrInterfaceType) jjtGetChild(0)) : Optional.empty();
    }

    /**
     * Accept the visitor. *
     */
    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    void onInjectFinished() {
        // If this method is called, then a qualifier was injected
        ASTAmbiguousName name = (ASTAmbiguousName) jjtGetChild(0);
        this.replaceChildAt(0, name.forceTypeContext());
    }
}
