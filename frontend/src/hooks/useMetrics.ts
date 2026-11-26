import { useState } from "react";
import type { ExecutionStatus, Language, RunRecord } from "../types";

let runId = 0;

export function useMetrics() {
  const [runs, setRuns] = useState<RunRecord[]>([]);

  const recordRun = (
    language: Language,
    status: ExecutionStatus,
    executionTimeMs: number
  ) => {
    setRuns((prev) => [
      ...prev,
      { id: ++runId, language, status, executionTimeMs, timestamp: Date.now() },
    ]);
  };

  const clearRuns = () => setRuns([]);

  return { runs, recordRun, clearRuns };
}