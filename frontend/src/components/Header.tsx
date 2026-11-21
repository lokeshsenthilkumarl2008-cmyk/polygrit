import LanguageSelector from "./LanguageSelector";
import type { Language } from "../types/types";

interface Props {
  language: Language;
  onLanguageChange: (language: Language) => void;
  onRun: () => void;
  isRunning: boolean;
}

export default function Header({ language, onLanguageChange, onRun, isRunning }: Props) {
  return (
    <header className="header">
      <div className="header-title">
        <span className="logo">&#9670;</span>
        <h1>PolyglotMesh IDE</h1>
      </div>
      <div className="header-actions">
        <label htmlFor="lang">Language</label>
        <LanguageSelector value={language} onChange={onLanguageChange} />
        <button className="run-btn" onClick={onRun} disabled={isRunning}>
          {isRunning ? "Running..." : "\u25B6 Run"}
        </button>
      </div>
    </header>
  );
}