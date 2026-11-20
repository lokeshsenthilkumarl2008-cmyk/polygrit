import { LANGUAGES } from "../constants/languages";
import type { Language } from "../types/types";

interface Props {
  value: Language;
  onChange: (language: Language) => void;
}

export default function LanguageSelector({ value, onChange }: Props) {
  return (
    <select
      className="language-select"
      value={value}
      onChange={(e) => onChange(e.target.value as Language)}
    >
      {(Object.keys(LANGUAGES) as Language[]).map((lang) => (
        <option key={lang} value={lang}>
          {LANGUAGES[lang].label}
        </option>
      ))}
    </select>
  );
}