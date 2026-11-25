package io.github.darcymf.ats.check;

import java.util.List;

import io.github.darcymf.ats.pdf.ResumeDocument;

public class LengthCheck implements Check {

    static final int MIN_WORDS = 150;

    @Override
    public CheckResult run(ResumeDocument doc) {
        int pages = doc.pageCount();
        String trimmed = doc.text().trim();
        int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
        String title = "Length";
        List<String> details = List.of(pages + (pages == 1 ? " page" : " pages"), words + " words");

        if (pages > 2) {
            return CheckResult.warn("length", title,
                    pages + " pages is long for a graduate or internship application. Recruiters usually expect one or two.",
                    details, "Cut it to two pages. Keep the most recent and most relevant work.");
        }
        if (words < MIN_WORDS) {
            return CheckResult.warn("length", title,
                    "Only " + words + " words. There may not be enough for the ATS to match against a job ad.",
                    details, "Add a bullet or two on what you built or achieved in each role or project.");
        }
        return new CheckResult("length", title, CheckResult.Status.PASS,
                pages + (pages == 1 ? " page" : " pages") + ", " + words + " words.", details, null);
    }
}
