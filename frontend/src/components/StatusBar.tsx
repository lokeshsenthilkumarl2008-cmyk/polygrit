import { LANGUAGES } from "../constants/languages";
import type { Language } from "../types";

interface Props {
  language: Language;
  fileName: string;
  line: number;
  column: number;
  lastRunMs: number | null;
}

export default function StatusBar({ language, fileName, line, column, lastRunMs }: Props) {
  return (
    <footer className="status-bar">
      <span>{fileName}</span>
      <span className="status-right">
        {lastRunMs !== null && <span>Last run: {lastRunMs} ms</span>}
        <span>
          Ln {line}, Col {column}
        </span>
        <span className="status-hide-small">Spaces: 4</span>
        <span>{LANGUAGES[language].label}</span>
      </span>
    </footer>
  );
}