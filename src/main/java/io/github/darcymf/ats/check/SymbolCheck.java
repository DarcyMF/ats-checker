package io.github.darcymf.ats.check;

import java.util.LinkedHashSet;
import java.util.Set;

import io.github.darcymf.ats.pdf.ResumeDocument;

/**
 * Icon fonts (the little phone and email icons in a lot of templates) come out as characters
 * from Unicode's "private use" area, which mean nothing outside that font.
 */
public class SymbolCheck implements Check {

    @Override
    public CheckResult run(ResumeDocument doc) {
        Set<String> codes = new LinkedHashSet<>();
        int count = 0;
        String raw = doc.rawText();
        for (int i = 0; i < raw.length(); ) {
            int cp = raw.codePointAt(i);
            if (isJunk(cp)) {
                count++;
                codes.add(String.format("U+%04X", cp));
            }
            i += Character.charCount(cp);
        }
        String title = "Icons and symbols";
        if (count == 0) {
            return CheckResult.pass("symbols", title, "No icon characters that would come out as gibberish.");
        }
        return CheckResult.warn("symbols", title,
                count + (count == 1 ? " character" : " characters")
                        + " came out as unreadable symbols. These are usually icons for phone, email or location from the template.",
                codes.stream().limit(8).toList(),
                "Replace icons with plain words, like \"Phone:\" and \"Email:\", or remove them.");
    }

    static boolean isJunk(int cp) {
        boolean privateUse = (cp >= 0xE000 && cp <= 0xF8FF) || cp >= 0xF0000;
        boolean replacement = cp == 0xFFFD;
        boolean control = Character.getType(cp) == Character.CONTROL && cp != '\n' && cp != '\r' && cp != '\t';
        return privateUse || replacement || control;
    }
}
