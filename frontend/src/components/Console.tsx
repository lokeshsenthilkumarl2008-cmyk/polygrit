import { useEffect, useRef } from "react";
import type { ConsoleLine } from "../types";

interface Props {
  lines: ConsoleLine[];
  onClear: () => void;
}

export default function Console({ lines, onClear }: Props) {
  const endRef = useRef<HTMLDivElement>(null);

  // Naya output aate hi neeche scroll karo
  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [lines]);

  return (
    <div className="console">
      <div className="console-header">
        <span>CONSOLE OUTPUT</span>
        <button className="clear-btn" onClick={onClear}>
          Clear
        </button>
      </div>
      <div className="console-body">
        {lines.length === 0 && (
          <div className="console-empty">Run your code to see output here.</div>
        )}
        {lines.map((line) => (
          <pre key={line.id} className={`console-line ${line.type}`}>
            {line.text}
          </pre>
        ))}
        <div ref={endRef} />
      </div>
    </div>
  );
}