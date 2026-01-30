/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ecmascript.ast;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.InfixExpression;

abstract class AbstractInfixEcmascriptNode<T extends InfixExpression> extends AbstractEcmascriptNode<T> {

    AbstractInfixEcmascriptNode(T infixExpression) {
        super(infixExpression);
    }

    String getOperator() {
        int operator = node.getOperator();
        if (operator == Token.ASSIGN_BITXOR) {
            return "^=";
        } else if (operator != Token.METHOD) {
            return AstRoot.operatorToString(operator);
        }
        return "";
    }

    EcmascriptNode<?> getLeft() {
        return getChild(0);
    }

    EcmascriptNode<?> getRight() {
        return getChild(1);
    }
}
