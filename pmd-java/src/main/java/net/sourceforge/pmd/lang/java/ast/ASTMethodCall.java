/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTNullLiteral.java */

package net.sourceforge.pmd.lang.java.ast;

import java.util.Optional;


/**
 * A method invocation expression. This node represents both qualified (with a left-hand side)
 * and unqualified invocation expressions.
 *
 * <pre>
 *
 * TODO the third branch is ambiguous with the second and won't be matched without a rewrite phase
 *
 * MethodCall ::=  &lt;IDENTIFIER&gt; {@link ASTArgumentList ArgumentList}
 *              |  {@link ASTPrimaryExpression PrimaryExpression} "." {@link ASTTypeArguments TypeArguments}? &lt;IDENTIFIER&gt; {@link ASTArgumentList ArgumentList}
 *              |  {@link ASTClassOrInterfaceType ClassName} "." {@link ASTTypeArguments TypeArguments}? &lt;IDENTIFIER&gt; {@link ASTArgumentList ArgumentList}
 * </pre>
 */
public final class ASTMethodCall extends AbstractLateInitNode implements ASTPrimaryExpression {



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
     * Gets the expression preceding the ".", if any. May return empty if
     * this call is not qualified (no "."), or if the qualifier is a type
     * instead of an expression.
     *
     * <p>If the LHS is an {@link ASTAmbiguousName}, returns it.
     */
    public Optional<ASTPrimaryExpression> getLhsExpression() {
        // note: getNameNode must be called before jjtGetChild here because it changes the first child
        return Optional.of(jjtGetChild(0))
                       .filter(it -> it instanceof ASTPrimaryExpression)
                       .map(it -> (ASTPrimaryExpression) it);
    }


    /**
     * Gets the type name preceding the ".", if any. May return empty if
     * this call is not qualified (no "."), or if the qualifier is a type
     * instead of an expression.
     *
     * <p>If the LHS is an {@link ASTAmbiguousName}, returns it.
     */
    public Optional<ASTClassOrInterfaceType> getLhsType() {
        // note: getNameNode must be called before jjtGetChild here because it changes the first child
        return Optional.of(jjtGetChild(0))
                       .filter(it -> it instanceof ASTClassOrInterfaceType)
                       .map(it -> (ASTClassOrInterfaceType) it);
    }


    public ASTArgumentList getArguments() {
        return (ASTArgumentList) jjtGetChild(jjtGetNumChildren() - 1);
    }


    public Optional<ASTTypeArguments> getExplicitTypeArguments() {
        return Optional.ofNullable(getFirstChildOfType(ASTTypeArguments.class));
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
