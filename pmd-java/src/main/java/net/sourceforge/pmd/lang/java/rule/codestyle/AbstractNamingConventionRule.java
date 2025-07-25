/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.codestyle;

import java.util.regex.Pattern;

import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.properties.PropertyBuilder.RegexPropertyBuilder;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.util.StringUtil.CaseConvention;


/**
 * Base class for naming conventions rule. Not public API, but
 * used to uniformize eg property names between our rules.
 *
 * <p>Protected methods may leak API because concrete classes
 * are not final so they're package private instead
 *
 * @author Clément Fournier
 * @since 6.5.0
 */
abstract class AbstractNamingConventionRule<T extends JavaNode> extends AbstractJavaRulechainRule {

    static final String CAMEL_CASE = "[a-z][a-zA-Z0-9]*";
    static final String PASCAL_CASE = "[A-Z][a-zA-Z0-9]*";

    @SafeVarargs
    protected AbstractNamingConventionRule(Class<? extends JavaNode> first, Class<? extends JavaNode>... visits) {
        super(first, visits);
    }

    /** The argument is interpreted as the display name, and is converted to camel case to get the property name. */
    RegexPropertyBuilder defaultProp(String displayName) {
        return defaultProp(CaseConvention.SPACE_SEPARATED.convertTo(CaseConvention.CAMEL_CASE, displayName), displayName);
    }

    /** Returns a pre-filled builder with the given name and display name (for the description). */
    RegexPropertyBuilder defaultProp(String name, String displayName) {
        return PropertyFactory.regexProperty(name + "Pattern")
                              .desc("Regex which applies to " + displayName.trim() + " names")
                              .defaultValue(defaultConvention());
    }

    /** Default regex string for this kind of entities. */
    abstract String defaultConvention();


    /** Generic "kind" of node, eg "static method" or "utility class". */
    abstract String kindDisplayName(T node, PropertyDescriptor<Pattern> descriptor);

    /** Extracts the name that should be pattern matched. */
    abstract String nameExtractor(T node);


    void checkMatches(T node, PropertyDescriptor<Pattern> regex, Object data) {
        String name = nameExtractor(node);
        if (!getProperty(regex).matcher(name).matches()) {
            asCtx(data).addViolation(node, kindDisplayName(node, regex),
                                     name,
                                     getProperty(regex).toString());
        }
    }

}
