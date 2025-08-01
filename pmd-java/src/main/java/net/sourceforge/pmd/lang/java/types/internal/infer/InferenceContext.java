/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.types.internal.infer;


import static net.sourceforge.pmd.lang.java.types.TypeOps.asList;
import static net.sourceforge.pmd.util.CollectionUtil.intersect;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.types.JClassType;
import net.sourceforge.pmd.lang.java.types.JMethodSig;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.JTypeVar;
import net.sourceforge.pmd.lang.java.types.JTypeVisitable;
import net.sourceforge.pmd.lang.java.types.SubstVar;
import net.sourceforge.pmd.lang.java.types.Substitution;
import net.sourceforge.pmd.lang.java.types.TypeOps;
import net.sourceforge.pmd.lang.java.types.TypeSystem;
import net.sourceforge.pmd.lang.java.types.internal.infer.ExprMirror.InvocationMirror.MethodCtDecl;
import net.sourceforge.pmd.lang.java.types.internal.infer.IncorporationAction.CheckBound;
import net.sourceforge.pmd.lang.java.types.internal.infer.IncorporationAction.PropagateAllBounds;
import net.sourceforge.pmd.lang.java.types.internal.infer.IncorporationAction.PropagateBounds;
import net.sourceforge.pmd.lang.java.types.internal.infer.IncorporationAction.SubstituteInst;
import net.sourceforge.pmd.lang.java.types.internal.infer.InferenceVar.BoundKind;
import net.sourceforge.pmd.lang.java.types.internal.infer.VarWalkStrategy.GraphWalk;
import net.sourceforge.pmd.util.CollectionUtil;

/**
 * Context of a type inference process. This object maintains a set of
 * unique inference variables. Inference variables maintain the set of
 * bounds that apply to them.
 */
final class InferenceContext {

    // ivar/ctx ids are globally unique, & repeatable in debug output if you do exactly the same run
    private static int varId = 0;
    private static int ctxId = 0;

    private final Map<InstantiationListener, Set<InferenceVar>> instantiationListeners = new HashMap<>();
    // explicit dependencies between variables for graph building
    private final Map<InferenceVar, Set<InferenceVar>> instantiationConstraints = new HashMap<>();
    // This flag is set to true when the explicit dependencies are changed,
    // or when this context adopted new ivars. This means we should interrupt
    // resolution and recompute the dependency graph between ivars, because
    // the new variables may have dependencies on existing variables, and vice versa.
    private boolean graphWasChanged = false;

    private final Set<InferenceVar> freeVars = new LinkedHashSet<>();
    private final Set<InferenceVar> inferenceVars = new LinkedHashSet<>();
    private final Deque<IncorporationAction> incorporationActions = new ArrayDeque<>();
    final TypeSystem ts;
    private final SupertypeCheckCache supertypeCheckCache;
    final TypeInferenceLogger logger;

    private Substitution mapping = Substitution.EMPTY;
    private @Nullable InferenceContext parent;
    private boolean needsUncheckedConversion;
    private final int id;

    /**
     * Create an inference context from a set of type variables to instantiate.
     * This creates inference vars and adds the initial bounds as described in
     *
     * https://docs.oracle.com/javase/specs/jls/se9/html/jls-18.html#jls-18.1.3
     *
     * under the purple rectangle.
     *
     * @param ts                  The global type system
     * @param supertypeCheckCache Super type check cache, shared by all
     *                            inference runs in the same compilation unit
     *                            (stored in {@link Infer}).
     * @param tvars               Initial tvars which will be turned
     *                            into ivars
     * @param logger              Logger for events related to ivar bounds
     */
    InferenceContext(TypeSystem ts, SupertypeCheckCache supertypeCheckCache, List<JTypeVar> tvars, TypeInferenceLogger logger) {
        this(ts, supertypeCheckCache, tvars, logger, true);
    }

    /**
     * Create an inference context from a set of type variables to instantiate.
     * This creates inference vars and may add the initial bounds as described in
     *
     * https://docs.oracle.com/javase/specs/jls/se9/html/jls-18.html#jls-18.1.3
     *
     * under the purple rectangle.
     *
     * @param ts                  The global type system
     * @param supertypeCheckCache Super type check cache, shared by all
     *                            inference runs in the same compilation unit
     *                            (stored in {@link Infer}).
     * @param tvars               Initial tvars which will be turned
     *                            into ivars
     * @param logger              Logger for events related to ivar bounds
     * @param addPrimaryBound     Whether to add the primary bound of the vars.
     */
    @SuppressWarnings("PMD.AssignmentToNonFinalStatic") // ctxId
    InferenceContext(TypeSystem ts, SupertypeCheckCache supertypeCheckCache, List<JTypeVar> tvars, TypeInferenceLogger logger, boolean addPrimaryBound) {
        this.ts = ts;
        this.supertypeCheckCache = supertypeCheckCache;
        this.logger = logger;
        this.id = ctxId++;

        for (JTypeVar p : tvars) {
            addVarImpl(p);
        }

        if (addPrimaryBound) {
            addPrimaryBounds();
        }
    }

