package io.github.darcymf.ats.check;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.darcymf.ats.pdf.ResumeDocument;

/** Wide letter spacing on headings comes out as "E X P E R I E N C E". */
public class LetterSpacingCheck implements Check {

    static final Pattern SPACED = Pattern.compile("(?<![\\p{L}\\p{N}])(?:\\p{L} ){3,}\\p{L}(?![\\p{L}\\p{N}])");

    @Override
    public CheckResult run(ResumeDocument doc) {
        Set<String> found = new LinkedHashSet<>();
        Matcher m = SPACED.matcher(doc.text());
        while (m.find()) {
            found.add(m.group());
        }
        String title = "Spaced-out letters";
        if (found.isEmpty()) {
            return CheckResult.pass("letter-spacing", title, "No words broken up by wide letter spacing.");
        }
        return CheckResult.warn("letter-spacing", title,
                "Some text came out with a space between every letter, so the ATS can't match it to a word or a section.",
                found.stream().limit(8).toList(),
                "Remove the extra letter spacing (character spacing in Word, letter-spacing in CSS) from headings and your name.");
    }
}
