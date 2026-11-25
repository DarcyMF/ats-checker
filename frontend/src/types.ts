// Mirrors the JSON from POST /api/check

export type Status = "PASS" | "WARN" | "FAIL";

export interface CheckResult {
  id: string;
  title: string;
  status: Status;
  message: string;
  details: string[];
  fix: string | null;
}

export interface KeywordReport {
  found: string[];
  broken: string[];
  missing: string[];
  score: number;
}

export interface Report {
  text: string;
  pages: number;
  checks: CheckResult[];
  keywords: KeywordReport | null;
}
