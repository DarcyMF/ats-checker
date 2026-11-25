package io.github.darcymf.ats.check;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import io.github.darcymf.ats.pdf.ResumeDocument;

/**
 * Fonts like Calibri and Garamond join "fi", "fl" and "ff" into one character (a ligature).
 * Some parsers keep that single character, so "firmware" turns into "ﬁrmware" and a keyword
 * search for "firmware" misses it.
 */
public class LigatureCheck implements Check {

    private static final Pattern LIGATURE = Pattern.compile("[\\uFB00-\\uFB06]");
    private static final int MAX_EXAMPLES = 12;

    @Override
    public CheckResult run(ResumeDocument doc) {
        Set<String> words = new LinkedHashSet<>();
        int count = 0;
        for (String token : doc.rawText().split("\\s+")) {
            if (LIGATURE.matcher(token).find()) {
                count++;
                words.add(clean(token));
            }
        }
        String title = "Joined letters (ligatures)";
        if (count == 0) {
            return CheckResult.pass("ligatures", title, "No joined letters like \"ﬁ\" or \"ﬂ\". Words read normally.");
        }
        return CheckResult.warn("ligatures", title,
                count + (count == 1 ? " word uses" : " words use")
                        + " a joined \"fi\", \"fl\" or \"ff\" character. Some parsers keep it as one symbol, so a search for these words won't find them.",
                words.stream().limit(MAX_EXAMPLES).toList(),
                "Turn ligatures off. In Word: Font > Advanced > Ligatures: None. In CSS: font-variant-ligatures: none. Or use Arial, which doesn't join letters.");
    }

    /** "ﬁrmware," becomes "firmware" for display. */
    static String clean(String token) {
        String plain = Normalizer.normalize(token, Normalizer.Form.NFKC);
        return plain.replaceAll("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$", "");
    }
}
