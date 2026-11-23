import { useEffect, useRef } from "react";
import Editor, { type OnMount } from "@monaco-editor/react";

interface EditorTab {
  id: string;
  language: string;
  content: string;
}

interface Props {
  tab: EditorTab;
  onChange: (value: string) => void;
  onCursorChange: (line: number, column: number) => void;
  onRun: () => void;
}

export default function CodeEditor({ tab, onChange, onCursorChange, onRun }: Props) {
  // Ref rakhne se Monaco hamesha latest onRun call karta hai
  const runRef = useRef(onRun);
  useEffect(() => {
    runRef.current = onRun;
  }, [onRun]);

  const handleMount: OnMount = (editor, monaco) => {
    editor.onDidChangeCursorPosition((e) => {
      onCursorChange(e.position.lineNumber, e.position.column);
    });
    // Ctrl+Enter (Mac par Cmd+Enter) se code run karo
    editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter, () => runRef.current());
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