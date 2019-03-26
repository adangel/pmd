/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTNullLiteral.java */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Represents an array dimension in an {@linkplain ASTArrayType array type},
 * or in an {@linkplain ASTArrayAllocationDims array allocation expression}.
 *
 * <pre class="grammar">
 *
 * ArrayTypeDim ::= {@link ASTAnnotation TypeAnnotation}* "[" "]"
 *
 * </pre>
 *
 */
public class ASTArrayTypeDim extends AbstractJavaTypeNode {
    public ASTArrayTypeDim(int id) {
        super(id);
    }

    public ASTArrayTypeDim(JavaParser p, int id) {
        super(p, id);
    }

    /**
     * Accept the visitor. *
     */
    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
