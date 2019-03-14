/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.apex.rule.bestpractices;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.pmd.lang.apex.ast.ASTMethod;
import net.sourceforge.pmd.lang.apex.ast.ASTParameter;
import net.sourceforge.pmd.lang.apex.ast.ASTVariableExpression;
import net.sourceforge.pmd.lang.apex.rule.AbstractApexRule;

public class UnusedFormalParameterRule extends AbstractApexRule {

    public UnusedFormalParameterRule() {
        addRuleChainVisit(ASTMethod.class);
    }

    @Override
    public Object visit(ASTMethod node, Object data) {
        List<ASTVariableExpression> usages = node.findDescendantsOfType(ASTVariableExpression.class);
        Set<String> referencedVariables = new HashSet<>();
        for (ASTVariableExpression variableExpression : usages) {
            referencedVariables.add(variableExpression.getNode().getIdentifier().getValue());
        }

        List<ASTParameter> parameters = node.findChildrenOfType(ASTParameter.class);
        for (ASTParameter parameter : parameters) {
            if (!referencedVariables.contains(parameter.getImage())) {
                addViolation(data, parameter, parameter.getImage());
            }
        }
        return data;
    }
}
