import { useState } from "react";
import Console from "./Console";
import MetricsPanel from "./MetricsPanel";
import type { MetricsStats } from "../utils/metrics";
import type { ConsoleLine, RunRecord } from "../types";

interface Props {
  lines: ConsoleLine[];
  onClearConsole: () => void;
  runs: RunRecord[];
  stats: MetricsStats;
  onClearMetrics: () => void;
}

type PanelTab = "console" | "metrics";

export default function BottomPanel({ lines, onClearConsole, runs, stats, onClearMetrics }: Props) {
  const [active, setActive] = useState<PanelTab>("console");

  return (
    <div className="bottom-panel">
      <div className="panel-tabs">
        <button
          className={`panel-tab ${active === "console" ? "active" : ""}`}
          onClick={() => setActive("console")}
        >
          Console
        </button>
        <button
          className={`panel-tab ${active === "metrics" ? "active" : ""}`}
          onClick={() => setActive("metrics")}
        >
          Metrics
          {runs.length > 0 && <span className="panel-badge">{runs.length}</span>}
        </button>
      </div>
      <div className="panel-content">
        {active === "console" ? (
          <Console lines={lines} onClear={onClearConsole} />
        ) : (
          <MetricsPanel runs={runs} stats={stats} onClear={onClearMetrics} />
        )}
      </div>
    </div>
  );
}