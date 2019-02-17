/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTLiteral.java */

package net.sourceforge.pmd.lang.java.ast;

import org.apache.commons.lang3.StringEscapeUtils;


/**
 * Represents a string literal. The image of this node is the literal as it appeared
 * in the source. {@link #getEscapedValue()} allows to recover the actual runtime value.
 */
public final class ASTStringLiteral extends AbstractJavaTypeNode implements ASTLiteral {


    public ASTStringLiteral(int id) {
        super(id);
    }


    public ASTStringLiteral(JavaParser p, int id) {
        super(p, id);
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


    /**
     * Returns the value without delimiters and unescaped.
     */
    public String getEscapedValue() {
        return StringEscapeUtils.unescapeJava(getImage());
    }

}
