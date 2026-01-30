/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ecmascript.ast;

import org.mozilla.javascript.ast.FunctionCall;

public final class ASTFunctionCall extends AbstractFunctionCallNode<FunctionCall> {
    ASTFunctionCall(FunctionCall functionCall) {
        super(functionCall);
    }

    @Override
    protected <P, R> R acceptJsVisitor(EcmascriptVisitor<? super P, ? extends R> visitor, P data) {
        return visitor.visit(this, data);
    }

    @Override
    public EcmascriptNode<?> getTarget() {
        return super.getTarget();
    }

    @Override
    public int getNumArguments() {
        return super.getNumArguments();
    }

    @Override
    public EcmascriptNode<?> getArgument(int index) {
        return super.getArgument(index);
    }

    @Override
    public boolean hasArguments() {
        return super.hasArguments();
    }
}
