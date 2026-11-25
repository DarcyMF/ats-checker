import { useState, type ReactElement } from "react";
import type { CheckResult, KeywordReport, Report } from "./types";

const MARK = { PASS: "✓", WARN: "!", FAIL: "✕" } as const;

export default function Results({ report }: { report: Report }) {
  const problems = report.checks.filter((c) => c.status !== "PASS");
  const passed = report.checks.filter((c) => c.status === "PASS");
  // worst first
  problems.sort((a, b) => (a.status === b.status ? 0 : a.status === "FAIL" ? -1 : 1));

  return (
    <div className="report">
      <div className="verdict">
        <p className="verdict-main">
          {problems.length === 0
            ? "No problems found. An ATS should read this fine."
            : `${problems.length} ${problems.length === 1 ? "thing" : "things"} to fix`}
        </p>
        <p className="small muted">
          {report.pages} {report.pages === 1 ? "page" : "pages"} · {passed.length} of {report.checks.length} checks
          passed
        </p>
      </div>

      {problems.length > 0 && (
        <ul className="checks">
          {problems.map((c) => (
            <Row key={c.id} check={c} />
          ))}
        </ul>
      )}

      {report.keywords && <Keywords k={report.keywords} />}

      <details className="passed" open={problems.length === 0}>
        <summary>Passed ({passed.length})</summary>
        <ul className="checks">
          {passed.map((c) => (
            <Row key={c.id} check={c} />
          ))}
        </ul>
      </details>

      <ExtractedText text={report.text} />
    </div>
  );
}

function Row({ check }: { check: CheckResult }) {
  return (
    <li className={`check ${check.status.toLowerCase()}`}>
      <span className="mark" aria-label={check.status.toLowerCase()}>
        {MARK[check.status]}
      </span>
      <div>
        <p className="check-title">{check.title}</p>
        <p className="check-msg">{check.message}</p>
        {check.status !== "PASS" && check.details.length > 0 && (
          <p className="chips">
            {check.details.map((d) => (
              <code key={d}>{d}</code>
            ))}
          </p>
        )}
        {check.fix && (
          <p className="fix">
            <b>Fix:</b> {check.fix}
          </p>
        )}
      </div>
    </li>
  );
}

function Keywords({ k }: { k: KeywordReport }) {
  const total = k.found.length + k.broken.length + k.missing.length;
  if (total === 0) {
    return (
      <div className="keywords">
        <h3>Job ad skills</h3>
        <p className="small muted">Couldn't find any technical skills in that ad to check against.</p>
      </div>
    );
  }
  return (
    <div className="keywords">
      <div className="kw-head">
        <h3>Job ad skills</h3>
        <p>
          <b>{k.found.length}</b> of {total} found · {k.score}%
        </p>
      </div>
      <div className="meter" role="img" aria-label={`${k.score}% of the job ad's skills found`}>
        <span style={{ width: `${k.score}%` }} />
      </div>
      <div className="kw-cols">
        <KwList title="Found" items={k.found} cls="pass" />
        <KwList title="Missing" items={k.missing} cls="fail" />
        {k.broken.length > 0 && <KwList title="In your resume but unreadable" items={k.broken} cls="warn" />}
      </div>
      {k.missing.length > 0 && (
        <p className="small muted">
          Only add a missing skill if you've really used it. Recruiters ask about everything on your resume.
        </p>
      )}
    </div>
  );
}

function KwList({ title, items, cls }: { title: string; items: string[]; cls: string }) {
  return (
    <div className={`kw ${cls}`}>
      <p className="kw-title">
        {title} ({items.length})
      </p>
      {items.length ? (
        <ul>
          {items.map((s) => (
            <li key={s}>{s}</li>
          ))}
        </ul>
      ) : (
        <p className="small muted">None</p>
      )}
    </div>
  );
}

// joined letters | icon characters | E X P E R I E N C E
const PROBLEM = /([ﬀ-ﬆ])|([-�])|((?<![\p{L}\p{N}])(?:\p{L} ){3,}\p{L}(?![\p{L}\p{N}]))/gu;

function ExtractedText({ text }: { text: string }) {
  const [copied, setCopied] = useState(false);
  const lines = text.replace(/\n+$/, "").split("\n");

  return (
    <div className="extracted">
      <div className="ex-head">
        <h3>What the ATS reads</h3>
        <button
          type="button"
          className="ghost"
          onClick={() => {
            navigator.clipboard.writeText(text).then(() => {
              setCopied(true);
              setTimeout(() => setCopied(false), 1500);
            });
          }}
        >
          {copied ? "Copied" : "Copy text"}
        </button>
      </div>
      <p className="small muted">
        The plain text from your PDF, in the order a basic parser reads it. Problems are highlighted.
      </p>
      <pre>
        {lines.map((line, i) => (
          <span className="ln" key={i}>
            {highlight(line)}
            {"\n"}
          </span>
        ))}
      </pre>
    </div>
  );
}

function highlight(line: string) {
  const out: (string | ReactElement)[] = [];
  let last = 0;
  for (const m of line.matchAll(PROBLEM)) {
    const at = m.index ?? 0;
    if (at > last) out.push(line.slice(last, at));
    const kind = m[1] ? "Joined letters" : m[2] ? "Unreadable icon" : "Spaced-out letters";
    out.push(
      <mark key={at} title={kind}>
        {m[2] ? "□" : m[0]}
      </mark>,
    );
    last = at + m[0].length;
  }
  if (last < line.length) out.push(line.slice(last));
  return out;
}
