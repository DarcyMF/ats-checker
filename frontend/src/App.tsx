import { useRef, useState } from "react";
import { checkResume } from "./api";
import type { Report } from "./types";
import Upload from "./Upload";
import Results from "./Results";

const REPO = "https://github.com/DarcyMF/ats-checker";
const SAMPLE = "/samples/two-column-resume.pdf";

export default function App() {
  const [file, setFile] = useState<File | null>(null);
  const [jobAd, setJobAd] = useState("");
  const [report, setReport] = useState<Report | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const resultsRef = useRef<HTMLElement>(null);

  async function run(f: File, ad: string) {
    setLoading(true);
    setError(null);
    try {
      setReport(await checkResume(f, ad));
      // on phones the results are below the form, so jump to them
      if (window.matchMedia("(max-width: 860px)").matches) {
        requestAnimationFrame(() => resultsRef.current?.scrollIntoView({ behavior: "smooth" }));
      }
    } catch (e) {
      setReport(null);
      setError(e instanceof Error ? e.message : "Something went wrong.");
    } finally {
      setLoading(false);
    }
  }

  async function trySample() {
    const res = await fetch(SAMPLE);
    const sample = new File([await res.blob()], "two-column-resume.pdf", { type: "application/pdf" });
    const ad = "Graduate software engineer. Java, Spring Boot, Docker, AWS, Python and Kubernetes.";
    setFile(sample);
    setJobAd(ad);
    run(sample, ad);
  }

  return (
    <>
      <header className="top">
        <div className="wrap top-in">
          <a className="brand" href="/">
            <span className="brand-mark" aria-hidden="true" />
            ATS Checker
          </a>
          <nav>
            <a href="#how">What it checks</a>
            <a href={REPO}>Source</a>
          </nav>
        </div>
      </header>

      <main>
        <section className="intro wrap">
          <h1>See your resume the way hiring software reads it.</h1>
          <p className="lede">
            Most applications go through an applicant tracking system (ATS) before a person sees them. It turns
            your PDF into plain text and searches it for skills. Upload yours to see that text, what breaks, and
            which skills from a job ad it can find.
          </p>
        </section>

        <section className="tool wrap">
          <form
            className="panel inputs"
            onSubmit={(e) => {
              e.preventDefault();
              if (file) run(file, jobAd);
            }}
          >
            <Upload file={file} onFile={setFile} />

            <label className="field">
              <span className="field-label">
                Job ad <span className="optional">optional</span>
              </span>
              <textarea
                value={jobAd}
                onChange={(e) => setJobAd(e.target.value)}
                rows={9}
                placeholder="Paste the whole job ad to see which of its skills your resume covers."
              />
            </label>

            <button className="primary" type="submit" disabled={!file || loading}>
              {loading ? "Checking..." : "Check my resume"}
            </button>
            <p className="small muted">
              Your file is read in memory and thrown away. Nothing is saved.{" "}
              <button type="button" className="link" onClick={trySample} disabled={loading}>
                Try a bad example
              </button>
            </p>
          </form>

          <section className="panel results" ref={resultsRef} aria-live="polite">
            {error && (
              <p className="error" role="alert">
                {error}
              </p>
            )}
            {report ? <Results report={report} /> : !error && <Empty />}
          </section>
        </section>

        <section className="how wrap" id="how">
          <h2>What it checks</h2>
          <table>
            <tbody>
              {HOW.map(([what, why]) => (
                <tr key={what}>
                  <th scope="row">{what}</th>
                  <td>{why}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <h2>Why I made this</h2>
          <p className="prose">
            I ran my own resume through a PDF text extractor and found my name split in two and "fi" turned into
            a single symbol, so a search for "firmware" missed it. It looked fine on screen. Paid checkers give you
            a score; I wanted to see the actual text. This reads PDFs the way a basic parser does. Real ATSs vary,
            and the good ones are smarter, so if this reads your resume correctly, they will too.
          </p>
        </section>
      </main>

      <footer className="foot">
        <div className="wrap foot-in">
          <span>Built by Darcy Franklin with Java, Spring Boot, PDFBox and React.</span>
          <a href={REPO}>View the source on GitHub</a>
        </div>
      </footer>
    </>
  );
}

function Empty() {
  return (
    <div className="empty">
      <p className="empty-title">Results show up here</p>
      <ol>
        <li>Choose your resume PDF</li>
        <li>Paste a job ad if you have one</li>
        <li>See what the ATS reads, and what to fix</li>
      </ol>
    </div>
  );
}

const HOW: [string, string][] = [
  ["Readable text", "Scanned or image-only PDFs have no text, so the ATS gets a blank page."],
  ["Name at the top", "Parsers usually take the first line as your name. A contact line or logo first gets it wrong."],
  ["Contact details", "Your email and phone need to be plain text, not icons, images or the page header."],
  ["Columns", "A basic parser reads straight across the page, mixing lines from a sidebar into your experience."],
  ["Joined letters", "Fonts like Calibri join \"fi\" and \"fl\" into one character, which can break keyword search."],
  ["Icons", "Icon fonts come out as unreadable characters."],
  ["Spaced-out letters", "Wide letter spacing turns EXPERIENCE into E X P E R I E N C E."],
  ["Section headings", "Standard headings like Experience, Education and Skills tell the ATS where each part starts."],
  ["Job ad skills", "Recruiters filter by skills. It lists which ones from the ad your resume has and which it's missing."],
];
