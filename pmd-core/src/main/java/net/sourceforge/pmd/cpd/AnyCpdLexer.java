/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.document.TextDocument;
import net.sourceforge.pmd.util.StringUtil;

/**
 * Simple tokenization into words and separators. Can ignore end-of-line
 * comments and recognize double/single quoted string literals. It is
 * not a goal to be very customizable, or have very high quality.
 * Higher-quality lexers should be implemented with a lexer generator.
 *
 * <p>In PMD 7, this replaces AbstractTokenizer, which provided nearly
 * no more functionality.</p>
 * <p>Note: This class has been called AnyTokenizer in PMD 6.</p>
 */
public class AnyCpdLexer implements CpdLexer {

    private static final Pattern DEFAULT_PATTERN = makePattern("");

    private static Pattern makePattern(String singleLineCommentStart) {
        return Pattern.compile(
            "\\w++" // either a word
                + eolCommentFragment(singleLineCommentStart) // a comment
                + "|[^\"'\\s]" // a single separator char
                + "|\"(?:[^\"\\\\]++|\\\\.)*+\"" // a double-quoted string
                + "|'(?:[^'\\\\]++|\\\\.)*+'" // a single-quoted string
                + "|\n" // or a newline (to count lines), note that sourcecode normalizes line endings
        );
    }

    private final Pattern pattern;
    private final String commentStart;

    public AnyCpdLexer() {
        this(DEFAULT_PATTERN, "");
    }

    public AnyCpdLexer(String eolCommentStart) {
        this(makePattern(eolCommentStart), eolCommentStart);
    }

    private AnyCpdLexer(Pattern pattern, String commentStart) {
        this.pattern = pattern;
        this.commentStart = commentStart;
    }

    private static String eolCommentFragment(String start) {
        if (StringUtils.isBlank(start)) {
            return "";
        } else {
            return "|(?:" + Pattern.quote(start) + "[^\n]*+)"; // note: sourcecode normalizes line endings
        }
    }

    @Override
    public void tokenize(TextDocument document, TokenFactory tokens) {
        Chars text = document.getText();
        Matcher matcher = pattern.matcher(text);
        int lineNo = 1;
        int lastLineStart = 0;
        while (matcher.find()) {
            String image = matcher.group();
            if (isComment(image)) {
                continue;
            } else if (StringUtils.isWhitespace(image)) {
                lineNo++;
                lastLineStart = matcher.end();
                continue;
            }

            int bline = lineNo;
            int bcol = 1 + matcher.start() - lastLineStart; // + 1 because columns are 1 based
            int ecol = StringUtil.columnNumberAt(image, image.length()); // this already outputs a 1-based column
            if (ecol == image.length() + 1) {
                ecol = bcol + image.length(); // single-line token
            } else {
                // multiline, need to update the line count
                lineNo += StringUtil.lineNumberAt(image, image.length()) - 1;
                lastLineStart = matcher.start() + image.length() - ecol + 1;
            }
            tokens.recordToken(image, bline, bcol, lineNo, ecol);
        }
    }

    private boolean isComment(String tok) {
        return !commentStart.isEmpty() && tok.startsWith(commentStart);
    }
}
