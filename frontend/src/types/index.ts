export type ExecutionStatus = "SUCCESS" | "ERROR" | "TIMEOUT" | "MEMORY_EXCEEDED";
export type Language = "java" | "python" | "javascript";

export interface ExecuteRequest {
  language: Language;
  code: string;
}

export interface ExecuteResponse {
  status: ExecutionStatus;
  output: string;
  error: string | null;
  executionTimeMs: number;
}

export interface ConsoleLine {
  id: number;
  type: "output" | "error" | "info" | "warning";
  text: string;
  timestamp: number;
}