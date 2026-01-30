/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.table.coreimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.util.AssertionUtil;

/**
 * An unmodifiable multimap type, efficient if the single-value case is the
 * most common.
 */
final class MostlySingularMultimap<K, V> {

    @SuppressWarnings("rawtypes")
    private static final MostlySingularMultimap EMPTY = new MostlySingularMultimap<>(Collections.emptyMap());

    private final Map<K, Object> map;

    private MostlySingularMultimap(Map<K, Object> map) {
        this.map = map;
    }

    /**
     * @deprecated Since 7.22.0. Will be made package-private.
     */
    @Deprecated
    @FunctionalInterface
    public interface MapMaker<K> { //NOPMD PublicMemberInNonPublicType

        /** Produce a new mutable map with the contents of the given map. */
        <V> Map<K, V> copy(Map<K, V> m);
    }

    @NonNull List<V> get(K k) {
        Object vs = map.get(k);
        return interpretValue(vs);
    }

    boolean isEmpty() {
        return map.isEmpty();
    }

    Set<K> keySet() {
        return map.keySet();
    }

    boolean containsKey(Object v) {
        return map.containsKey(v);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    void processValuesOneByOne(BiConsumer<K, V> consumer) {
        for (Entry<K, Object> entry : map.entrySet()) {
            K k = entry.getKey();
            Object vs = entry.getValue();
            if (vs instanceof VList) {
                for (V v : (VList<V>) vs) {
                    consumer.accept(k, v);
                }
            } else {
                consumer.accept(k, (V) vs);
            }
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private static <V> List<V> interpretValue(Object vs) {
        if (vs == null) {
            return Collections.emptyList();
        } else if (vs instanceof VList) {
            return (VList<V>) vs;
        } else {
            return Collections.singletonList((V) vs);
        }
    }

    @SuppressWarnings("unchecked")
    static <K, V> MostlySingularMultimap<K, V> empty() {
        return EMPTY;
    }

    static <K, V> Builder<K, V> newBuilder(MapMaker<K> mapMaker) {
        return new Builder<>(mapMaker);
    }


    // In case the value type V is an array list
    private static class VList<V> extends ArrayList<V> {

        VList(int size) {
            super(size);
        }

    }

    /**
     * Builder for a multimap. Can only be used once.
     * @deprecated Since 7.22.0. Will be made package-private.
     */
    @Deprecated
    public static final class Builder<K, V> { //NOPMD PublicMemberInNonPublicType

        private final MapMaker<K> mapMaker;
        private @Nullable Map<K, Object> map;
        private boolean consumed;
        /** True unless some entry has a list of values. */
        private boolean isSingular = true;

        private Builder(MapMaker<K> mapMaker) {
            this.mapMaker = mapMaker;
        }

        private Map<K, Object> getMapInternal() {
            if (map == null) {
                map = mapMaker.copy(Collections.emptyMap());
                Validate.isTrue(map.isEmpty(), "Map should be empty");
            }
            return map;
        }


        /**
         * @deprecated Since 7.22.0. Will be made package-private.
         */
        @Deprecated
        public void replaceValue(K key, V v) { //NOPMD PublicMemberInNonPublicType
            checkKeyValue(key, v);
            getMapInternal().put(key, v);
        }

        /**
         * @deprecated Since 7.22.0. Will be made package-private.
         */
        @Deprecated
        public void addUnlessKeyExists(K key, V v) { //NOPMD PublicMemberInNonPublicType
            checkKeyValue(key, v);
            getMapInternal().putIfAbsent(key, v);
        }

        /**
         * @deprecated Since 7.22.0. Will be made package-private.
         */
        @Deprecated
        public void appendValue(K key, V v) { //NOPMD PublicMemberInNonPublicType
            appendValue(key, v, false);
        }

        /**
         * @deprecated Since 7.22.0. Will be made package-private.
         */
        @Deprecated
        public void appendValue(K key, V v, boolean noDuplicate) { //NOPMD PublicMemberInNonPublicType
            checkKeyValue(key, v);

            getMapInternal().compute(key, (k, oldV) -> {
                return appendSingle(oldV, v, noDuplicate);
            });
        }

        private void checkKeyValue(K key, V v) {
            ensureOpen();
            AssertionUtil.requireParamNotNull("value", v);
            AssertionUtil.requireParamNotNull("key", key);
        }

        /**
         * @deprecated Since 7.22.0. Will be made package-private.
         */
        @Deprecated
        public Builder<K, V> groupBy(Iterable<? extends V> values, //NOPMD PublicMemberInNonPublicType
                                     Function<? super V, ? extends K> keyExtractor) {
            ensureOpen();
            return groupBy(values, keyExtractor, Function.identity());
        }


        /**
         * @deprecated Since 7.22.0. Will be made package-private.
         */
        @Deprecated
        public <I> Builder<K, V> groupBy(Iterable<? extends I> values, //NOPMD PublicMemberInNonPublicType
                                         Function<? super I, ? extends K> keyExtractor,
                                         Function<? super I, ? extends V> valueExtractor) {
            ensureOpen();
            for (I i : values) {
                appendValue(keyExtractor.apply(i), valueExtractor.apply(i));
            }
            return this;
        }

        /**
         * @deprecated Since 7.22.0. Will be made package-private.
         */
        @Deprecated
        // no duplicates
        public Builder<K, V> absorb(Builder<K, V> other) { //NOPMD PublicMemberInNonPublicType
            ensureOpen();
            other.ensureOpen();

            if (this.map == null) {
                this.map = other.map;
                this.isSingular = other.isSingular;
            } else {
                // isSingular may be changed in the loop by appendSingle
                this.isSingular &= other.isSingular;

                for (Entry<K, Object> otherEntry : other.getMapInternal().entrySet()) {
                    K key = otherEntry.getKey();
                    Object otherV = otherEntry.getValue();
                    map.compute(key, (k, myV) -> {
                        if (myV == null) {
                            return otherV;
                        } else if (otherV instanceof VList) {
                            Object newV = myV;
                            for (V v : (VList<V>) otherV) {
                                newV = appendSingle(newV, v, true);
                            }
                            return newV;
                        } else {
                            return appendSingle(myV, (V) otherV, true);
                        }
                    });
                }
            }

            other.consume();
            return this;
        }

        private Object appendSingle(@Nullable Object vs, V v, boolean noDuplicate) {
            if (vs == null) {
                return v;
            } else if (vs instanceof VList) {
                if (noDuplicate && ((VList) vs).contains(v)) {
                    return vs;
                }
                ((VList) vs).add(v);
                return vs;
            } else {
                if (noDuplicate && vs.equals(v)) {
                    return vs;
                }
                List<V> vs2 = new VList<>(2);
                isSingular = false;
                vs2.add((V) vs);
                vs2.add(v);
                return vs2;
            }
        }

        /**
         * @deprecated Since 7.22.0. Will be made package-private.
         */
        @Deprecated
        public MostlySingularMultimap<K, V> build() { //NOPMD PublicMemberInNonPublicType
            consume();
            return isEmpty() ? empty() : new MostlySingularMultimap<>(getMapInternal());
        }

        /**
         * @deprecated Since 7.22.0. Will be made package-private.
         */
        @Deprecated
        public @Nullable Map<K, V> buildAsSingular() { //NOPMD PublicMemberInNonPublicType
            consume();
            if (!isSingular) {
                return null; // NOPMD: returning null as in the spec (Nullable)
            }
            return (Map<K, V>) map;
        }


        private void consume() {
            ensureOpen();
            consumed = true;
        }

        private void ensureOpen() {
            Validate.isTrue(!consumed, "Builder was already consumed");
        }

        /**
         * @deprecated Since 7.22.0. Will be made package-private.
         */
        @Deprecated
        public boolean isSingular() { //NOPMD PublicMemberInNonPublicType
            return isSingular;
        }

        /**
         * @deprecated Since 7.22.0. Will be made package-private.
         */
        @Deprecated
        public Map<K, List<V>> getMutableMap() { //NOPMD PublicMemberInNonPublicType
            Map<K, List<V>> mutable = mapMaker.copy(Collections.emptyMap());
            for (Entry<K, Object> entry : getMapInternal().entrySet()) {
                mutable.put(entry.getKey(), interpretValue(entry.getValue()));
            }
            return mutable;
        }

        /**
         * @deprecated Since 7.22.0. Will be made package-private.
         */
        @Deprecated
        public boolean isEmpty() { //NOPMD PublicMemberInNonPublicType
            return map == null || map.isEmpty();
        }

    }

}
