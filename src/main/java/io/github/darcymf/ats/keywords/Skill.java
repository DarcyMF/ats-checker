package io.github.darcymf.ats.keywords;

import java.util.List;
import java.util.regex.Pattern;

/** A skill from skills.txt and the patterns for each way of writing it. */
record Skill(String name, List<Pattern> patterns) {

    // Stops "C" matching inside "C++", "Git" inside "GitHub" and "SQL" inside "MySQL".
    private static final String BEFORE = "(?<![\\p{L}\\p{N}+#.])";
    private static final String AFTER = "(?![\\p{L}\\p{N}+#&])";

    static Skill parse(String line) {
        String[] parts = line.split("\\|");
        String name = parts[0].trim().replaceFirst("^=", "");
        List<Pattern> patterns = java.util.Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Skill::compile)
                .toList();
        return new Skill(name, patterns);
    }

    private static Pattern compile(String alias) {
        boolean exactCase = alias.startsWith("=") || alias.length() <= 2;
        alias = alias.replaceFirst("^=", "");
        // Let "REST API" also match "REST  API" or a line break between the words.
        String body = Pattern.quote(alias).replace(" ", "\\E\\s+\\Q");
        int flags = exactCase ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        return Pattern.compile(BEFORE + body + AFTER, flags);
    }

    boolean in(String text) {
        for (Pattern p : patterns) {
            if (p.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }
}
