import type { RunRecord } from "../types";

export interface MetricsStats {
  totalRuns: number;
  successRate: number; // 0 to 100
  avgMs: number;
  fastestMs: number | null;
  slowestMs: number | null;
  lastMs: number | null;
}

export function computeStats(runs: RunRecord[]): MetricsStats {
  if (runs.length === 0) {
    return {
      totalRuns: 0,
      successRate: 0,
      avgMs: 0,
      fastestMs: null,
      slowestMs: null,
      lastMs: null,
    };
  }

  const times = runs.map((r) => r.executionTimeMs);
  const successCount = runs.filter((r) => r.status === "SUCCESS").length;
  const sum = times.reduce((a, b) => a + b, 0);

  return {
    totalRuns: runs.length,
    successRate: Math.round((successCount / runs.length) * 100),
    avgMs: Math.round(sum / runs.length),
    fastestMs: Math.min(...times),
    slowestMs: Math.max(...times),
    lastMs: times[times.length - 1],
  };
}