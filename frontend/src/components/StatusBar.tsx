import { LANGUAGES } from "../constants/languages";
import type { Language } from "../types";

interface Props {
  language: Language;
  fileName: string;
  line: number;
  column: number;
  lastRunMs: number | null;
  totalRuns: number;
  avgMs: number;
}

export default function StatusBar({
  language,
  fileName,
  line,
  column,
  lastRunMs,
  totalRuns,
  avgMs,
}: Props) {
  return (
    <footer className="status-bar">
      <span>{fileName}</span>
      <span className="status-right">
        {totalRuns > 0 && (
          <span>
            Runs: {totalRuns} | Avg: {avgMs} ms
          </span>
        )}
        {lastRunMs !== null && <span>Last run: {lastRunMs} ms</span>}
        <span>
          Ln {line}, Col {column}
        </span>
        <span className="status-hide-small">Spaces: 4</span>
        <span>{LANGUAGES[language as keyof typeof LANGUAGES].label}</span>
      </span>
    </footer>
  );
}