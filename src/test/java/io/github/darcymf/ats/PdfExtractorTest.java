package io.github.darcymf.ats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.github.darcymf.ats.pdf.PdfExtractor;
import io.github.darcymf.ats.pdf.ResumeDocument;
import io.github.darcymf.ats.pdf.UnreadablePdfException;

class PdfExtractorTest {

    private final PdfExtractor extractor = new PdfExtractor();

    @Test
    void readsTextInOrder() {
        ResumeDocument doc = extractor.extract(TestPdfs.singleColumn());

        assertThat(doc.pageCount()).isEqualTo(1);
        assertThat(doc.text()).startsWith("Jordan Smith\n");
        assertThat(doc.text().indexOf("Experience")).isLessThan(doc.text().indexOf("Education"));
        assertThat(doc.rawText()).isEqualTo(doc.text());
        assertThat(doc.pages().get(0).glyphs()).isNotEmpty();
    }

    @Test
    void scannedPdfHasNoTextButOneImage() {
        ResumeDocument doc = extractor.extract(TestPdfs.imageOnly());

        assertThat(doc.text()).isBlank();
        assertThat(doc.imageCount()).isEqualTo(1);
    }

    @Test
    void countsPages() {
        assertThat(extractor.extract(TestPdfs.pages(3)).pageCount()).isEqualTo(3);
    }

    @Test
    void rejectsBrokenFiles() {
        byte[] junk = "%PDF-1.7 this is not really a pdf".getBytes();

        assertThatThrownBy(() -> extractor.extract(junk)).isInstanceOf(UnreadablePdfException.class);
    }
}
