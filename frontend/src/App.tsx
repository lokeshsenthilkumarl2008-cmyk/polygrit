import { useState } from "react";
import CodeEditor from "./components/CodeEditor";
import BottomPanel from "./components/BottomPanel";
// import Console from "./components/Console";
import Header from "./components/Header";
import SplitPane from "./components/SplitPane";
import StatusBar from "./components/StatusBar";
import TabBar from "./components/TabBar";
import { useMetrics } from "./hooks/useMetrics";
import { computeStats } from "./utils/metrics";
import { LANGUAGES } from "./constants/languages";
import { ApiError, executeCode } from "./services/api";
import type { ConsoleLine, Language } from "./types";

interface EditorTab {
  id: string;
  name: string;
  language: Language;
  content: string;
}

const extOf = (language: Language) => LANGUAGES[language as keyof typeof LANGUAGES].ext;
const starterOf = (language: Language) => LANGUAGES[language as keyof typeof LANGUAGES].starter;

let tabCounter = 1;
let lineId = 0;

const createTab = (language: Language, name?: string): EditorTab => {
  const id = `tab-${tabCounter}`;
  const defaultName = tabCounter === 1 ? `main.${extOf(language)}` : `script${tabCounter}.${extOf(language)}`;
  tabCounter += 1;
  return { id, name: name ?? defaultName, language, content: starterOf(language) };
};

// "main.py" -> "main.java"
const withExtension = (name: string, ext: string) => `${name.replace(/\.[^.]+$/, "")}.${ext}`;

const firstTab = createTab("python");

export default function App() {
  const [tabs, setTabs] = useState<EditorTab[]>([firstTab]);
  const [activeId, setActiveId] = useState(firstTab.id);
  const [cursor, setCursor] = useState({ line: 1, column: 1 });
  const [lines, setLines] = useState<ConsoleLine[]>([]);
  const [isRunning, setIsRunning] = useState(false);
  const [lastRunMs, setLastRunMs] = useState<number | null>(null);
    const { runs, recordRun, clearRuns } = useMetrics();
  const stats = computeStats(runs);

  const tab = tabs.find((t) => t.id === activeId) ?? tabs[0];

  const updateActive = (patch: Partial<EditorTab>) => {
    setTabs((prev) => prev.map((t) => (t.id === tab.id ? { ...t, ...patch } : t)));
  };

  const addLine = (type: ConsoleLine["type"], text: string) => {
    setLines((prev) => [...prev, { id: ++lineId, type, text, timestamp: Date.now() }]);
  };

  // ---- Tab handlers ----
  const handleAddTab = () => {
    const newTab = createTab(tab.language);
    setTabs((prev) => [...prev, newTab]);
    setActiveId(newTab.id);
    setCursor({ line: 1, column: 1 });
  };

  const handleCloseTab = (id: string) => {
    if (tabs.length === 1) return;
    const index = tabs.findIndex((t) => t.id === id);
    const remaining = tabs.filter((t) => t.id !== id);
    setTabs(remaining);
    if (id === tab.id) {
      setActiveId(remaining[Math.max(0, index - 1)].id);
    }
  };

  const handleRenameTab = (id: string, name: string) => {
    setTabs((prev) => prev.map((t) => (t.id === id ? { ...t, name } : t)));
  };

  const handleSelectTab = (id: string) => {
    setActiveId(id);
    setCursor({ line: 1, column: 1 });
  };

  // ---- Language change (sirf active tab par) ----
  const handleLanguageChange = (language: Language) => {
    const untouched = tab.content === starterOf(tab.language);
    updateActive({
      language,
      name: withExtension(tab.name, extOf(language)),
      // Code nahi badla hai to naye language ka starter code do
      ...(untouched ? { content: starterOf(language) } : {}),
    });
    setCursor({ line: 1, column: 1 });
  };

  // ---- Run ----
  const handleRun = async () => {
    if (isRunning) return;

    if (!tab.content.trim()) {
      addLine("warning", "Editor is empty. Write some code first.");
      return;
    }

    setIsRunning(true);
    addLine("info", `> Running ${tab.name}...`);

    try {
      const res = await executeCode({ language: tab.language, code: tab.content });
      setLastRunMs(res.executionTimeMs);
      recordRun(tab.language, res.status, res.executionTimeMs);

      if (res.output) addLine("output", res.output);

      if (res.status === "SUCCESS") {
        addLine("info", `Finished in ${res.executionTimeMs} ms`);
      } else if (res.status === "TIMEOUT") {
        addLine("warning", `Timeout: ${res.error ?? "possible infinite loop"}`);
      } else if (res.status === "MEMORY_EXCEEDED") {
        addLine("warning", `Memory limit exceeded: ${res.error ?? ""}`);
      } else {
        addLine("error", res.error ?? "Unknown error");
      }
    } catch (err) {
      if (err instanceof ApiError) {
        const label =
          err.kind === "timeout" ? "Timeout" :
          err.kind === "network" ? "Connection error" :
          err.kind === "http" ? "Server error" : "Invalid response";
        addLine("error", `${label}: ${err.message}`);
      } else {
        addLine("error", `Request failed: ${err instanceof Error ? err.message : "unknown error"}`);
      }
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
              <TabBar
                tabs={tabs}
                activeId={tab.id}
                onSelect={handleSelectTab}
                onAdd={handleAddTab}
                onClose={handleCloseTab}
                onRename={handleRenameTab}
              />
              <div className="editor-wrapper">
                <CodeEditor
                  tab={tab}
                  onChange={(content) => updateActive({ content })}
                  onCursorChange={(line, column) => setCursor({ line, column })}
                  onRun={handleRun}
                />
              </div>
            </>
          }
                    bottom={
            <BottomPanel
              lines={lines}
              onClearConsole={() => setLines([])}
              runs={runs}
              stats={stats}
              onClearMetrics={clearRuns}
            />
          }
        />
      </main>
      <StatusBar
        language={tab.language}
        fileName={tab.name}
        line={cursor.line}
        column={cursor.column}
        lastRunMs={lastRunMs}
        totalRuns={stats.totalRuns}
        avgMs={stats.avgMs}
      />
    </div>
  );
}