package io.github.darcymf.ats.check;

import io.github.darcymf.ats.pdf.ResumeDocument;

/** One thing that can go wrong when software reads a resume. */
public interface Check {

    CheckResult run(ResumeDocument doc);
}
