# PolyglotMesh IDE (Frontend)

A browser-based IDE built with React, TypeScript and Monaco Editor. It lets a developer write Java, Python and JavaScript code in tabs, run it through the backend execution API, and see the result in an output console.

## Features

- Monaco Editor (same editor as VS Code) with syntax highlighting for Java, Python and JavaScript
- Multiple tabs: add, rename (double-click) and close, each with its own code and language
- Language selector with starter code for each language
- Run button and `Ctrl+Enter` shortcut with a loading state
- Resizable split-pane: editor on top, console below
- Output console with timestamps, colored output, error and warning lines, Copy and Clear
- Special messages for timeout (infinite loops) and memory-limit errors
- Status bar with line, column, language and last run time
- Connection, server and request-timeout error handling
- Mock mode to work without the backend

## Tech Stack

React 18+, TypeScript, Vite, `@monaco-editor/react`, plain CSS

## Getting Started

```bash
cd frontend
npm install
cp .env.example .env     # on Windows: copy .env.example .env
npm run dev
```

Open http://localhost:5173

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8080` | Base URL of the Spring Boot backend |
| `VITE_USE_MOCK` | `false` | Set to `true` to use the built-in fake API |

Restart `npm run dev` after changing `.env`.

## API Contract

The frontend expects this endpoint from the backend.

`POST {VITE_API_URL}/api/execute`

Request:

```json
{ "language": "python", "code": "print('hi')" }
```

Response:

```json
{
  "status": "SUCCESS",
  "output": "hi\n",
  "error": null,
  "executionTimeMs": 12
}
```

`status` is one of `SUCCESS`, `ERROR`, `TIMEOUT`, `MEMORY_EXCEEDED`. The backend must allow CORS for `http://localhost:5173`.

The API adapter in `src/services/api.ts` normalizes backend status and timing field names to this frontend contract.

## Mock Mode

With `VITE_USE_MOCK=true` the app runs without a backend:

| Code contains | Result |
|---|---|
| anything normal | success output |
| `while True` / `while(true)` | TIMEOUT |
| `raise` or `throw` | ERROR with stack trace |
| `MEMORY_TEST` | MEMORY_EXCEEDED |

## Project Structure

```
src/
  components/   Header, TabBar, CodeEditor, SplitPane, Console, StatusBar, LanguageSelector
  constants/    language config and starter code
  services/     api.ts (backend calls and mock)
  types/        shared TypeScript types
```

## Backend integration status

The run action is connected to the integrated Spring Boot backend. Run counts and metrics remain frontend-local. Backend execution history and supported-language metadata are available but do not yet have frontend views. See [the backend README](../backend/README.md) for setup, API coverage, and current limitations.