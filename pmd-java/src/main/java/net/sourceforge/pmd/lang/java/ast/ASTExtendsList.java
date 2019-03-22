/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTExtendsList.java */

package net.sourceforge.pmd.lang.java.ast;

import java.util.Iterator;


/**
 * Represents the {@code extends} clause of a class or interface declaration.
 * If the parent is an interface declaration, then these types are all interface
 * types. Otherwise, then this list contains exactly one element.
 *
 * <pre class="grammar">
 *
 * ExtendsList ::= "extends" {@link ASTType Type} ( "," {@link ASTType Type} )*
 * </pre>
 */
public class ASTExtendsList extends AbstractJavaNode implements Iterable<ASTClassOrInterfaceType> {
    public ASTExtendsList(int id) {
        super(id);
    }

    public ASTExtendsList(JavaParser p, int id) {
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


    @Override    // TODO this doesn't preserve the annotations.
    public Iterator<ASTClassOrInterfaceType> iterator() {
        return new NodeChildrenIterator<>(this, ASTClassOrInterfaceType.class);
    }
}