    /**
     * Add the primary bounds for the ivars of this context. This is usually done upon construction but may be deferred
     * in some scenarios (inference of ground target type of an explicitly typed lambda).
     */
    void addPrimaryBounds() {
        for (InferenceVar ivar : inferenceVars) {
            addPrimaryBound(ivar);
        }
    }

    /**
     * Performs a shallow copy of this context, which would allow solving
     * the variables without executing listeners. Instantiation listeners
     * are not copied, and parent contexts are not copied.
     */
    public InferenceContext shallowCopy() {
        final InferenceContext copy = new InferenceContext(ts, supertypeCheckCache, Collections.emptyList(), logger);
        copy.freeVars.addAll(this.freeVars);
        copy.inferenceVars.addAll(this.inferenceVars);
        copy.incorporationActions.addAll(this.incorporationActions);
        copy.instantiationConstraints.putAll(this.instantiationConstraints);
        copy.mapping = mapping; // mapping is immutable, so we can share it safely

        return copy;
    }

    public int getId() {
        return id;
    }

    private void addPrimaryBound(InferenceVar ivar) {
        for (JTypeMirror ui : asList(ivar.getBaseVar().getUpperBound())) {
            ivar.addPrimaryBound(BoundKind.UPPER, mapToIVars(ui));
        }
    }

    /**
     * Add a variable to this context.
     */
    InferenceVar addVar(JTypeVar tvar) {
        InferenceVar ivar = addVarImpl(tvar);
        addPrimaryBound(ivar);

        for (InferenceVar otherIvar : inferenceVars) {
            // remove remaining occurrences of type params
            otherIvar.substBounds(this::mapToIVars);
        }
        return ivar;
    }

    /**
     * Add a variable to this context.
     */
    private InferenceVar addVarImpl(@NonNull JTypeVar tvar) {
        InferenceVar ivar = new InferenceVar(this, tvar, varId++);
        freeVars.add(ivar);
        inferenceVars.add(ivar);
        mapping = mapping.plus(tvar, ivar);
        return ivar;
    }

    /**
     * Replace all type variables in the given type with corresponding
     * inference vars.
     */
    JTypeMirror mapToIVars(JTypeMirror t) {
        return TypeOps.subst(t, mapping);
    }

    /**
     * Replace all type variables in the given type with corresponding
     * inference vars.
     */
    JMethodSig mapToIVars(JMethodSig t) {
        return t.subst(mapping);
    }

    /**
     * Returns true if the type mentions no free inference variables.
     * This is what the JLS calls a "proper type".
     */
    boolean isGround(JTypeVisitable t) {
        return !TypeOps.mentionsAny(t, freeVars);
    }

    /**
     * Returns true if the type mentions no free inference variables.
     */
    boolean areAllGround(Collection<? extends JTypeVisitable> ts) {
        for (JTypeVisitable t : ts) {
            if (!isGround(t)) {
                return false;
            }
        }
        return true;
    }

    Set<InferenceVar> freeVarsIn(Iterable<? extends JTypeVisitable> types) {
        Set<InferenceVar> vars = new LinkedHashSet<>();
        for (InferenceVar ivar : freeVars) {
            for (JTypeVisitable t : types) {
                if (TypeOps.mentions(t, ivar)) {
                    vars.add(ivar);
                }
            }
        }
        return vars;
    }

    Set<InferenceVar> freeVarsIn(JTypeVisitable t) {
        return freeVarsIn(Collections.singleton(t));
    }

    /**
     * Replace instantiated inference vars with their instantiation in the given type.
     */
    JTypeMirror ground(JTypeMirror t) {
        return t.subst(InferenceContext::groundSubst);
    }

    JClassType ground(JClassType t) {
        return t.subst(InferenceContext::groundSubst);
    }

    /**
     * Replace instantiated inference vars with their instantiation in the given type.
     */
    JMethodSig ground(JMethodSig t) {
        return t.subst(InferenceContext::groundSubst);
    }

    void setNeedsUncheckedConversion() {
        this.needsUncheckedConversion = true;
    }

    /**
     * Whether incorporation/solving required an unchecked conversion.
     * This means the invocation type of the overload must be erased.
     *
     * @see MethodCtDecl#needsUncheckedConversion()
     */
    boolean needsUncheckedConversion() {
        return this.needsUncheckedConversion;
    }

    SupertypeCheckCache getSupertypeCheckCache() {
        return supertypeCheckCache;
    }

