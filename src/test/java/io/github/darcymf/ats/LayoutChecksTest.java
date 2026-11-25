package io.github.darcymf.ats;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.darcymf.ats.check.CheckResult;
import io.github.darcymf.ats.check.CheckResult.Status;
import io.github.darcymf.ats.check.ColumnCheck;
import io.github.darcymf.ats.check.LengthCheck;
import io.github.darcymf.ats.check.TextLayerCheck;
import io.github.darcymf.ats.pdf.PdfExtractor;

/** Checks that need a real PDF, because they look at where text sits on the page. */
class LayoutChecksTest {

    private final PdfExtractor extractor = new PdfExtractor();

    @Test
    void twoColumnsAreFlagged() {
        CheckResult result = new ColumnCheck().run(extractor.extract(TestPdfs.twoColumn()));

        assertThat(result.status()).isEqualTo(Status.WARN);
        assertThat(result.details()).containsExactly("Page 1");
    }

    @Test
    void fullWidthHeaderDoesntHideTheColumns() {
        assertThat(new ColumnCheck().run(extractor.extract(TestPdfs.twoColumnWithHeader())).status())
                .isEqualTo(Status.WARN);
    }

    @Test
    void realSidebarTemplateIsFlagged() throws Exception {
        // made in Chrome from a typical template: big spaced-out name across the top, then a sidebar
        assertThat(new ColumnCheck().run(extractor.extract(fixture("sidebar-resume.pdf"))).status())
                .isEqualTo(Status.WARN);
    }

    @Test
    void sparseSidebarIsFlagged() throws Exception {
        // a few short skill words beside a lot of main text
        assertThat(new ColumnCheck().run(extractor.extract(fixture("sparse-sidebar-resume.pdf"))).status())
                .isEqualTo(Status.WARN);
    }

    private byte[] fixture(String name) throws Exception {
        try (var in = getClass().getResourceAsStream("/fixtures/" + name)) {
            return in.readAllBytes();
        }
    }

    @Test
    void oneColumnPasses() {
        assertThat(new ColumnCheck().run(extractor.extract(TestPdfs.singleColumn())).status())
                .isEqualTo(Status.PASS);
    }

    @Test
    void datesOnTheRightAreNotMistakenForAColumn() {
        assertThat(new ColumnCheck().run(extractor.extract(TestPdfs.rightAlignedDates())).status())
                .isEqualTo(Status.PASS);
    }

    @Test
    void scannedResumeFails() {
        CheckResult result = new TextLayerCheck().run(extractor.extract(TestPdfs.imageOnly()));

        assertThat(result.status()).isEqualTo(Status.FAIL);
        assertThat(result.message()).contains("scanned");
    }

    @Test
    void normalResumeHasText() {
        assertThat(new TextLayerCheck().run(extractor.extract(TestPdfs.singleColumn())).status())
                .isEqualTo(Status.PASS);
    }

    @Test
    void threePagesIsTooLong() {
        assertThat(new LengthCheck().run(extractor.extract(TestPdfs.pages(3))).status())
                .isEqualTo(Status.WARN);
    }
}
