/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

module net.sourceforge.pmd.cli {
    exports net.sourceforge.pmd.cli;
    // exports net.sourceforge.pmd.cli.commands; // empty

    opens net.sourceforge.pmd.cli.commands.internal to info.picocli;
    opens net.sourceforge.pmd.cli.commands.typesupport.internal to info.picocli;
    opens net.sourceforge.pmd.cli.commands.mixins.internal to info.picocli;

    requires net.sourceforge.pmd.core;

    requires org.slf4j;
    requires org.slf4j.simple;
    requires info.picocli;
    requires me.tongfei.progressbar;
    requires org.checkerframework.checker.qual;
    requires org.apache.commons.lang3;

    // automatic names
    requires pmd.designer;
    requires org.jline.terminal;
}
