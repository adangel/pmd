/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTNullLiteral.java */

package net.sourceforge.pmd.lang.java.ast;

/**
 * A reference to an unqualified variable. {@linkplain ASTAmbiguousName Ambiguous names} are promoted
 * to this status in the syntactic contexts, where we know they're definitely variable references.
 * This node represents both references to fields and to variables (for now?).
 *
 * <pre>
 *
 * VariableReference ::= &lt;IDENTIFIER&gt;
 *
 * </pre>
 */
public final class ASTVariableReference extends AbstractJavaTypeNode implements ASTPrimaryExpression {

    /**
     * Constructor promoting an ambiguous name to a variable reference.
     */
    ASTVariableReference(ASTAmbiguousName name) {
        super(JavaParserTreeConstants.JJTVARIABLEREFERENCE);
        setImage(name.getImage());
        copyTextCoordinates(name);
    }


    ASTVariableReference(int id) {
        super(id);
    }


    ASTVariableReference(JavaParser p, int id) {
        super(p, id);
    }


    /**
     * Gets the name of the referenced variable.
     */
    public String getVariableName() {
        return getImage();
    }

    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }
}