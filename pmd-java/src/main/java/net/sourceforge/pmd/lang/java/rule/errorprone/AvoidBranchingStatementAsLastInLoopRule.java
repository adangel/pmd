/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTBreakStatement;
import net.sourceforge.pmd.lang.java.ast.ASTContinueStatement;
import net.sourceforge.pmd.lang.java.ast.ASTDoStatement;
import net.sourceforge.pmd.lang.java.ast.ASTFinallyClause;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTForeachStatement;
import net.sourceforge.pmd.lang.java.ast.ASTReturnStatement;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchStatement;
import net.sourceforge.pmd.lang.java.ast.ASTWhileStatement;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

public class AvoidBranchingStatementAsLastInLoopRule extends AbstractJavaRulechainRule {

    public static final String CHECK_FOR = "for";
    public static final String CHECK_DO = "do";
    public static final String CHECK_WHILE = "while";

    private static final Map<String, String> LOOP_TYPES_MAPPINGS;
    private static final List<String> DEFAULTS = Arrays.asList(CHECK_FOR, CHECK_DO, CHECK_WHILE);

    static {
        Map<String, String> mappings = new HashMap<>();
        mappings.put(CHECK_FOR, CHECK_FOR);
        mappings.put(CHECK_DO, CHECK_DO);
        mappings.put(CHECK_WHILE, CHECK_WHILE);
        LOOP_TYPES_MAPPINGS = Collections.unmodifiableMap(mappings);
    }

    // TODO I don't think we need this configurability.
    // I think we should tone that down to just be able to ignore some type of statement,
    // but I can't see a use case to e.g. report only breaks in 'for' loops but not in 'while'.

    public static final PropertyDescriptor<List<String>> CHECK_BREAK_LOOP_TYPES = propertyFor("break");
    public static final PropertyDescriptor<List<String>> CHECK_CONTINUE_LOOP_TYPES = propertyFor("continue");
    public static final PropertyDescriptor<List<String>> CHECK_RETURN_LOOP_TYPES = propertyFor("return");



    public AvoidBranchingStatementAsLastInLoopRule() {
        super(ASTBreakStatement.class, ASTContinueStatement.class, ASTReturnStatement.class);
        definePropertyDescriptor(CHECK_BREAK_LOOP_TYPES);
        definePropertyDescriptor(CHECK_CONTINUE_LOOP_TYPES);
        definePropertyDescriptor(CHECK_RETURN_LOOP_TYPES);
    }


    @Override
    public Object visit(ASTBreakStatement node, Object data) {
        // skip breaks, that are within a switch statement
        if (node.ancestors().get(1) instanceof ASTSwitchStatement) {
            return data;
        }
        return check(CHECK_BREAK_LOOP_TYPES, node, data);
    }


    protected Object check(PropertyDescriptor<List<String>> property, Node node, Object data) {
        Node parent = node.getParent();
        if (parent instanceof ASTBlock) {
            parent = parent.getParent();
            if (parent instanceof ASTFinallyClause) {
                // get the parent of the block, in which the try statement is: ForStatement/Block/TryStatement/Finally
                // e.g. a ForStatement
                parent = ((ASTFinallyClause) parent).ancestors().get(2);
            }
        }
        if (parent instanceof ASTForStatement || parent instanceof ASTForeachStatement) {
            if (hasPropertyValue(property, CHECK_FOR)) {
                asCtx(data).addViolation(node);
            }
        } else if (parent instanceof ASTWhileStatement) {
            if (hasPropertyValue(property, CHECK_WHILE)) {
                asCtx(data).addViolation(node);
            }
        } else if (parent instanceof ASTDoStatement) {
            if (hasPropertyValue(property, CHECK_DO)) {
                asCtx(data).addViolation(node);
            }
        }
        return data;
    }


    protected boolean hasPropertyValue(PropertyDescriptor<List<String>> property, String value) {
        return getProperty(property).contains(value);
    }


    @Override
    public Object visit(ASTContinueStatement node, Object data) {
        return check(CHECK_CONTINUE_LOOP_TYPES, node, data);
    }


    @Override
    public Object visit(ASTReturnStatement node, Object data) {
        return check(CHECK_RETURN_LOOP_TYPES, node, data);
    }


    @Override
    public String dysfunctionReason() {
        return checksNothing() ? "All loop types are ignored" : null;
    }

    private static PropertyDescriptor<List<String>> propertyFor(String stmtName) {
        return PropertyFactory.enumListProperty("check" + StringUtils.capitalize(stmtName) + "LoopTypes", LOOP_TYPES_MAPPINGS)
                .desc("List of loop types in which " + stmtName + " statements will be checked")
                .defaultValue(DEFAULTS)
                .build();
    }

    public boolean checksNothing() {

        return getProperty(CHECK_BREAK_LOOP_TYPES).isEmpty() && getProperty(CHECK_CONTINUE_LOOP_TYPES).isEmpty()
            && getProperty(CHECK_RETURN_LOOP_TYPES).isEmpty();
    }
}
