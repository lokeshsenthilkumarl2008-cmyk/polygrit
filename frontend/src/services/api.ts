import type { ExecuteRequest, ExecuteResponse, ExecutionStatus } from "../types";

const API_URL = (import.meta.env.VITE_API_URL ?? "http://localhost:8080").replace(/\/+$/, "");
const USE_MOCK = import.meta.env.VITE_USE_MOCK === "true";
const REQUEST_TIMEOUT_MS = 180000;

export class ApiError extends Error {
  kind: "network" | "timeout" | "http" | "invalid";

  constructor(kind: ApiError["kind"], message: string) {
    super(message);
    this.name = "ApiError";
    this.kind = kind;
  }
}

const STATUS_MAP: Record<string, ExecutionStatus> = {
  SUCCESS: "SUCCESS",
  COMPLETED: "SUCCESS",
  ERROR: "ERROR",
  FAILED: "ERROR",
  COMPILATION_ERROR: "ERROR",
  INVALID_REQUEST: "ERROR",
  UNSUPPORTED_LANGUAGE: "ERROR",
  SECURITY_VIOLATION: "ERROR",
  TIMEOUT: "TIMEOUT",
  MEMORY_EXCEEDED: "MEMORY_EXCEEDED",
  RESOURCE_EXHAUSTED: "MEMORY_EXCEEDED",
};

function normalizeResponse(data: unknown): ExecuteResponse {
  if (typeof data !== "object" || data === null || Array.isArray(data)) {
    throw new ApiError("invalid", "Backend returned an invalid response.");
  }

  const response = data as Record<string, unknown>;
  const rawStatus = typeof response.status === "string" ? response.status.toUpperCase() : "";
  const status = STATUS_MAP[rawStatus];
  if (!status || typeof response.output !== "string") {
    throw new ApiError("invalid", "Backend response is missing a valid status or output.");
  }

  const executionTimeMs = response.executionTimeMs ?? response.executionTime;
  if (typeof executionTimeMs !== "number" || !Number.isFinite(executionTimeMs) || executionTimeMs < 0) {
    throw new ApiError("invalid", "Backend response contains an invalid execution time.");
  }
  if (response.error !== null && response.error !== undefined && typeof response.error !== "string") {
    throw new ApiError("invalid", "Backend response contains an invalid error message.");
  }

  return {
    status,
    output: response.output,
    error: typeof response.error === "string" ? response.error : null,
    executionTimeMs,
  };
}

export async function executeCode(req: ExecuteRequest): Promise<ExecuteResponse> {
  if (USE_MOCK) return mockExecute(req);

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

  try {
    const res = await fetch(`${API_URL}/api/execute`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(req),
      signal: controller.signal,
    });

    if (!res.ok) {
      // Backend kabhi error ka body bhi bhejta hai, use dikhane ki koshish karo
      const text = await res.text().catch(() => "");
      throw new ApiError("http", `Server error ${res.status}${text ? `: ${text}` : ""}`);
    }

    return normalizeResponse(await res.json());
  } catch (err) {
    if (err instanceof ApiError) throw err;
    if (err instanceof DOMException && err.name === "AbortError") {
      throw new ApiError("timeout", `Backend ne ${REQUEST_TIMEOUT_MS / 1000}s mein jawab nahi diya.`);
    }
    throw new ApiError(
      "network",
      `Backend se connect nahi ho paya (${API_URL}). Server chal raha hai aur CORS allowed hai?`
    );
  } finally {
    clearTimeout(timer);
  }
}

// MOCK: backend ke bina testing ke liye. VITE_USE_MOCK=true par chalta hai.
async function mockExecute(req: ExecuteRequest): Promise<ExecuteResponse> {
  await new Promise((resolve) => setTimeout(resolve, 600));

  if (/while\s*\(?\s*(true|True)\s*\)?/.test(req.code)) {
    return { status: "TIMEOUT", output: "", error: "Execution exceeded the 5000 ms time limit.", executionTimeMs: 5000 };
  }
  if (req.code.includes("raise") || req.code.includes("throw")) {
    return {
      status: "ERROR",
      output: "",
      error: 'Traceback (most recent call last):\n  File "main", line 3, in <module>\nRuntimeError: mock error',
      executionTimeMs: 8,
    };
  }
  return { status: "SUCCESS", output: `Hello from mock (${req.language})\n`, error: null, executionTimeMs: 12 };
}