package io.github.darcymf.ats.keywords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.darcymf.ats.pdf.ResumeDocument;

/**
 * Finds the skills a job ad asks for and checks whether the resume mentions them.
 * This is roughly how recruiters filter in an ATS: they search for the skills in the ad.
 */
@Component
public class KeywordMatcher {

    private final List<Skill> skills;

    public KeywordMatcher() {
        this(loadSkills());
    }

    KeywordMatcher(List<Skill> skills) {
        this.skills = skills;
    }

    public KeywordReport match(ResumeDocument resume, String jobAd) {
        List<String> found = new ArrayList<>();
        List<String> broken = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (Skill skill : skills) {
            if (!skill.in(jobAd)) {
                continue;
            }
            if (skill.in(resume.rawText())) {
                found.add(skill.name());
            } else if (skill.in(resume.text())) {
                // only shows up once "ﬁ" is turned back into "fi"
                broken.add(skill.name());
            } else {
                missing.add(skill.name());
            }
        }
        int total = found.size() + broken.size() + missing.size();
        int score = total == 0 ? 0 : Math.round(100f * found.size() / total);
        return new KeywordReport(found, broken, missing, score);
    }

    public int skillCount() {
        return skills.size();
    }

    static List<Skill> loadSkills() {
        try (InputStream in = KeywordMatcher.class.getResourceAsStream("/skills.txt")) {
            if (in == null) {
                throw new IllegalStateException("skills.txt is missing from resources");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .map(Skill::parse)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
