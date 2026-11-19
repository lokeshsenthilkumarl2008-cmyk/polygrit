import { useState } from "react";
import { LANGUAGES } from "./constants/languages";
import type { EditorTab } from "./types/types";
import CodeEditor from "./components/CodeEditor";
const initialTab: EditorTab = {
  id: "python-1",
  name: `main.${LANGUAGES.python.ext}`,
  language: "python",
  content: LANGUAGES.python.starter,
};

export default function App() {
  const [tab, setTab] = useState<EditorTab>(initialTab);

  return (
    <div style={{ height: "100vh", display: "flex", flexDirection: "column", background: "#1e1e1e" }}>
      <header style={{ padding: "10px 16px", color: "#fff", background: "#333", fontFamily: "sans-serif" }}>
        PolyglotMesh IDE
      </header>
      <div style={{ flex: 1, minHeight: 0 }}>
        <CodeEditor tab={tab} onChange={(content) => setTab({ ...tab, content })} />
      </div>
    </div>
  );
}