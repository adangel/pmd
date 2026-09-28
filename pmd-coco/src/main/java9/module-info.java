/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.coco {
    exports net.sourceforge.pmd.lang.coco;
    exports net.sourceforge.pmd.lang.coco.ast; // note: everything there is deprecated
    exports net.sourceforge.pmd.lang.coco.cpd;

    requires net.sourceforge.pmd.core;

    requires org.antlr.antlr4.runtime;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.coco.CocoLanguageModule;
}
