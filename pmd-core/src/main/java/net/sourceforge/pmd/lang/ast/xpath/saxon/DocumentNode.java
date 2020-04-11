/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.xpath.saxon;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.lang.ast.Node;

import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.SingleNodeIterator;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.Type;

/**
 * A Saxon OM Document node for an AST Node.
 */
@Deprecated
@InternalApi
public class DocumentNode extends AbstractNodeInfo implements DocumentInfo {

    /**
     * The root ElementNode of the DocumentNode.
     */
    protected final ElementNode rootNode;

    /**
     * Mapping from AST Node to corresponding ElementNode.
     */
    public final Map<Node, ElementNode> nodeToElementNode = new HashMap<>();

    /**
     * Construct a DocumentNode, with the given AST Node serving as the root
     * ElementNode.
     *
     * @param node
     *            The root AST Node.
     *
     * @see ElementNode
     */
    public DocumentNode(Node node) {
        this.rootNode = new ElementNode(this, new IdGenerator(), null, node, -1);
    }

    @Override
    public String[] getUnparsedEntity(String name) {
        throw createUnsupportedOperationException("DocumentInfo.getUnparsedEntity(String)");
    }

    @Override
    public Iterator getUnparsedEntityNames() {
        throw createUnsupportedOperationException("DocumentInfo.getUnparsedEntityNames()");
    }

    @Override
    public NodeInfo selectID(String id, boolean getParent) {
        throw createUnsupportedOperationException("DocumentInfo.selectID(String,boolean)");
    }

    @Override
    public int getNodeKind() {
        return Type.DOCUMENT;
    }

    @Override
    public DocumentInfo getDocumentRoot() {
        return this;
    }

    @Override
    public boolean hasChildNodes() {
        return true;
    }

    @Override
    public boolean isTyped() {
        return false;
    }

    @Override
    public Object getUserData(String key) {
        throw createUnsupportedOperationException("DocumentInfo.getUserData(String)");
    }

    @Override
    public void setUserData(String key, Object value) {
        throw createUnsupportedOperationException("DocumentInfo.setUserData(String,Object)");
    }

    @Override
    public AxisIterator iterateAxis(byte axisNumber) {
        switch (axisNumber) {
        case AxisInfo.DESCENDANT:
            return new Navigator.DescendantEnumeration(this, false, true);
        case AxisInfo.DESCENDANT_OR_SELF:
            return new Navigator.DescendantEnumeration(this, true, true);
        case AxisInfo.CHILD:
            return SingleNodeIterator.makeIterator(rootNode);
        default:
            return super.iterateAxis(axisNumber);
        }
    }
}
