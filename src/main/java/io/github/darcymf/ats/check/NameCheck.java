package io.github.darcymf.ats.check;

import java.util.List;

import io.github.darcymf.ats.pdf.ResumeDocument;

/** Most parsers take the first line as your name. */
public class NameCheck implements Check {

    @Override
    public CheckResult run(ResumeDocument doc) {
        String first = firstLine(doc.text());
        String title = "Name at the top";
        if (first.isEmpty()) {
            return CheckResult.fail("name", title, "Couldn't find any text at the top of the page.", List.of(),
                    "Put your name as plain text on the first line.");
        }
        if (looksLikeName(first)) {
            return CheckResult.pass("name", title, "First line reads as a name: \"" + first + "\".");
        }
        return CheckResult.warn("name", title,
                "The first line a parser reads is \"" + shorten(first) + "\", which doesn't look like a name. It may fill your name in wrong.",
                List.of(shorten(first)),
                "Make your name the very first line, on its own, in normal text (not an image or the page header).");
    }

    static String firstLine(String text) {
        for (String line : text.split("\n")) {
            if (!line.isBlank()) {
                return line.trim();
            }
        }
        return "";
    }

    static boolean looksLikeName(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length < 2 || parts.length > 4 || line.length() > 40) {
            return false;
        }
        for (String p : parts) {
            // allow O'Brien, Smith-Jones, and names with accents; a single letter means it was spaced out
            if (p.length() < 2 || !p.matches("[\\p{L}][\\p{L}'’.-]*")) {
                return false;
            }
        }
        return true;
    }

    private static String shorten(String s) {
        return s.length() > 60 ? s.substring(0, 57) + "..." : s;
    }
}
