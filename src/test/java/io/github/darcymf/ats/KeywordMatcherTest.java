package io.github.darcymf.ats;

import static io.github.darcymf.ats.TestPdfs.fromText;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.darcymf.ats.keywords.KeywordMatcher;
import io.github.darcymf.ats.keywords.KeywordReport;

class KeywordMatcherTest {

    private final KeywordMatcher matcher = new KeywordMatcher();

    @Test
    void loadsTheSkillList() {
        assertThat(matcher.skillCount()).isGreaterThan(150);
    }

    @Test
    void splitsAdSkillsIntoFoundAndMissing() {
        KeywordReport report = matcher.match(
                fromText("Java developer. Built REST APIs with Spring Boot and Postgres."),
                "We want Java, Spring Boot, PostgreSQL, Docker and AWS experience.");

        assertThat(report.found()).containsExactly("Java", "Spring Boot", "PostgreSQL");
        assertThat(report.missing()).containsExactlyInAnyOrder("Docker", "AWS");
        assertThat(report.score()).isEqualTo(60);
    }

    @Test
    void wordsInsideOtherWordsDontCount() {
        KeywordReport report = matcher.match(
                fromText("Used C++, GitHub and MySQL"),
                "Experience with C, Git and SQL");

        assertThat(report.found()).isEmpty();
        assertThat(report.missing()).containsExactly("C", "SQL", "Git");
    }

    @Test
    void shortNamesAreCaseSensitive() {
        KeywordReport report = matcher.match(fromText("Wrote R scripts"), "nice to have: r or c");

        // lowercase r and c in the ad aren't the languages
        assertThat(report.found()).isEmpty();
        assertThat(report.missing()).isEmpty();
    }

    @Test
    void everydayWordsAreNotSkills() {
        KeywordReport report = matcher.match(fromText("Java"),
                "You'll react quickly, excel at teamwork and express ideas clearly. Spring is busy.");

        assertThat(report.found()).isEmpty();
        assertThat(report.missing()).isEmpty();
    }

    @Test
    void ligaturesHideKeywords() {
        KeywordReport report = matcher.match(fromText("Wrote STM32 ﬁrmware"), "Firmware and STM32");

        assertThat(report.found()).containsExactly("STM32");
        assertThat(report.broken()).containsExactly("Firmware");
        assertThat(report.score()).isEqualTo(50);
    }

    @Test
    void multiWordSkillsMatchAcrossLineBreaks() {
        KeywordReport report = matcher.match(fromText("Built a REST\nAPI"), "REST API");

        assertThat(report.found()).contains("REST");
    }

    @Test
    void adWithNoKnownSkillsScoresZero() {
        KeywordReport report = matcher.match(fromText("Java"), "Must be a great communicator");

        assertThat(report.score()).isZero();
        assertThat(report.found()).isEmpty();
    }
}
