package io.github.darcymf.ats.pdf;

import java.util.List;

/**
 * Everything the checks need to know about one uploaded PDF.
 *
 * @param text      text as PDFBox gives it, with joined letters like "ﬁ" split back into "fi"
 * @param rawText   the same text before that cleanup, which is closer to what a basic parser sees
 * @param pages     where each character sits on the page, used to spot multi-column layouts
 * @param imageCount number of images in the file
 */
public record ResumeDocument(String text, String rawText, List<PageLayout> pages, int imageCount) {

    public int pageCount() {
        return pages.size();
    }

    /** One page: its size and the box around every visible character. */
    public record PageLayout(float width, float height, List<Glyph> glyphs) {
    }

    /** x and y are from the top left of the page, in PDF points. */
    public record Glyph(float x, float y, float width, String text) {
    }
}
