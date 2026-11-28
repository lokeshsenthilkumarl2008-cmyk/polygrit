import MetricCard from "./MetricCard";
import type { MetricsStats } from "../utils/metrics";
import type { RunRecord } from "../types";

interface Props {
  runs: RunRecord[];
  stats: MetricsStats;
  onClear: () => void;
}

const ms = (value: number | null) => (value === null ? "-" : `${value} ms`);

const formatTime = (ts: number) =>
  new Date(ts).toLocaleTimeString("en-GB", { hour12: false });

export default function MetricsPanel({ runs, stats, onClear }: Props) {
  if (runs.length === 0) {
    return (
      <div className="console-empty metrics-empty">
        No runs yet. Run some code to see execution metrics here.
      </div>
    );
  }

  const successTone = stats.successRate >= 80 ? "good" : stats.successRate < 50 ? "bad" : "default";
  const recent = [...runs].reverse().slice(0, 5);

  return (
    <div className="metrics-panel">
      <div className="metrics-grid">
        <MetricCard label="Last run" value={ms(stats.lastMs)} />
        <MetricCard label="Average" value={ms(stats.avgMs)} />
        <MetricCard label="Fastest" value={ms(stats.fastestMs)} tone="good" />
        <MetricCard label="Slowest" value={ms(stats.slowestMs)} tone="bad" />
        <MetricCard label="Total runs" value={String(stats.totalRuns)} />
        <MetricCard label="Success rate" value={`${stats.successRate}%`} tone={successTone} />
      </div>

      <div className="metrics-recent-header">
        <span>RECENT RUNS</span>
        <button className="clear-btn" onClick={onClear}>
          Reset metrics
        </button>
      </div>

      <table className="metrics-table">
        <thead>
          <tr>
            <th>Time</th>
            <th>Language</th>
            <th>Status</th>
            <th>Duration</th>
          </tr>
        </thead>
        <tbody>
          {recent.map((r) => (
            <tr key={r.id}>
              <td>{formatTime(r.timestamp)}</td>
              <td>{r.language}</td>
              <td className={`status-${r.status.toLowerCase()}`}>{r.status}</td>
              <td>{r.executionTimeMs} ms</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}