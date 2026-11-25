package io.github.darcymf.ats;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import io.github.darcymf.ats.pdf.ResumeDocument;

/** Builds small PDFs in code so the tests don't need binary files checked in. */
public final class TestPdfs {

    private static final float PAGE_TOP = PDRectangle.A4.getHeight() - 60;
    private static final float LINE = 14;

    private TestPdfs() {
    }

    public static final String FILLER =
            "Built and tested features for a web app used by real customers every day";

    /** A normal one-column resume. */
    public static byte[] singleColumn() {
        List<String> lines = new ArrayList<>(List.of(
                "Jordan Smith",
                "Brisbane QLD | 0412 345 678 | jordan@example.com | linkedin.com/in/jordansmith",
                "Experience"));
        for (int i = 0; i < 10; i++) lines.add("- " + FILLER + " with Java and PostgreSQL " + i);
        lines.add("Education");
        lines.add("Bachelor of Engineering, 2022 - 2025");
        lines.add("Skills");
        lines.add("Java, Python, SQL, Git, Docker");
        return linesAt(50, lines);
    }

    /** Title on the left and dates pushed right, like most resumes. Still one column. */
    public static byte[] rightAlignedDates() {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = PAGE_TOP;
                for (int job = 0; job < 5; job++) {
                    text(cs, 50, y, "Software Developer, Company " + job);
                    text(cs, 480, y, "2023 - 2024");
                    y -= LINE;
                    for (int b = 0; b < 3; b++) {
                        text(cs, 60, y, "- " + FILLER + " and the team " + b);
                        y -= LINE;
                    }
                }
            }
            return save(doc);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Sidebar on the left, main content on the right. */
    public static byte[] twoColumn() {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = PAGE_TOP;
                for (int i = 0; i < 30; i++) {
                    text(cs, 40, y, "Skill number " + i + " here");
                    text(cs, 260, y, "Did some work on a project " + i + " and shipped");
                    y -= LINE;
                }
            }
            return save(doc);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Name and contact line across the full width, then a sidebar. The most common template. */
    public static byte[] twoColumnWithHeader() {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = PAGE_TOP;
                text(cs, 40, y, "Jordan Smith                                        Software Developer                    Brisbane");
                y -= LINE;
                text(cs, 40, y, "0412 345 678 | jordan@example.com | linkedin.com/in/jordansmith | github.com/jordansmith");
                y -= LINE * 2;
                for (int i = 0; i < 25; i++) {
                    text(cs, 40, y, "Skill " + i);
                    text(cs, 200, y, "Did some work on a project " + i + " and shipped it on time");
                    y -= LINE;
                }
            }
            return save(doc);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** What you get from scanning a printed resume: one picture, no text. */
    public static byte[] imageOnly() {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            BufferedImage img = new BufferedImage(200, 280, BufferedImage.TYPE_INT_RGB);
            PDImageXObject image = LosslessFactory.createFromImage(doc, img);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.drawImage(image, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            }
            return save(doc);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] pages(int count) {
        try (PDDocument doc = new PDDocument()) {
            for (int p = 0; p < count; p++) {
                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    text(cs, 50, PAGE_TOP, "Page " + p + " " + FILLER);
                }
            }
            return save(doc);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * A document made straight from text, for checks that only look at the words.
     * Joined letters in the input are kept in rawText and split in text, like PDFBox does.
     */
    public static ResumeDocument fromText(String raw) {
        String clean = Normalizer.normalize(raw, Normalizer.Form.NFKC);
        return new ResumeDocument(clean, raw, List.of(new ResumeDocument.PageLayout(595, 842, List.of())), 0);
    }

    private static byte[] linesAt(float x, List<String> lines) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = PAGE_TOP;
                for (String line : lines) {
                    text(cs, x, y, line);
                    y -= LINE;
                }
            }
            return save(doc);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void text(PDPageContentStream cs, float x, float y, String s) throws IOException {
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        cs.newLineAtOffset(x, y);
        cs.showText(s);
        cs.endText();
    }

    private static byte[] save(PDDocument doc) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        return out.toByteArray();
    }
}
