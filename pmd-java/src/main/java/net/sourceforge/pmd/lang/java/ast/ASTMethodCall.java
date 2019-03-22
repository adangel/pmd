/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTNullLiteral.java */

package net.sourceforge.pmd.lang.java.ast;

import javax.annotation.Nullable;


/**
 * A method invocation expression. This node represents both qualified (with a left-hand side)
 * and unqualified invocation expressions.
 *
 * <pre class="grammar">
 *
 * TODO the third branch is ambiguous with the second and won't be matched without a rewrite phase
 *
 * MethodCall ::=  &lt;IDENTIFIER&gt; {@link ASTArgumentList ArgumentList}
 *              |  Lhs "." {@link ASTTypeArguments TypeArguments}? &lt;IDENTIFIER&gt; {@link ASTArgumentList ArgumentList}
 *
 * Lhs        ::= {@link ASTPrimaryExpression PrimaryExpression} | {@link ASTClassOrInterfaceType ClassName} | {@link ASTAmbiguousName AmbiguousName}
 * </pre>
 */
public final class ASTMethodCall extends AbstractLateInitNode implements ASTPrimaryExpression, ASTQualifiableExpression {



    ASTMethodCall(int id) {
        super(id);
    }


    ASTMethodCall(JavaParser p, int id) {
        super(p, id);
    }


    /**
     * Returns the name of the called method.
     */
    public String getMethodName() {
        return getImage();
    }

    /**
     * Gets the type name preceding the ".", if any. May return empty if
     * this call is not qualified (no "."), or if the qualifier is a type
     * instead of an expression.
     *
     * <p>If the LHS is an {@link ASTAmbiguousName}, returns it.
     */
    @Nullable
    public ASTClassOrInterfaceType getLhsType() {
        return getChildAs(0, ASTClassOrInterfaceType.class);
    }


    public ASTArgumentList getArguments() {
        return (ASTArgumentList) jjtGetChild(jjtGetNumChildren() - 1);
    }


    @Nullable
    public ASTTypeArguments getExplicitTypeArguments() {
        return getFirstChildOfType(ASTTypeArguments.class);
    }


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
        if (getImage() != null) {
            return;
        }

        // the cast serves as an assert
        ASTAmbiguousName fstChild = (ASTAmbiguousName) jjtGetChild(0);

        fstChild.shrinkOrDeleteInParentSetImage();

        assert getImage() != null;
    }

}
