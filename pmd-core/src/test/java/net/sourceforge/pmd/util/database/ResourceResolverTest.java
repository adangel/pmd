/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.xml.transform.Source;

import org.junit.jupiter.api.Test;

/**
 *
 * @author sturton
 */
class ResourceResolverTest {

    /**
     * Test of resolve method, of class ResourceResolver.
     */
    @Test
    void testResolve() throws Exception {
        System.out.println("resolve");
        String href = "";
        String base = "";
        ResourceResolver instance = new ResourceResolver();
        Source expResult = null;
        Source result = instance.resolve(href, base);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to
        // fail.
        // fail("The test case is a prototype.");
    }
}
