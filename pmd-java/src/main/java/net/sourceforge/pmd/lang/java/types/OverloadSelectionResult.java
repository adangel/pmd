/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.types;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.annotation.Experimental;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorCall;
import net.sourceforge.pmd.lang.java.ast.ASTEnumConstant;
import net.sourceforge.pmd.lang.java.ast.InvocationNode;
import net.sourceforge.pmd.lang.java.ast.TypeNode;
import net.sourceforge.pmd.lang.java.types.internal.infer.ExprMirror;

/**
 * Information about the overload-resolution for a specific
 * {@linkplain InvocationNode expression}.
 */
// implnote: this is implemented privately in the infer package
public interface OverloadSelectionResult {

    /**
     * Returns the type of the method or constructor that is called by
     * the {@link InvocationNode}. This is a method type whose type
     * parameters have been instantiated by their actual inferred values.
     *
     * <p>For constructors the return type of this signature may be
     * different from the type of this node. For an anonymous class
     * constructor (in {@link ASTEnumConstant} or {@link ASTConstructorCall}),
     * the selected constructor is the *superclass* constructor. In
     * particular, if the anonymous class implements an interface,
     * the constructor is the constructor of class {@link Object}.
     * In that case though, the {@link TypeNode#getTypeMirror()} of the {@link InvocationNode}
     * will be the type of the anonymous class (hence the difference).
     *
     * <p></p>
     */
    JMethodSig getMethodType();

    /**
     * Whether the declaration needed unchecked conversion to be
     * applicable. In this case, the return type of the method is
     * erased.
     */
    boolean needsUncheckedConversion();


    /**
     * Returns true if this is a varargs call. This means, that the
     * called method is varargs, and was overload-selected in the varargs
     * phase. For example:
     * <pre>{@code
     * Arrays.asList("a", "b");                     // this is a varargs call
     * Arrays.asList(new String[] { "a", "b" });    // this is not a varargs call
     * }</pre>
     *
     * In this case, the last formal parameter of the method type should
     * be interpreted specially with-respect-to the argument expressions
     * (see {@link #ithFormalParam(int)}).
     */
    boolean isVarargsCall();

    /**
     * Returns the type of the i-th formal parameter of the method.
     * This is relevant when the call is varargs: {@code i} can in
     * that case be greater that the number of formal parameters.
     * In that case, the formal param type is the array element of
     * the vararg parameter, not the array type.
     *
     * @param i Index for a formal
     *
     * @throws AssertionError If the parameter is negative, or
     *                        greater than the number of argument
     *                        expressions to the method
     */
    default JTypeMirror ithFormalParam(int i) {
        return getMethodType().ithFormalParam(i, isVarargsCall());
    }

    /**
     * Returns true if the invocation of this method failed. This
     * means, the presented method type is a fallback, whose type
     * parameters might not have been fully instantiated. This may
     * also mean several methods were ambiguous, and an arbitrary
     * one was chosen.
     */
    boolean isFailed();


    /**
     * Return the type from which the search for accessible candidates
     * for overload resolution starts. This is just a hint as to where
     * the candidates come from. There may actually be candidates that
     * don't come from this type, eg in the case where this is an
     * unqualified method call, and some candidate methods are imported.
     * The type is null in some cases, eg, this is not implemented for
     * constructor call expressions.
     *
     * @see ExprMirror.MethodUsageMirror#getTypeToSearch()
     * @experimental Since 7.14.0
     */
    @Experimental
    @Nullable JTypeMirror getTypeToSearch();
}
