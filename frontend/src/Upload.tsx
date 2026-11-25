import { useState } from "react";

interface Props {
  file: File | null;
  onFile: (f: File) => void;
}

export default function Upload({ file, onFile }: Props) {
  const [over, setOver] = useState(false);

  function take(files: FileList | null) {
    const f = files?.[0];
    if (f) onFile(f);
  }

  return (
    <label
      className={`drop${over ? " over" : ""}${file ? " has-file" : ""}`}
      onDragOver={(e) => {
        e.preventDefault();
        setOver(true);
      }}
      onDragLeave={() => setOver(false)}
      onDrop={(e) => {
        e.preventDefault();
        setOver(false);
        take(e.dataTransfer.files);
      }}
    >
      <input
        type="file"
        accept="application/pdf,.pdf"
        className="sr-only"
        onChange={(e) => take(e.target.files)}
      />
      {file ? (
        <>
          <span className="drop-name">{file.name}</span>
          <span className="small muted">
            {formatSize(file.size)} · <u>choose a different file</u>
          </span>
        </>
      ) : (
        <>
          <span className="drop-title">Drop your resume PDF here</span>
          <span className="small muted">
            or <u>choose a file</u> (up to 5 MB)
          </span>
        </>
      )}
    </label>
  );
}

function formatSize(bytes: number) {
  return bytes < 1024 * 1024 ? `${Math.max(1, Math.round(bytes / 1024))} KB` : `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}