    private static JTypeMirror groundSubst(SubstVar var) {
        if (var instanceof InferenceVar) {
            JTypeMirror inst = ((InferenceVar) var).getInst();
            if (inst != null) {
                return inst;
            }
        }
        return var;
    }

    /**
     * Replace instantiated inference vars with their instantiation in the given type,
     * or else replace them with a failed type.
     */
    static JMethodSig finalGround(JMethodSig t) {
        return t.subst(s -> {
            if (!(s instanceof InferenceVar)) {
                return s;
            } else {
                InferenceVar ivar = (InferenceVar) s;
                return ivar.getInst() != null ? ivar.getInst() : s.getTypeSystem().ERROR;
            }
        });
    }


    /**
     * Replace instantiated inference vars with their instantiation in the given type,
     * or else replace them with a wildcard.
     */
    static JTypeMirror groundOrWildcard(JTypeMirror t) {
        return t.subst(s -> {
            if (!(s instanceof InferenceVar)) {
                return s;
            } else {
                InferenceVar ivar = (InferenceVar) s;
                return ivar.getInst() != null ? ivar.getInst() : s.getTypeSystem().UNBOUNDED_WILD;
            }
        });
    }

    /**
     * Copy variable in this inference context to the given context
     */
    void duplicateInto(final InferenceContext that) {
        boolean changedGraph = !that.freeVars.containsAll(this.freeVars)
            || !this.instantiationConstraints.isEmpty();
        that.graphWasChanged |= changedGraph;
        that.inferenceVars.addAll(this.inferenceVars);
        that.freeVars.addAll(this.freeVars);
        that.incorporationActions.addAll(this.incorporationActions);
        that.instantiationListeners.putAll(this.instantiationListeners);
        CollectionUtil.mergeMaps(
            that.instantiationConstraints,
            this.instantiationConstraints,
            (set1, set2) -> {
                set1.addAll(set2);
                return set1;
            });

        this.parent = that;

        // propagate existing bounds into the new context
        for (InferenceVar freeVar : this.freeVars) {
            that.incorporationActions.add(new PropagateAllBounds(freeVar));
        }
    }


    // The `from` ivars depend on the `dependencies` ivars for resolution.
    void addInstantiationDependencies(Set<? extends InferenceVar> from, Set<? extends InferenceVar> dependencies) {
        if (from.isEmpty()) {
            return;
        }
        Set<InferenceVar> outputVars = new HashSet<>(dependencies);
        outputVars.removeAll(from);
        if (outputVars.isEmpty()) {
            return;
        }
        for (InferenceVar inputVar : from) {
            logger.ivarDependencyRegistered(this, inputVar, outputVars);
            instantiationConstraints.merge(inputVar, outputVars, (o1, o2) -> {
                o2 = new LinkedHashSet<>(o2);
                o2.addAll(o1);
                return o2;
            });
        }
    }

    Map<InferenceVar, Set<InferenceVar>> getInstantiationDependencies() {
        return instantiationConstraints;
    }

    void addInstantiationListener(Set<? extends JTypeMirror> relevantTypes, InstantiationListener listener) {
        Set<InferenceVar> free = freeVarsIn(relevantTypes);
        if (free.isEmpty()) {
            listener.onInstantiation(this);
            return;
        }
        instantiationListeners.put(listener, free);
    }

    /**
     * Call the listeners registered with {@link #addInstantiationListener(Set, InstantiationListener)}.
     * Listeners are used to perform deferred checks, like checking
     * compatibility of a formal parameter with an expression when the
     * formal parameter is not ground.
     */
    void callListeners() {
        if (instantiationListeners.isEmpty()) {
            return;
        }
        Set<InferenceVar> solved = new LinkedHashSet<>(inferenceVars);
        solved.removeAll(freeVars);


        for (Entry<InstantiationListener, Set<InferenceVar>> entry : new LinkedHashSet<>(instantiationListeners.entrySet())) {
            if (solved.containsAll(entry.getValue())) {
                try {
                    entry.getKey().onInstantiation(this);
                } catch (ResolutionFailedException ignored) {
                    // that is a compile-time error, but that
                    // shouldn't affect PMD

                    // This can happen eg when an assertion fails in a
                    // subcontext that depends on this one, which is waiting
                    // for more inference to happen

                    // TODO investigate
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    instantiationListeners.remove(entry.getKey());
                }
            }
        }
    }

    Set<InferenceVar> getFreeVars() {
        return Collections.unmodifiableSet(freeVars);
    }

    private void onVarInstantiated(InferenceVar ivar) {
        if (parent != null) {
            parent.onVarInstantiated(ivar);
            return;
        }

        logger.ivarInstantiated(this, ivar, ivar.getInst());

        incorporationActions.addFirst(new SubstituteInst(ivar, ivar.getInst()) {
            @Override
            public void apply(InferenceContext ctx) {
                freeVars.removeIf(it -> it.getInst() != null);
                super.apply(ctx);
            }
        });
    }


