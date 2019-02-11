/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.apex.ast;

import java.lang.reflect.Field;

import apex.jorje.semantic.ast.statement.FieldDeclaration;

public class ASTFieldDeclaration extends AbstractApexNode<FieldDeclaration> {

    public ASTFieldDeclaration(FieldDeclaration fieldDeclaration) {
        super(fieldDeclaration);
    }

    @Override
    public Object jjtAccept(ApexParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public String getImage() {
        try {
            Field nameField = FieldDeclaration.class.getDeclaredField("name");
            nameField.setAccessible(true);
            return String.valueOf(nameField.get(node));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return "";
    }
}
