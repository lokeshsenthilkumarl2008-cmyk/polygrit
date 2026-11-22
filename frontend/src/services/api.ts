import type { ExecuteRequest, ExecuteResponse, ExecutionStatus } from "../types";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";
const USE_MOCK = import.meta.env.VITE_USE_MOCK === "true";
const REQUEST_TIMEOUT_MS = 15000;

export class ApiError extends Error {
  kind: "network" | "timeout" | "http" | "invalid";

  constructor(kind: ApiError["kind"], message: string) {
    super(message);
    this.name = "ApiError";
    this.kind = kind;
  }
}

const VALID_STATUSES: ExecutionStatus[] = ["SUCCESS", "ERROR", "TIMEOUT", "MEMORY_EXCEEDED"];

// Backend ka response check karke safe format mein badalta hai.
// Agar Developer 1 ke field names alag hon, to sirf yahan change karo.
function normalizeResponse(data: any): ExecuteResponse {
  if (!data || typeof data !== "object") {
    throw new ApiError("invalid", "Backend ne galat format ka response diya.");
  }
  const status: ExecutionStatus = VALID_STATUSES.includes(data.status) ? data.status : "ERROR";
  return {
    status,
    output: typeof data.output === "string" ? data.output : "",
    error: typeof data.error === "string" ? data.error : null,
    executionTimeMs: typeof data.executionTimeMs === "number" ? data.executionTimeMs : 0,
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