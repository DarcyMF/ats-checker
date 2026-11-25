package io.github.darcymf.ats.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

/**
 * Pulls the text out of a PDF the way a simple parser would: top to bottom, left to right.
 * The PDF is only ever held in memory.
 */
@Component
public class PdfExtractor {

    public ResumeDocument extract(byte[] pdf) {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            if (!doc.getCurrentAccessPermission().canExtractContent()) {
                throw new UnreadablePdfException("This PDF blocks text copying, so an ATS can't read it either.");
            }

            Collector collector = new Collector();
            collector.setSortByPosition(true);
            collector.setLineSeparator("\n");
            collector.setPageEnd("\n");
            String text = collector.getText(doc);

            int images = 0;
            for (PDPage page : doc.getPages()) {
                images += countImages(page.getResources(), 0);
            }
            return new ResumeDocument(text, collector.raw.toString(), collector.pages, images);
        } catch (InvalidPasswordException e) {
            throw new UnreadablePdfException("This PDF is password protected. Save a copy without a password.", e);
        } catch (IOException e) {
            throw new UnreadablePdfException("Couldn't read this PDF. Try exporting it again.", e);
        }
    }

    private static int countImages(PDResources resources, int depth) throws IOException {
        if (resources == null || depth > 5) {
            return 0;
        }
        int count = 0;
        for (COSName name : resources.getXObjectNames()) {
            PDXObject obj = resources.getXObject(name);
            if (obj instanceof PDImageXObject) {
                count++;
            } else if (obj instanceof PDFormXObject form) {
                count += countImages(form.getResources(), depth + 1);
            }
        }
        return count;
    }

    /**
     * PDFTextStripper already splits "ﬁ" into "fi" in its output. To show what a parser that
     * doesn't do that would see, we rebuild the text again from the raw characters.
     */
    private static final class Collector extends PDFTextStripper {
        final StringBuilder raw = new StringBuilder();
        final List<ResumeDocument.PageLayout> pages = new ArrayList<>();
        private List<ResumeDocument.Glyph> glyphs;

        Collector() {
            super();
        }

        @Override
        protected void startPage(PDPage page) throws IOException {
            super.startPage(page);
            PDRectangle box = page.getCropBox();
            glyphs = new ArrayList<>();
            pages.add(new ResumeDocument.PageLayout(box.getWidth(), box.getHeight(), glyphs));
        }

        @Override
        protected void processTextPosition(TextPosition t) {
            super.processTextPosition(t);
            String s = t.getUnicode();
            if (glyphs != null && s != null && !s.isBlank()) {
                glyphs.add(new ResumeDocument.Glyph(t.getXDirAdj(), t.getYDirAdj(), t.getWidthDirAdj(), s));
            }
        }

        @Override
        protected void writeString(String text, List<TextPosition> positions) throws IOException {
            super.writeString(text, positions);
            for (TextPosition p : positions) {
                if (p.getUnicode() != null) {
                    raw.append(p.getUnicode());
                }
            }
        }

        @Override
        protected void writeWordSeparator() throws IOException {
            super.writeWordSeparator();
            raw.append(getWordSeparator());
        }

        @Override
        protected void writeLineSeparator() throws IOException {
            super.writeLineSeparator();
            raw.append(getLineSeparator());
        }

        @Override
        protected void writePageEnd() throws IOException {
            super.writePageEnd();
            raw.append(getPageEnd());
        }
    }
}
