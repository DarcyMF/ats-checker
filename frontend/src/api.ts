import type { Report } from "./types";

export async function checkResume(file: File, jobAd: string): Promise<Report> {
  const form = new FormData();
  form.append("file", file);
  if (jobAd.trim()) form.append("jobAd", jobAd);

  let res: Response;
  try {
    res = await fetch("/api/check", { method: "POST", body: form });
  } catch {
    throw new Error("Couldn't reach the server. Check your connection and try again.");
  }
  if (!res.ok) {
    const body = await res.json().catch(() => null);
    throw new Error(body?.error ?? `Something went wrong (${res.status}). Try again.`);
  }
  return res.json();
}
