/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTArrayDimsAndInits.java */

package net.sourceforge.pmd.lang.java.ast;

/**
 * Represents array dimensions in {@linkplain ASTArrayAllocation array allocation expressions}.
 * Those are kept separate from {@linkplain ASTArrayTypeDims ArrayTypeDims}
 * because here, some dimensions may be initialized with an expression.
 *
 * <pre class="grammar">
 *
 * ArrayAllocationDims ::= ({@link ASTArrayDimExpr ArrayDimExpr})+ ({@link ASTArrayTypeDim})*
 *                       | ({@link ASTArrayTypeDim})+
 *
 * </pre>
 */
public class ASTArrayAllocationDims extends AbstractJavaNode {

    public ASTArrayAllocationDims(int id) {
        super(id);
    }

    public ASTArrayAllocationDims(JavaParser p, int id) {
        super(p, id);
    }

    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    public int getArrayDepth() {
        return jjtGetNumChildren();
    }

}
