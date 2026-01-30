/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.plsql.ast;

abstract class AbstractSelectStatement extends AbstractPLSQLNode {

    private boolean distinct;
    private boolean unique;
    private boolean all;

    AbstractSelectStatement(int i) {
        super(i);
    }

    void setDistinct(boolean distinct) {
        this.distinct = true;
    }

    boolean isDistinct() {
        return distinct;
    }

    void setUnique(boolean unique) {
        this.unique = unique;
    }

    boolean isUnique() {
        return unique;
    }

    void setAll(boolean all) {
        this.all = all;
    }

    boolean isAll() {
        return all;
    }

}
