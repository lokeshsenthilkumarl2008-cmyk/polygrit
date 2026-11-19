import Editor from "@monaco-editor/react";
import type { EditorTab } from "../types/types";

interface Props {
  tab: EditorTab;
  onChange: (value: string) => void;
}

export default function CodeEditor({ tab, onChange }: Props) {
  return (
    <Editor
      height="100%"
      theme="vs-dark"
      path={tab.id}
      language={tab.language}
      value={tab.content}
      onChange={(v) => onChange(v ?? "")}
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