    void onBoundAdded(InferenceVar ivar, BoundKind kind, JTypeMirror bound, boolean isPrimary) {
        // guard against α <: Object
        // all variables have it, it's useless to propagate it
        if (kind != BoundKind.UPPER || bound != ts.OBJECT) {
            if (parent != null) {
                parent.onBoundAdded(ivar, kind, bound, isPrimary);
                return;
            }

            logger.boundAdded(this, ivar, kind, bound, isPrimary);

            incorporationActions.add(new CheckBound(ivar, kind, bound));
            incorporationActions.add(new PropagateBounds(ivar, kind, bound));
        }
    }

    void onIvarMerged(InferenceVar prev, InferenceVar delegate) {
        if (parent != null) {
            parent.onIvarMerged(prev, delegate);
            return;
        }

        logger.ivarMerged(this, prev, delegate);

        mapping = mapping.plus(prev.getBaseVar(), delegate);
        incorporationActions.addFirst(new SubstituteInst(prev, delegate));
    }

    /**
     * Runs the incorporation hooks registered for the free vars.
     *
     * @throws ResolutionFailedException If some propagated bounds are incompatible
     */
    void incorporate() {
        if (incorporationActions.isEmpty()) {
            return;
        }

        IncorporationAction hook = incorporationActions.pollFirst();
        while (hook != null) {

            if (hook.doApplyToInstVar || hook.ivar.getInst() == null) {
                hook.apply(this);
            }

            hook = incorporationActions.pollFirst();
        }
    }

    /**
     * @throws ResolutionFailedException Because it calls {@link #incorporate()}
     */
    void solve() {
        solve(false);
    }

    boolean solve(boolean onlyBoundedVars) {
        return solve(() -> new GraphWalk(this, onlyBoundedVars));
    }

    /**
     * Solve a single var, this does not solve its dependencies, so that
     * if some bounds are not ground, instantiation will be wrong.
     */
    void solve(InferenceVar var) {
        solve(new GraphWalk(var));
    }


    private boolean solve(Supplier<VarWalkStrategy> newWalker) {
        VarWalkStrategy strategy = newWalker.get();
        while (strategy != null) {
            if (solve(strategy)) {
                break;
            }
            strategy = newWalker.get();
        }
        return freeVars.isEmpty();
    }


    /**
     * This returns true if solving the VarWalkStrategy succeeded entirely.
     * Resolution can be interrupted early to account for new ivars and dependencies,
     * which may change the graph dependencies. In this case this method returns
     * false, we recompute the graph with the new ivars and dependencies, and
     * we try again to make progress.
     */
    private boolean solve(VarWalkStrategy walker) {
        graphWasChanged = false;
        incorporate();

        while (walker.hasNext()) {

            Set<InferenceVar> varsToSolve = walker.next();

            boolean progress = true;
            //repeat until all variables are solved
            outer:
            while (!intersect(freeVars, varsToSolve).isEmpty() && progress) {
                if (graphWasChanged) {
                    graphWasChanged = false;
                    logger.contextDependenciesChanged(this);
                    return false;
                }

                progress = false;
                for (List<ReductionStep> wave : ReductionStep.WAVES) {
                    if (solveBatchProgressed(varsToSolve, wave)) {
                        incorporate();
                        progress = true;
                        callListeners();
                        continue outer;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Tries to solve as much of varsToSolve as possible using some reduction steps.
     * Returns the set of solved variables during this step.
     */
    private boolean solveBatchProgressed(Set<InferenceVar> varsToSolve, List<ReductionStep> wave) {
        for (InferenceVar ivar : intersect(varsToSolve, freeVars)) {
            for (ReductionStep step : wave) {
                if (step.accepts(ivar, this)) {
                    ivar.setInst(step.solve(ivar, this));
                    onVarInstantiated(ivar);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isEmpty() {
        return inferenceVars.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Inference context " + getId()).append('\n');
        for (InferenceVar ivar : inferenceVars) {
            sb.append(ivar);
            if (ivar.getInst() != null) {
                sb.append(" := ").append(ivar.getInst()).append('\n');
            } else {
                ivar.formatBounds(sb).append('\n');
            }
        }

        return sb.toString();
    }

    /** A callback called when a set of variables have been solved. */
    public interface InstantiationListener {

        /**
         * Called when the set of dependencies provided to {@link #addInstantiationListener(Set, InstantiationListener)}
         * have been solved. The parameter is not necessarily the context
         * on which this has been registered, because contexts adopt the
         * inference variables of their children in some cases, to solve
         * them together. Use {@link #ground(JClassType)} with the context
         * parameter, not the context on which the callback was registered.
         */
        void onInstantiation(InferenceContext solvedCtx);

    }
}
