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
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [lines]);

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(lines.map((l) => l.text).join("\n"));
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    } catch {
      setCopied(false);
    }
  };

  return (
    <div className="console">
      <div className="console-header">
        <span>CONSOLE OUTPUT</span>
        <span className="console-actions">
          <button className="clear-btn" onClick={handleCopy} disabled={lines.length === 0}>
            {copied ? "Copied!" : "Copy"}
          </button>
          <button className="clear-btn" onClick={onClear}>
            Clear
          </button>
        </span>
      </div>
      <div className="console-body">
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