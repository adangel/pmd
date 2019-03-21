/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTPrimitiveType.java */

package net.sourceforge.pmd.lang.java.ast;

import java.util.List;
import javax.annotation.Nullable;


/**
 * Represents an annotated type.
 *
 * <pre>
 *
 * AnnotatedType ::= ({@link ASTAnnotation TypeAnnotation})+ {@link ASTType Type}
 *
 * </pre>
 */
public class ASTAnnotatedType extends AbstractJavaTypeNode implements ASTType {

    ASTAnnotatedType(int id) {
        super(id);
    }


    ASTAnnotatedType(JavaParser p, int id) {
        super(p, id);
    }


    @Override
    public String getTypeImage() {
        return getBaseType().getImage();
    }

    /**
     * Returns the annotated type.
     */
    public ASTType getBaseType() {
        return (ASTType) jjtGetChild(jjtGetNumChildren() - 1);
    }

    @Override
    public boolean isPrimitiveType() {
        return getBaseType().isPrimitiveType();
    }

    @Nullable
    @Override
    public ASTPrimitiveType asPrimitiveType() {
        return getBaseType().asPrimitiveType();
    }

    @Nullable
    @Override
    public ASTReferenceType asReferenceType() {
        return getBaseType().asReferenceType();
    }

    /**
     * Returns the annotations contained within this node.
     */
    @Override
    public List<ASTAnnotation> getDeclaredAnnotations() {
        return findChildrenOfType(ASTAnnotation.class);
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