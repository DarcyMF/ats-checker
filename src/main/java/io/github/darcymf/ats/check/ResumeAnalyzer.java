package io.github.darcymf.ats.check;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.darcymf.ats.keywords.KeywordMatcher;
import io.github.darcymf.ats.keywords.KeywordReport;
import io.github.darcymf.ats.pdf.ResumeDocument;

@Service
public class ResumeAnalyzer {

    // Order matters: this is the order they show on the page, worst problems first.
    private final List<Check> checks = List.of(
            new TextLayerCheck(),
            new NameCheck(),
            new ContactCheck(),
            new ColumnCheck(),
            new LigatureCheck(),
            new SymbolCheck(),
            new LetterSpacingCheck(),
            new SectionCheck(),
            new LengthCheck());

    private final KeywordMatcher keywords;

    public ResumeAnalyzer(KeywordMatcher keywords) {
        this.keywords = keywords;
    }

    public Report analyze(ResumeDocument doc, String jobAd) {
        List<CheckResult> results = checks.stream().map(c -> c.run(doc)).toList();
        KeywordReport keywordReport = jobAd == null || jobAd.isBlank() ? null : keywords.match(doc, jobAd);
        return new Report(doc.rawText(), doc.pageCount(), results, keywordReport);
    }

    /** @param text what the parser read, before joined letters are cleaned up */
    public record Report(String text, int pages, List<CheckResult> checks, KeywordReport keywords) {
    }
}
