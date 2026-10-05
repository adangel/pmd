/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cli.internal;

import static org.jline.utils.WCWidth.wcwidth;

import java.io.IOException;
import java.io.PrintStream;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import me.tongfei.progressbar.ConsoleProgressBarConsumer;
import me.tongfei.progressbar.InteractiveConsoleProgressBarConsumer;

/**
 * This is a friend class for me.tongfei.progressbar, as TerminalUtils is package-private.
 */
@SuppressWarnings("PMD.CloseResource")
public final class PmdProgressBarFriend {
    private static final char CARRIAGE_RETURN = '\r';

    private PmdProgressBarFriend() {
        throw new AssertionError("Can't instantiate utility classes");
    }

    public static ConsoleProgressBarConsumer createConsoleConsumer(PrintStream ps) {
        Terminal terminal = null;
        try {
            terminal = TerminalBuilder.builder().dumb(true).build();
        } catch (IOException e) {
            throw new RuntimeException("This should never happen! Dumb terminal should have been created.");
        }
        boolean cursorMovementSupported =
                terminal.getStringCapability(InfoCmp.Capability.cursor_up) != null
                    && terminal.getStringCapability(InfoCmp.Capability.cursor_down) != null;

        return cursorMovementSupported
                ? new InteractiveConsoleProgressBarConsumer(ps)
                : new PostCarriageReturnConsoleProgressBarConsumer(ps);
    }

    private static class PostCarriageReturnConsoleProgressBarConsumer extends ConsoleProgressBarConsumer {

        private final PrintStream out;

        PostCarriageReturnConsoleProgressBarConsumer(PrintStream out) {
            super(out);
            this.out = out;
        }

        // from me.tongfei.progressbar.StringDisplayUtils
        static int getCharDisplayLength(char c) {
            // wcwidth is actually implemented in jline3: no need to implement our own
            // control characters will have -1 wcwidth, but actually 0 when displayed
            return Math.max(wcwidth(c), 0);
        }

        // CHECKSTYLE:OFF
        static String trimDisplayLength(String s, int maxDisplayLength) {
            if (maxDisplayLength <= 0) {
                return "";
            }

            int totalLength = 0;
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '\033') {  // skip ANSI escape sequences
                    while (i < s.length() && s.charAt(i) != 'm') {
                        i++;
                    }
                    i++;  // skip the 'm' character
                }
                totalLength += getCharDisplayLength(s.charAt(i));
                if (totalLength > maxDisplayLength) {
                    return s.substring(0, i);
                }
            }
            return s;
        }
        // CHECKSTYLE:ON

        @Override
        public void accept(String str) {
            // Set the carriage return at the end instead of at the beginning
            out.print(trimDisplayLength(str, getMaxRenderedLength()) + CARRIAGE_RETURN);
        }

        @Override
        public void clear() {
            // do nothing (prints an empty line otherwise)
        }
    }
}
