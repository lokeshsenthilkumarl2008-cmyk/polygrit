import { useState } from "react";
import CodeEditor from "./components/CodeEditor";
import Header from "./components/Header";
import StatusBar from "./components/StatusBar";
import { LANGUAGES } from "./constants/languages";
import type { EditorTab, Language } from "./types/types";

const createTab = (language: Language): EditorTab => ({
  id: `${language}-1`,
  name: `main.${LANGUAGES[language].ext}`,
  language,
  content: LANGUAGES[language].starter,
});

export default function App() {
  const [tab, setTab] = useState<EditorTab>(createTab("python"));
  const [cursor, setCursor] = useState({ line: 1, column: 1 });

  const handleLanguageChange = (language: Language) => {
    const untouched = tab.content === LANGUAGES[tab.language].starter;
    if (untouched) {
      setTab(createTab(language));
    } else {
      setTab({ ...tab, language, name: `main.${LANGUAGES[language].ext}` });
    }
    setCursor({ line: 1, column: 1 });
  };

  return (
    <div className="ide">
      <Header language={tab.language} onLanguageChange={handleLanguageChange} />
      <main className="editor-area">
        <div className="file-tab">{tab.name}</div>
        <div className="editor-wrapper">
          <CodeEditor
            tab={tab}
            onChange={(content) => setTab({ ...tab, content })}
            onCursorChange={(line, column) => setCursor({ line, column })}
          />
        </div>
      </main>
      <StatusBar
        language={tab.language}
        fileName={tab.name}
        line={cursor.line}
        column={cursor.column}
      />
    </div>
  );
}