/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.visualforce.ast;

public abstract class AbstractVfTest {

    protected final VfParsingHelper vf =
        VfParsingHelper.DEFAULT
            .withResourceContext(getClass());

}
