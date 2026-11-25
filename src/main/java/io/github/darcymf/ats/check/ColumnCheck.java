package io.github.darcymf.ats.check;

import java.util.ArrayList;
import java.util.List;

import io.github.darcymf.ats.pdf.ResumeDocument;
import io.github.darcymf.ats.pdf.ResumeDocument.Glyph;
import io.github.darcymf.ats.pdf.ResumeDocument.PageLayout;

/**
 * Looks for a vertical gap that runs down the middle of the page with text on both sides.
 * A basic parser reads straight across that gap and mixes lines from the two columns.
 *
 * The page is cut into thin vertical strips and we count how many characters touch each strip.
 * In a one-column resume every strip in the middle is busy, because full-width lines cross it.
 * Dates pushed to the right edge don't count, since the bullet points below them still cross
 * the middle.
 */
public class ColumnCheck implements Check {

    static final int STRIPS = 120;
    /** Pages with fewer characters than this don't have enough text to judge. */
    static final int MIN_GLYPHS = 150;
    /** A strip counts as empty if it's this quiet compared to a normal strip. */
    static final double EMPTY_RATIO = 0.08;
    /** Each side of the gap needs at least this share of the page's text. */
    static final double MIN_SIDE_SHARE = 0.12;

    @Override
    public CheckResult run(ResumeDocument doc) {
        List<String> pages = new ArrayList<>();
        for (int i = 0; i < doc.pages().size(); i++) {
            if (hasGutter(doc.pages().get(i))) {
                pages.add("Page " + (i + 1));
            }
        }
        String title = "Layout and reading order";
        if (pages.isEmpty()) {
            return CheckResult.pass("columns", title, "Single column, so it reads top to bottom in the right order.");
        }
        return CheckResult.warn("columns", title,
                "Looks like two columns. A basic parser reads straight across the page, so lines from the two columns get mixed together. Check the text below to see what it read.",
                pages,
                "Use one column. Put skills or contact details in their own section instead of a sidebar.");
    }

    static boolean hasGutter(PageLayout page) {
        List<Glyph> glyphs = page.glyphs();
        if (glyphs.size() < MIN_GLYPHS || page.width() <= 0) {
            return false;
        }
        int[] cover = new int[STRIPS];
        int[] startsIn = new int[STRIPS];
        for (Glyph g : glyphs) {
            int from = strip(g.x(), page.width());
            int to = strip(g.x() + Math.max(g.width(), 0.1f), page.width());
            for (int s = from; s <= to; s++) {
                cover[s]++;
            }
            startsIn[from]++;
        }

        int left = 0;
        while (left < STRIPS && cover[left] == 0) left++;
        int right = STRIPS - 1;
        while (right >= 0 && cover[right] == 0) right--;
        if (right - left < 10) {
            return false;
        }

        double normal = median(cover, left, right);
        double emptyBelow = Math.max(1, normal * EMPTY_RATIO);
        int margin = (int) ((right - left) * 0.15);

        // Look for a run of at least two quiet strips away from the edges.
        for (int s = left + margin; s < right - margin; s++) {
            if (cover[s] <= emptyBelow && cover[s + 1] <= emptyBelow) {
                int before = 0;
                for (int k = left; k < s; k++) before += startsIn[k];
                double leftShare = (double) before / glyphs.size();
                if (leftShare >= MIN_SIDE_SHARE && leftShare <= 1 - MIN_SIDE_SHARE) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int strip(float x, float width) {
        int s = (int) (x / width * STRIPS);
        return Math.max(0, Math.min(STRIPS - 1, s));
    }

    private static double median(int[] values, int from, int to) {
        int[] copy = java.util.Arrays.copyOfRange(values, from, to + 1);
        java.util.Arrays.sort(copy);
        return copy[copy.length / 2];
    }
}
