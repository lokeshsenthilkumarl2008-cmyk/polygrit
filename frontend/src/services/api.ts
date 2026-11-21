import type { ExecuteRequest, ExecuteResponse } from "../types";

// MOCK API: Day 3 ke liye fake backend.
// Day 4 mein is poori function ko real fetch call se replace karna hai:
//   POST {VITE_API_URL}/api/execute  with body { language, code }
export async function executeCode(req: ExecuteRequest): Promise<ExecuteResponse> {
  // MOCK: network delay ki nakal
  await new Promise((resolve) => setTimeout(resolve, 600));

  const code = req.code;

  // MOCK: infinite loop detect karke TIMEOUT dikhana (sandbox validation demo ke liye)
  if (/while\s*\(?\s*(true|True)\s*\)?/.test(code)) {
    return {
      status: "TIMEOUT",
      output: "",
      error: "Execution exceeded the 5000 ms time limit.",
      executionTimeMs: 5000,
    };
  }

  // MOCK: error stack trace dikhana
  if (code.includes("raise") || code.includes("throw")) {
    return {
      status: "ERROR",
      output: "",
      error:
        "Traceback (most recent call last):\n" +
        '  File "main", line 3, in <module>\n' +
        "RuntimeError: mock error from sandbox",
      executionTimeMs: 8,
    };
  }

  // MOCK: normal success response
  return {
    status: "SUCCESS",
    output: `Hello from mock (${req.language})\n`,
    error: null,
    executionTimeMs: 12,
  };
}