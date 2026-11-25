package io.github.darcymf.ats;

import static io.github.darcymf.ats.TestPdfs.fromText;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.darcymf.ats.check.CheckResult;
import io.github.darcymf.ats.check.CheckResult.Status;
import io.github.darcymf.ats.check.ContactCheck;
import io.github.darcymf.ats.check.LetterSpacingCheck;
import io.github.darcymf.ats.check.LigatureCheck;
import io.github.darcymf.ats.check.NameCheck;
import io.github.darcymf.ats.check.SectionCheck;
import io.github.darcymf.ats.check.SymbolCheck;

/** Checks that only look at the words, so they can run on plain text. */
class TextChecksTest {

    @Test
    void ligaturesAreFoundAndShownAsNormalWords() {
        CheckResult result = new LigatureCheck().run(fromText("Wrote ﬁrmware and a proﬁle page, then ﬂashed it"));

        assertThat(result.status()).isEqualTo(Status.WARN);
        assertThat(result.details()).containsExactly("firmware", "profile", "flashed");
    }

    @Test
    void noLigaturesPasses() {
        assertThat(new LigatureCheck().run(fromText("Wrote firmware")).status()).isEqualTo(Status.PASS);
    }

    @Test
    void iconFontCharactersAreFlagged() {
        CheckResult result = new SymbolCheck().run(fromText(" 0412 345 678  me@example.com"));

        assertThat(result.status()).isEqualTo(Status.WARN);
        assertThat(result.details()).containsExactly("U+F095", "U+F0E0");
    }

    @Test
    void normalPunctuationIsNotASymbolProblem() {
        assertThat(new SymbolCheck().run(fromText("Résumé – C++ & C# • 2024–2025 | “quotes”")).status())
                .isEqualTo(Status.PASS);
    }

    @Test
    void findsEmailPhoneAndLinkedIn() {
        CheckResult result = new ContactCheck().run(fromText(
                "Jordan Smith\n0412 345 678 | jordan@example.com | linkedin.com/in/jordan-smith"));

        assertThat(result.status()).isEqualTo(Status.PASS);
        assertThat(result.details()).containsExactly("Email: found", "Phone: found", "LinkedIn: found");
    }

    @Test
    void yearRangesAreNotPhoneNumbers() {
        CheckResult result = new ContactCheck().run(fromText("Jordan Smith\njordan@example.com\nUQ 2016 - 2023"));

        assertThat(result.status()).isEqualTo(Status.WARN);
        assertThat(result.details()).contains("Phone: not found");
    }

    @Test
    void internationalPhoneFormatsWork() {
        assertThat(ContactCheck.findPhone("+61 412 345 678")).isTrue();
        assertThat(ContactCheck.findPhone("(07) 3365 1111")).isTrue();
        assertThat(ContactCheck.findPhone("GPA 6.17/7")).isFalse();
    }

    @Test
    void noContactDetailsFails() {
        assertThat(new ContactCheck().run(fromText("Jordan Smith\nSoftware student")).status())
                .isEqualTo(Status.FAIL);
    }

    @Test
    void nameOnFirstLinePasses() {
        assertThat(new NameCheck().run(fromText("\n  Mary-Jane O'Brien \nEmail")).status()).isEqualTo(Status.PASS);
    }

    @Test
    void contactLineFirstIsFlagged() {
        CheckResult result = new NameCheck().run(fromText("0412 345 678 | me@example.com\nJordan Smith"));

        assertThat(result.status()).isEqualTo(Status.WARN);
    }

    @Test
    void spacedOutNameIsNotAName() {
        assertThat(new NameCheck().run(fromText("J O R D A N  S M I T H")).status()).isEqualTo(Status.WARN);
    }

    @Test
    void spacedOutHeadingsAreFound() {
        CheckResult result = new LetterSpacingCheck().run(fromText("E X P E R I E N C E\nDid things at a company"));

        assertThat(result.status()).isEqualTo(Status.WARN);
        assertThat(result.details()).containsExactly("E X P E R I E N C E");
    }

    @Test
    void shortWordsAreNotSpacedLetters() {
        assertThat(new LetterSpacingCheck().run(fromText("I am a C and Java developer")).status())
                .isEqualTo(Status.PASS);
    }

    @Test
    void standardHeadingsInAnyCase() {
        CheckResult result = new SectionCheck().run(fromText("WORK EXPERIENCE\nstuff\nEducation:\nUQ\nTechnical Skills\nJava"));

        assertThat(result.status()).isEqualTo(Status.PASS);
        assertThat(result.details()).containsExactly("Experience", "Education", "Skills");
    }

    @Test
    void creativeHeadingsAreMissed() {
        CheckResult result = new SectionCheck().run(fromText("Where I've worked\nstuff\nEducation\nUQ\nMy toolbox\nJava"));

        assertThat(result.status()).isEqualTo(Status.WARN);
        assertThat(result.details()).containsExactly("Missing: Experience", "Missing: Skills");
    }

    @Test
    void headingWordsInsideASentenceDontCount() {
        CheckResult result = new SectionCheck().run(fromText("I have experience in education and skills"));
        assertThat(result.status()).isEqualTo(Status.WARN);
    }
}
