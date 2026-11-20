import Editor, { type OnMount } from "@monaco-editor/react";
import type { EditorTab } from "../types/types";

interface Props {
  tab: EditorTab;
  onChange: (value: string) => void;
  onCursorChange: (line: number, column: number) => void;
}

export default function CodeEditor({ tab, onChange, onCursorChange }: Props) {
  const handleMount: OnMount = (editor) => {
    editor.onDidChangeCursorPosition((e) => {
      onCursorChange(e.position.lineNumber, e.position.column);
    });
  };

  return (
    <Editor
      height="100%"
      theme="vs-dark"
      path={tab.id}
      language={tab.language}
      value={tab.content}
      onChange={(v) => onChange(v ?? "")}
      onMount={handleMount}
      options={{
        fontSize: 14,
        minimap: { enabled: false },
        automaticLayout: true,
        scrollBeyondLastLine: false,
        tabSize: 4,
        wordWrap: "on",
      }}
    />
  );
}