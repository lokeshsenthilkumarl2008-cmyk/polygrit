import { LANGUAGES } from "../constants/languages";
import type { Language } from "../types/types";

interface Props {
  language: Language;
  fileName: string;
  line: number;
  column: number;
}

export default function StatusBar({ language, fileName, line, column }: Props) {
  return (
    <footer className="status-bar">
      <span>{fileName}</span>
      <span className="status-right">
        <span>Ln {line}, Col {column}</span>
        <span>Spaces: 4</span>
        <span>{LANGUAGES[language].label}</span>
      </span>
    </footer>
  );
}