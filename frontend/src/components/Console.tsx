import { useEffect, useRef, useState } from "react";
import type { ConsoleLine } from "../types";

interface Props {
  lines: ConsoleLine[];
  onClear: () => void;
}

const formatTime = (ts: number) =>
  new Date(ts).toLocaleTimeString("en-GB", { hour12: false });

export default function Console({ lines, onClear }: Props) {
  const endRef = useRef<HTMLDivElement>(null);
  const copyResetRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [copyStatus, setCopyStatus] = useState<"idle" | "copied" | "failed">("idle");

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [lines]);

  useEffect(() => () => {
    if (copyResetRef.current) clearTimeout(copyResetRef.current);
  }, []);

  const handleCopy = async () => {
    if (lines.length === 0) return;

    try {
      const output = lines.map((line) => line.text).join("\n");
      await navigator.clipboard.writeText(output);
      setCopyStatus("copied");
    } catch {
      setCopyStatus("failed");
    }

    if (copyResetRef.current) clearTimeout(copyResetRef.current);
    copyResetRef.current = setTimeout(() => setCopyStatus("idle"), 1500);
  };

  const handleClear = () => {
    onClear();
    setCopyStatus("idle");
    if (copyResetRef.current) {
      clearTimeout(copyResetRef.current);
      copyResetRef.current = null;
    }
  };

  return (
    <div className="console">
      <div className="console-header">
        <span>CONSOLE OUTPUT{lines.length > 0 ? ` (${lines.length})` : ""}</span>
        <span className="console-actions" aria-live="polite">
          <button
            className="clear-btn"
            type="button"
            onClick={handleCopy}
            disabled={lines.length === 0}
            aria-label="Copy console output"
            title="Copy console output"
          >
            {copyStatus === "copied" ? "Copied!" : copyStatus === "failed" ? "Copy failed" : "Copy"}
          </button>
          <button
            className="clear-btn"
            type="button"
            onClick={handleClear}
            disabled={lines.length === 0}
            aria-label="Clear console output"
          >
            Clear
          </button>
        </span>
      </div>
      <div className="console-body" role="log" aria-live="polite" aria-label="Console output">
        {lines.length === 0 && (
          <div className="console-empty">Run your code (Ctrl+Enter) to see output here.</div>
        )}
        {lines.map((line) => (
          <div key={line.id} className="console-row">
            <span className="console-time">{formatTime(line.timestamp)}</span>
            <pre className={`console-line ${line.type}`}>{line.text}</pre>
          </div>
        ))}
        <div ref={endRef} />
      </div>
    </div>
  );
}