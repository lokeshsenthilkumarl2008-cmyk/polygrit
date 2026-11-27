# PolyglotMesh Backend

## What is integrated

The backend is a single Spring Boot application based on the `role-2-graalvm-engine` branch:

- `POST /api/execute` runs Java, JavaScript, or Python and returns the execution result.
- `GET /api/execute/languages` and `GET /api/execute/languages/{language}` expose supported-language metadata.
- `GET /api/test` and `GET /api/service-test` retain Developer 1's backend/service smoke endpoints.
- Every execution is saved to MongoDB.
- `GET /api/history`, `GET /api/history/{id}`, and `DELETE /api/history/{id}` expose the existing execution-history feature.

The frontend currently calls only `POST /api/execute`. Its metrics and run counts are still held in frontend state. History browsing and language metadata are available in the backend but are pending frontend work.

## Source branch integration

- `role-2-graalvm-engine`: supplied the runnable Spring MVC API, GraalVM Java/JavaScript/Python executors, configuration, and executor tests.
- `developer1-backend`: supplied the MongoDB execution-history entity and repository. Its separate WebFlux API and pending execution-engine stub were not copied as a second application; history persistence is wired into the GraalVM execution service instead.
- `main`: supplied the Docker, security, resource-limit, and runtime-management classes and their tests. They are retained under `com.polyglotmesh.docker`, `model`, `resource`, `runtime`, and `security`; they are not yet wired into the HTTP execution service. The Docker manager currently creates restricted containers but does not execute submitted code in them.

The backend response uses the GraalVM model's `COMPLETED`/`FAILED` statuses and `executionTime` field. The frontend API adapter normalizes those to its `SUCCESS`/`ERROR` and `executionTimeMs` types.

## Run locally (PowerShell)

Start the MongoDB service from the repository root:

```powershell
docker compose -f backend/docker-compose.yml up -d mongodb
```

In a separate terminal, start the backend:

```powershell
Set-Location backend
.\mvnw.cmd spring-boot:run
```

MongoDB defaults to `mongodb://localhost:27017/polyglotmesh`. Set `MONGODB_URI` before starting Spring Boot to use a different MongoDB instance. The backend binds to `127.0.0.1:8080` and allows browser requests from the Vite development origins `localhost:5173` and `127.0.0.1:5173`.

Build and test:

```powershell
Set-Location backend
.\mvnw.cmd test
```

## Frontend connection

From another terminal:

```powershell
Set-Location frontend
npm install
Copy-Item .env.example .env
npm run dev
```

Keep `VITE_API_URL=http://localhost:8080` and `VITE_USE_MOCK=false` in `frontend/.env`. Restart Vite after changing environment variables.

## Integration boundary and safety

The history endpoints are not connected to a frontend history view yet. The retained main-branch runtime/security components and Docker helper are also not part of the current HTTP execution path.

The current Java executor starts `javac` and `java` as local child processes. The GraalVM guest contexts restrict host access for JavaScript and Python, but guest memory and CPU limits are not enforced by the current HTTP execution path. The retained runtime module uses Graal's `CONSTRAINED` policy; its memory/thread limits are not available in the Community runtime used here. This application is a local development integration, not a production-grade multi-tenant code-execution service. Do not expose it to untrusted users or bind it to a public interface until the Docker sandbox is implemented and the execution boundary is independently reviewed.

Database-backed execution requests require MongoDB to be available: if persistence fails, the API returns `503` rather than silently reporting an unrecorded run as successful.
