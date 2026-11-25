package io.github.darcymf.ats.check;

import java.util.List;

import io.github.darcymf.ats.pdf.ResumeDocument;

/** A scanned or "print to image" resume has no text at all, so the ATS gets a blank page. */
public class TextLayerCheck implements Check {

    static final int MIN_CHARS = 200;

    @Override
    public CheckResult run(ResumeDocument doc) {
        long chars = doc.text().chars().filter(c -> !Character.isWhitespace(c)).count();
        String title = "Readable text";
        if (chars < MIN_CHARS) {
            String why = doc.imageCount() > 0
                    ? "The file is mostly images, so it was probably scanned or saved as a picture."
                    : "There's almost nothing to read.";
            return CheckResult.fail("text-layer", title,
                    "Only " + chars + " characters of text found. " + why + " An ATS would see an almost empty page.",
                    List.of(),
                    "Export the PDF straight from Word or Google Docs (File > Download > PDF) instead of scanning it.");
        }
        int words = doc.text().trim().split("\\s+").length;
        return CheckResult.pass("text-layer", title, words + " words of selectable text found.");
    }
}
