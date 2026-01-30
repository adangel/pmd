/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ecmascript.ast;

import org.mozilla.javascript.ast.FunctionCall;

abstract class AbstractFunctionCallNode<T extends FunctionCall> extends AbstractEcmascriptNode<T> {

    AbstractFunctionCallNode(T node) {
        super(node);
    }

    EcmascriptNode<?> getTarget() {
        return (EcmascriptNode<?>) getChild(0);
    }

    int getNumArguments() {
        return node.getArguments().size();
    }

    EcmascriptNode<?> getArgument(int index) {
        return (EcmascriptNode<?>) getChild(index + 1);
    }

    boolean hasArguments() {
        return getNumArguments() != 0;
    }

}
