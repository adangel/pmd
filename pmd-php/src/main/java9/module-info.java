/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.lang.php {
    exports net.sourceforge.pmd.lang.php;

    requires net.sourceforge.pmd.core;

    provides net.sourceforge.pmd.lang.Language with net.sourceforge.pmd.lang.php.PhpLanguageModule;
}
