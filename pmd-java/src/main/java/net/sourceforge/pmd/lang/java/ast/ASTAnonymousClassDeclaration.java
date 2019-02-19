/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTAllocationExpression.java */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.lang.java.qname.JavaTypeQualifiedName;


/**
 * An anonymous class declaration. This can occur in a {@link ASTConstructorCall class instance creation expression}
 * or in an {@link ASTEnumConstant enum constant declaration}.
 *
 *
 * <pre>
 *
 * AnonymousClassDeclaration ::= {@link ASTClassOrInterfaceBody}
 *
 * </pre>
 */
public final class ASTAnonymousClassDeclaration extends AbstractJavaTypeNode implements JavaQualifiableNode {

    private JavaTypeQualifiedName qualifiedName;


    ASTAnonymousClassDeclaration(int id) {
        super(id);
    }


    ASTAnonymousClassDeclaration(JavaParser p, int id) {
        super(p, id);
    }


    @Override
    public boolean isFindBoundary() {
        return true;
    }


    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    public ASTClassOrInterfaceBody getBody() {
        return (ASTClassOrInterfaceBody) jjtGetChild(0);
    }


    /**
     * Gets the qualified name of the anonymous class
     * declared by this node.
     */
    @Override
    public JavaTypeQualifiedName getQualifiedName() {
        return qualifiedName;
    }


    public void setQualifiedName(JavaTypeQualifiedName qname) {
        this.qualifiedName = qname;
    }

}
