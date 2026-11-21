import { useState } from "react";
import CodeEditor from "./components/CodeEditor";
import Console from "./components/Console";
import Header from "./components/Header";
import SplitPane from "./components/SplitPane";
import StatusBar from "./components/StatusBar";
import { LANGUAGES } from "./constants/languages";
import { executeCode } from "./services/api";
import type { ConsoleLine, Language } from "./types";

interface EditorTab {
  id: string;
  name: string;
  language: Language;
  content: string;
}

const createTab = (language: Language): EditorTab => ({
  id: `${language}-1`,
  name: `main.${LANGUAGES[language as keyof typeof LANGUAGES].ext}`,
  language,
  content: LANGUAGES[language as keyof typeof LANGUAGES].starter,
});

let lineId = 0;

export default function App() {
  const [tab, setTab] = useState<EditorTab>(createTab("python"));
  const [cursor, setCursor] = useState({ line: 1, column: 1 });
  const [lines, setLines] = useState<ConsoleLine[]>([]);
  const [isRunning, setIsRunning] = useState(false);

  const addLine = (type: ConsoleLine["type"], text: string) => {
    setLines((prev) => [...prev, { id: ++lineId, type, text, timestamp: Date.now() }]);
  };

  const handleLanguageChange = (language: Language) => {
    const untouched = tab.content === LANGUAGES[tab.language as keyof typeof LANGUAGES].starter;
    if (untouched) {
      setTab(createTab(language));
    } else {
      setTab({ ...tab, language, name: `main.${LANGUAGES[language as keyof typeof LANGUAGES].ext}` });
    }
    setCursor({ line: 1, column: 1 });
  };

  const handleRun = async () => {
    setIsRunning(true);
    addLine("info", `> Running ${tab.name}...`);

    try {
      // MOCK: abhi ye fake executeCode call hota hai (services/api.ts).
      // Day 4 mein api.ts real backend se connect hoga, is file mein change nahi chahiye.
      const res = await executeCode({ language: tab.language, code: tab.content });

      if (res.output) addLine("output", res.output);

      if (res.status === "SUCCESS") {
        addLine("info", `Finished in ${res.executionTimeMs} ms`);
      } else if (res.status === "TIMEOUT") {
        addLine("error", `Timeout: ${res.error ?? "possible infinite loop"}`);
      } else if (res.status === "MEMORY_EXCEEDED") {
        addLine("error", `Memory limit exceeded: ${res.error ?? ""}`);
      } else {
        addLine("error", res.error ?? "Unknown error");
      }
    } catch (err) {
      addLine("error", `Request failed: ${err instanceof Error ? err.message : "unknown error"}`);
    } finally {
      setIsRunning(false);
    }
  };

  return (
    <div className="ide">
      <Header
        language={tab.language as Language}
        onLanguageChange={handleLanguageChange}
        onRun={handleRun}
        isRunning={isRunning}
      />
      <main className="editor-area">
        <SplitPane
          top={
            <>
              <div className="file-tab">{tab.name}</div>
              <div className="editor-wrapper">
                <CodeEditor
                  tab={tab}
                  onChange={(content) => setTab({ ...tab, content })}
                  onCursorChange={(line, column) => setCursor({ line, column })}
                />
              </div>
            </>
          }
          bottom={<Console lines={lines} onClear={() => setLines([])} />}
        />
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