/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.swift.ast;

import java.util.ArrayDeque;
import java.util.Deque;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;

import net.sourceforge.pmd.lang.ast.AstVisitor;
import net.sourceforge.pmd.lang.ast.AstVisitorBase;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.impl.AbstractNodeWithTextCoordinates;
import net.sourceforge.pmd.lang.swift.ast.SwiftParser.SwTopLevel;

/**
 *
 */
public class SwiftParserTests extends BaseSwiftTreeDumpTest {

    @Test
    public void testSimpleSwift() {
        doTest("Simple");
    }

    @Test
    public void testBtree() {
        doTest("BTree");
    }
    
    @Test
    public void anotherTest() {
        @NonNull
        SwiftParsingHelper parser = getParser();
        SwTopLevel root = parser.parseResource("/Simple.swift");
        System.out.println(root);
        
        SwiftTreeBuilder treeBuilder = new SwiftTreeBuilder();
        AbstractSwiftNode<SwTopLevel> astRoot = treeBuilder.build(root);
        
        AstVisitor<Void, Void> visitor = new AstVisitorBase<Void, Void>() {
            private String prefix = "";
            @Override
            public Void visitNode(Node node, Void param) {
                prefix += "  ";
                System.out.println("new tree: " + prefix + node.getXPathNodeName());
                super.visitNode(node, param);
                prefix = prefix.substring(2);
                return null;
            }
        };
        astRoot.acceptVisitor(visitor, null);
        
    }

    private static class SwiftTreeBuilder extends SwiftVisitorBase<Void, Void> {
        private Deque<AbstractSwiftNode> swNodes = new ArrayDeque<>();
        private Deque<SwiftNode> antlrNodes = new ArrayDeque<>();
        
        private String prefix = "";
        
        <T extends SwiftNode> AbstractSwiftNode<T> build(T astNode) {
            
            AbstractSwiftNode<T> swNode = (AbstractSwiftNode<T>) createNodeAdapter(astNode);

            AbstractSwiftNode parent = getCurrentParentOrNull();
            if (parent != null) {
                parent.addChild(swNode, parent.getNumChildren());
            }
            
            swNodes.push(swNode);
            antlrNodes.push(astNode);
            astNode.acceptVisitor(this, null);
            antlrNodes.pop();
            swNodes.pop();
            
            return swNode;
        }
        
        private AbstractSwiftNode createNodeAdapter(SwiftNode astNode) {
            return new ASTSwNode(astNode);
        }
        
        private AbstractSwiftNode getCurrentParentOrNull() {
            return swNodes.isEmpty() ? null : swNodes.peek();
        }

        @Override
        public Void visitNode(Node node, Void param) {
            for (Node child : node.children()) {
                if (child instanceof SwiftTerminalNode) {
                    AbstractSwiftNode parent = getCurrentParentOrNull();
                    if (parent != null) {
                        parent.addData(((SwiftTerminalNode) child).getText());
                    }
                } else if (child instanceof SwiftNode) {
                    build((SwiftNode) child);
                } else {
                    throw new IllegalStateException("Got a child of type " + child.getClass());
                }
            }
            return null;
        }
        
        @Override
        public Void visitSwiftNode(SwiftNode node, Void param) {
            prefix += "  ";
            System.out.println(prefix + node.getClass().getSimpleName());
            super.visitSwiftNode(node, param);
            prefix = prefix.substring(2);
            return null;
        }
    }
    
    private abstract static class AbstractSwiftNode<T extends SwiftNode> extends AbstractNodeWithTextCoordinates<AbstractSwiftNode<?>, SwiftNode> implements SwiftNode {
        protected final T node;
        private String data = "";

        AbstractSwiftNode(T node) {
            this.node = node;
        }

        // overridden to make it visible in this package
        @Override
        protected void addChild(AbstractSwiftNode<?> child, int index) {
            super.addChild(child, index);
        }

        @Override
        public String getXPathNodeName() {
            return node.getXPathNodeName() + "[data=" + data + "]";
        }

        public void addData(String data) {
            this.data += data;
        }

        public String getData() {
            return data;
        }
    }
    
    private static class ASTSwNode extends AbstractSwiftNode<SwiftNode> {

        ASTSwNode(SwiftNode node) {
            super(node);
        }

        @Override
        public <P, R> R acceptVisitor(AstVisitor<? super P, ? extends R> visitor, P data) {
            return visitor.visitNode(this, data);
        }
    }
}
