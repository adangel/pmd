/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.xpath.saxon;

import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.lang.ast.Node;

import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.iter.ArrayIterator;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.AxisIteratorOverSequence;
import net.sf.saxon.tree.iter.EmptyAxisIterator;
import net.sf.saxon.tree.iter.SingleNodeIterator;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.Type;

/**
 * A Saxon OM Element type node for an AST Node.
 */
@Deprecated
@InternalApi
public class ElementNode extends AbstractNodeInfo {

    protected final DocumentNode document;
    protected final ElementNode parent;
    protected final Node node;
    protected final int id;
    protected final int siblingPosition;
    protected final NodeInfo[] children;

    public ElementNode(DocumentNode document, IdGenerator idGenerator, ElementNode parent, Node node,
            int siblingPosition) {
        this.document = document;
        this.parent = parent;
        this.node = node;
        this.id = idGenerator.getNextId();
        this.siblingPosition = siblingPosition;
        if (node.getNumChildren() > 0) {
            this.children = new NodeInfo[node.getNumChildren()];
            for (int i = 0; i < children.length; i++) {
                children[i] = new ElementNode(document, idGenerator, this, node.getChild(i), i);
            }
        } else {
            this.children = null;
        }
        document.nodeToElementNode.put(node, this);
    }

    @Override
    public Object getUnderlyingNode() {
        return node;
    }

    @Override
    public int getSiblingPosition() {
        return siblingPosition;
    }

    @Override
    public int getColumnNumber() {
        return node.getBeginColumn();
    }

    @Override
    public int getLineNumber() {
        return node.getBeginLine();
    }

    @Override
    public boolean hasChildNodes() {
        return children != null;
    }

    @Override
    public int getNodeKind() {
        return Type.ELEMENT;
    }

    @Override
    public DocumentInfo getDocumentRoot() {
        return document;
    }

    @Override
    public String getLocalPart() {
        return node.getXPathNodeName();
    }

    @Override
    public String getURI() {
        return "";
    }

    @Override
    public NodeInfo getParent() {
        return parent;
    }

    @Override
    public int compareOrder(NodeInfo other) {
        int result;
        if (this.isSameNodeInfo(other)) {
            result = 0;
        } else {
            result = Integer.signum(this.getLineNumber() - other.getLineNumber());
            if (result == 0) {
                result = Integer.signum(this.getColumnNumber() - other.getColumnNumber());
            }
            if (result == 0) {
                if (this.getParent().equals(other.getParent())) {
                    result = Integer.signum(this.getSiblingPosition() - ((ElementNode) other).getSiblingPosition());
                } else {
                    // we must not return 0 here, otherwise the node might be removed as duplicate when creating
                    // a union set. The the nodes are definitively different nodes (isSameNodeInfo == false).
                    result = 1;
                }
            }
        }
        return result;
    }

    @SuppressWarnings("PMD.MissingBreakInSwitch")
    @Override
    public AxisIterator iterateAxis(byte axisNumber) {
        switch (axisNumber) {
        case AxisInfo.ANCESTOR:
            return new Navigator.AncestorEnumeration(this, false);
        case AxisInfo.ANCESTOR_OR_SELF:
            return new Navigator.AncestorEnumeration(this, true);
        case AxisInfo.ATTRIBUTE:
            return new AttributeAxisIterator(this);
        case AxisInfo.CHILD:
            if (children == null) {
                return EmptyAxisIterator.emptyAxisIterator();
            } else {
                return new AxisIteratorOverSequence<>(new ArrayIterator(children));
            }
        case AxisInfo.DESCENDANT:
            return new Navigator.DescendantEnumeration(this, false, true);
        case AxisInfo.DESCENDANT_OR_SELF:
            return new Navigator.DescendantEnumeration(this, true, true);
        case AxisInfo.FOLLOWING:
            return new Navigator.FollowingEnumeration(this);
        case AxisInfo.FOLLOWING_SIBLING:
            if (parent == null || siblingPosition == parent.children.length - 1) {
                return EmptyAxisIterator.emptyAxisIterator();
            } else {
                return new AxisIteratorOverSequence<>(new ArrayIterator(parent.children, siblingPosition + 1, parent.children.length));
            }
        case AxisInfo.NAMESPACE:
            return super.iterateAxis(axisNumber);
        case AxisInfo.PARENT:
            return SingleNodeIterator.makeIterator(parent);
        case AxisInfo.PRECEDING:
            return new Navigator.PrecedingEnumeration(this, false);
        case AxisInfo.PRECEDING_SIBLING:
            if (parent == null || siblingPosition == 0) {
                return EmptyAxisIterator.emptyAxisIterator();
            } else {
                return new AxisIteratorOverSequence<>(new ArrayIterator(parent.children, 0, siblingPosition));
            }
        case AxisInfo.SELF:
            return SingleNodeIterator.makeIterator(this);
        case AxisInfo.PRECEDING_OR_ANCESTOR:
            return new Navigator.PrecedingEnumeration(this, true);
        default:
            return super.iterateAxis(axisNumber);
        }
    }

}
