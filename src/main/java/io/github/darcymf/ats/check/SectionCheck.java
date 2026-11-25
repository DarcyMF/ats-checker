package io.github.darcymf.ats.check;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.github.darcymf.ats.pdf.ResumeDocument;

/** ATSs sort a resume into fields using standard headings. Creative headings get missed. */
public class SectionCheck implements Check {

    static final Map<String, Set<String>> SECTIONS = new LinkedHashMap<>();

    static {
        SECTIONS.put("Experience", Set.of("experience", "work experience", "professional experience",
                "relevant experience", "employment", "employment history", "work history", "career history"));
        SECTIONS.put("Education", Set.of("education", "education and training", "qualifications",
                "academic background", "education & training"));
        SECTIONS.put("Skills", Set.of("skills", "technical skills", "key skills", "core skills",
                "skills and tools", "skills & tools", "technologies", "skills and interests"));
    }

    @Override
    public CheckResult run(ResumeDocument doc) {
        List<String> found = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, Set<String>> section : SECTIONS.entrySet()) {
            if (hasHeading(doc.text(), section.getValue())) {
                found.add(section.getKey());
            } else {
                missing.add(section.getKey());
            }
        }
        String title = "Section headings";
        if (missing.isEmpty()) {
            return new CheckResult("sections", title, CheckResult.Status.PASS,
                    "Found the standard headings an ATS looks for.", found, null);
        }
        return CheckResult.warn("sections", title,
                "Couldn't find a heading for " + String.join(" or ", missing)
                        + ". Without a standard heading, the ATS may not know where that part of your resume starts.",
                missing.stream().map(s -> "Missing: " + s).toList(),
                "Use plain headings on their own line: Experience, Education, Skills.");
    }

    static boolean hasHeading(String text, Set<String> names) {
        for (String line : text.split("\n")) {
            String h = line.trim().toLowerCase(Locale.ROOT).replaceAll("[:\\s]+$", "");
            if (names.contains(h)) {
                return true;
            }
        }
        return false;
    }
}
