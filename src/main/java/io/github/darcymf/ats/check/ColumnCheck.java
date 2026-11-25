package io.github.darcymf.ats.check;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.darcymf.ats.pdf.ResumeDocument;
import io.github.darcymf.ats.pdf.ResumeDocument.Glyph;
import io.github.darcymf.ats.pdf.ResumeDocument.PageLayout;

/**
 * Looks for a vertical gap that runs down the page with text on both sides.
 * A basic parser reads straight across that gap and mixes lines from the two columns.
 *
 * The page is cut into thin vertical strips, and for each strip we count how many lines of text
 * cross it. In a one-column resume most lines cross the middle, so every strip there is busy.
 * In a two-column resume almost nothing crosses the gap between the columns, except maybe a
 * full-width name and contact line at the top. Dates pushed to the right edge don't count,
 * because the bullet points below them still cross the middle.
 */
public class ColumnCheck implements Check {

    static final int STRIPS = 120;
    /** Pages with fewer characters than this don't have enough text to judge. */
    static final int MIN_GLYPHS = 150;
    static final int MIN_LINES = 10;
    /** A strip is a gap if no more than this share of lines cross it (leaves room for a header). */
    static final double MAX_CROSSING = 0.15;
    /** Each side of the gap needs at least this share of the page's text. */
    static final double MIN_SIDE_SHARE = 0.12;
    /** Characters within this many points of each other vertically are on the same line. */
    static final float LINE_TOLERANCE = 2f;

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

        // which strips each line of text touches
        Map<Integer, BitSet> lines = new HashMap<>();
        int[] startsIn = new int[STRIPS];
        for (Glyph g : glyphs) {
            int from = strip(g.x(), page.width());
            int to = strip(g.x() + Math.max(g.width(), 0.1f), page.width());
            lines.computeIfAbsent(Math.round(g.y() / LINE_TOLERANCE), k -> new BitSet(STRIPS)).set(from, to + 1);
            startsIn[from]++;
        }
        if (lines.size() < MIN_LINES) {
            return false;
        }

        int[] crossing = new int[STRIPS];
        BitSet any = new BitSet(STRIPS);
        for (BitSet line : lines.values()) {
            any.or(line);
            for (int s = line.nextSetBit(0); s >= 0; s = line.nextSetBit(s + 1)) {
                crossing[s]++;
            }
        }
        int left = any.nextSetBit(0);
        int right = any.length() - 1;
        if (right - left < 10) {
            return false;
        }

        double gapBelow = lines.size() * MAX_CROSSING;
        int margin = (int) ((right - left) * 0.15);
        // look for two quiet strips side by side, away from the edges
        for (int s = left + margin; s < right - margin; s++) {
            if (crossing[s] <= gapBelow && crossing[s + 1] <= gapBelow) {
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
}
