/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.renderers;

/**
 * @author Brian Remedios
 */
final class ColumnDescriptor<T> {

    final String id;
    final String title;
    final Accessor<T> accessor;

    /**
     * @deprecated Since 7.22.0. This interface should not have been public.
     */
    @Deprecated
    public interface Accessor<T> { // NOPMD PublicMembersInNonPublicType

        String get(int idx, T violation, String lineSeparator);
    }

    ColumnDescriptor(String theId, String theTitle, Accessor<T> theAccessor) {
        id = theId;
        title = theTitle;
        accessor = theAccessor;
    }
}
