/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.xpath;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.jaxen.Navigator;

import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.XPathHandler;
import net.sourceforge.pmd.lang.rule.xpath.SaxonXPathRuleQuery;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

@Deprecated
@InternalApi
public abstract class AbstractASTXPathHandler implements XPathHandler {


    @Override
    public Navigator getNavigator() {
        return new DocumentNavigator();
    }


    public void initialize(IndependentContext context, Language language, Class<?> functionsClass) {
        context.declareNamespace("pmd-" + language.getTerseName(), "java:" + functionsClass.getName());
    }


    @Override
    public List<ExtensionFunctionDefinition> getFunctions() {
        return Collections.emptyList();
    }

    public static List<ExtensionFunctionDefinition> convertAllStatics(final String namespacePrefix, final String namespaceUri, final Class<?> functionsClass) {
        List<ExtensionFunctionDefinition> functions = new ArrayList<>();
        for (final Method m : functionsClass.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())) {
                functions.add(convert(namespacePrefix, namespaceUri, m));
            }
        }
        return functions;
    }

    public static ExtensionFunctionDefinition convert(final String namespacePrefix, final String namespaceUri, final Method m) {
        ExtensionFunctionDefinition f = new ExtensionFunctionDefinition() {
            @Override
            public StructuredQName getFunctionQName() {
                return new StructuredQName(namespacePrefix, namespaceUri, m.getName());
            }

            @Override
            public SequenceType[] getArgumentTypes() {
                final Class<?>[] parameters = m.getParameterTypes();
                boolean firstArgIsContext = XPathContext.class.isAssignableFrom(parameters[0]);
                int parameterLength = firstArgIsContext ? parameters.length - 1 : parameters.length;
                SequenceType[] result = new SequenceType[parameterLength];
                for (int i = 0; i < parameterLength; i++) {
                    Class<?> type = parameters[firstArgIsContext ? i + 1 : i];
                    result[i] = convertToSequenceType(type);
                }
                return result;
            }
            
            private SequenceType convertToSequenceType(Class<?> type) {
                if (Enum.class.isAssignableFrom(type)) {
                    return SequenceType.SINGLE_STRING;
                } else if (String.class.isAssignableFrom(type)) {
                    return SequenceType.SINGLE_STRING;
                } else if (Boolean.class.isAssignableFrom(type) || Boolean.TYPE == type) {
                    return SequenceType.SINGLE_BOOLEAN;
                } else if (Integer.class.isAssignableFrom(type) || Integer.TYPE == type) {
                    return SequenceType.SINGLE_INTEGER;
                } else if (Long.class.isAssignableFrom(type) || Long.TYPE == type) {
                    return SequenceType.SINGLE_LONG;
                } else if (Double.class.isAssignableFrom(type) || Double.TYPE == type) {
                    return SequenceType.SINGLE_DOUBLE;
                } else if (Character.class.isAssignableFrom(type) || Character.TYPE == type) {
                    return SequenceType.SINGLE_STRING;
                } else if (Float.class.isAssignableFrom(type) || Float.TYPE == type) {
                    return SequenceType.SINGLE_FLOAT;
                } else if (Pattern.class.isAssignableFrom(type)) {
                    return SequenceType.SINGLE_STRING;
                } else {
                    throw new RuntimeException("Unable to create SequenceType for value of type: " + type);
                }
            }
            
            @Override
            public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
                return convertToSequenceType(m.getReturnType());
            }

            @Override
            public ExtensionFunctionCall makeCallExpression() {
                return new ExtensionFunctionCall() {
                    @Override
                    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
                        Object[] args = new Object[m.getParameterTypes().length];
                        boolean firstArgIsContext = XPathContext.class.isAssignableFrom(m.getParameterTypes()[0]);
                        if (firstArgIsContext) {
                            args[0] = context;
                        }
                        for (int i = 0; i < arguments.length; i++) {
                            args[firstArgIsContext ? i + 1 : i] = arguments[i].head().getStringValue();
                        }
                        try {
                            Object result = m.invoke(null, args);
                            return SaxonXPathRuleQuery.getAtomicRepresentation(result);
                        } catch (ReflectiveOperationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        };
        return f;
    }

    protected List<ExtensionFunctionDefinition> getFunctions(Language language, Class<?> functionsClass) {
        final String namespacePrefix = "pmd-" + language.getTerseName();
        final String namespaceUri = "https://pmd.github.io/pmd/xpath/lang/" + language.getTerseName();
        return convertAllStatics(namespacePrefix, namespaceUri, functionsClass);
    }
}
