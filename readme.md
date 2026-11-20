# PolyglotMesh – Security & Runtime Management

## Developer 3: Environment Setup Guide

This document explains how to set up the development environment for the Security & Runtime Management module of the PolyglotMesh project.

## 1. Project Overview

PolyglotMesh is a multi-language runtime sandbox designed to execute guest-language programs securely.

### Developer 3 Responsibilities

- Execution timeout management
- Memory and CPU resource limits
- Sandbox isolation
- Host access restrictions
- Runtime resource monitoring
- Docker-based container isolation

### Technologies Used

| Technology | Purpose |
|---|---|
| Java | Backend development |
| GraalVM | Multi-language execution and sandboxing |
| Docker | Container isolation |
| WSL 2 | Linux environment for Docker |
| Maven | Dependency management |
| VS Code | Development environment |

---

## 2. System Requirements

- Windows 10 or Windows 11 (supported version)
- Administrator access
- Internet connection
- Minimum 8 GB RAM recommended
- VS Code
- Java-compatible development environment

## 3. Install GraalVM

### Step 1: Download GraalVM

1. Visit https://www.graalvm.org/downloads/
2. Select the GraalVM JDK version required by the project.
3. Select Windows as the operating system.
4. Select x64 architecture for most Intel/AMD computers.
5. Download the JDK ZIP package.

### Step 2: Extract GraalVM

Extract the downloaded ZIP file into:

```text
C:\Java\
```

Example installation directory:

```text
C:\Java\graalvm-jdk-25\
```

The actual folder name depends on the downloaded version.

### Step 3: Configure Environment Variables

Open PowerShell and configure `JAVA_HOME`:

```powershell
[Environment]::SetEnvironmentVariable(
    "JAVA_HOME",
    "C:\Java\graalvm-jdk-25",
    "User"
)
```

Add GraalVM to the user PATH:

```powershell
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
$graalBin = "C:\Java\graalvm-jdk-25\bin"

if (($currentPath -split ';') -notcontains $graalBin) {
    [Environment]::SetEnvironmentVariable(
        "Path",
        "$currentPath;$graalBin",
        "User"
    )
}
```

Replace the example path with your actual installation directory.

Close and reopen PowerShell.

### Step 4: Verify GraalVM

```powershell
echo $env:JAVA_HOME
java -version
javac -version
where.exe java
```

Verify that the Java version output identifies GraalVM.

---

## 4. Install WSL 2

WSL 2 provides the Linux environment used by Docker Desktop.

### Step 1: Open PowerShell as Administrator

Run:

```powershell
wsl --install
```

Restart your computer if Windows requests it.

### Step 2: Update WSL

```powershell
wsl --update
```

### Step 3: Verify WSL

```powershell
wsl --version
wsl --status
```

### Troubleshooting: WSL Command Not Found

If PowerShell displays an error such as:

```text
wsl is not recognized as the name of a cmdlet
```

Try the following:

1. Open Windows Settings.
2. Navigate to System → Optional Features → More Windows Features.
3. Enable:
   - Windows Subsystem for Linux
   - Virtual Machine Platform
4. Click OK.
5. Restart Windows.
6. Open PowerShell as Administrator.
7. Run:

```powershell
wsl --install
```

If the command is still unavailable, update Windows and check whether your Windows version supports WSL.

---

## 5. Install Docker Desktop

### Step 1: Download Docker Desktop

Visit:

https://docs.docker.com/desktop/setup/install/windows-install/

Download Docker Desktop for Windows.

Choose the installer appropriate for your computer architecture.

### Step 2: Install Docker

1. Run the Docker Desktop installer.
2. Select the WSL 2 backend when offered.
3. Complete the installation.
4. Restart Windows if requested.
5. Open Docker Desktop.
6. Wait until Docker Engine is running.

### Step 3: Verify Docker

Open a new PowerShell terminal:

```powershell
docker --version
docker compose version
docker version
```

### Step 4: Test Docker Installation

Run:

```powershell
docker run hello-world
```

Expected result:

```text
Hello from Docker!
```

This confirms that Docker can download and run a test container.

---

## 6. Test Docker Resource Restrictions

Run the following command:

```powershell
docker run --rm `
  --network none `
  --memory=256m `
  --cpus=0.5 `
  alpine:latest `
  echo "PolyglotMesh restricted container working!"
```

Expected output:

```text
PolyglotMesh restricted container working!
```

This test verifies that Docker can launch a container with:

- Network access disabled
- Memory limited to 256 MB
- CPU limited to 0.5 CPU

Note: This is a Docker configuration test. The PolyglotMesh Java application must still be integrated with Docker to enforce these restrictions during actual guest execution.

---

## 7. Verify the Complete Development Environment

Run the following commands in PowerShell:

```powershell
java -version
javac -version
mvn -version
wsl --version
docker --version
docker compose version
```

All commands should execute successfully.

---

## 8. Developer 3 Implementation Checklist

| No. | Task | Status |
|---|---|---|
| 1 | Install GraalVM | Pending |
| 2 | Configure JAVA_HOME and PATH | Pending |
| 3 | Install WSL 2 | Pending |
| 4 | Install Docker Desktop | Pending |
| 5 | Verify Docker Engine | Pending |
| 6 | Configure GraalVM sandbox execution | Pending |
| 7 | Implement execution timeout management | Pending |
| 8 | Implement memory and CPU limits | Pending |
| 9 | Configure host access restrictions | Pending |
| 10 | Implement sandbox isolation | Pending |
| 11 | Implement runtime resource monitoring | Pending |
| 12 | Integrate security module with the main execution pipeline | Pending |

---

## 9. Expected Final Architecture

The Developer 3 security module should work as part of the PolyglotMesh execution pipeline.

```text
        User Code
            |
            v
     Execution Manager
            |
            v
     Security Manager
            |
      +-----+------+
      |            |
      v            v
  GraalVM        Docker
  Sandbox       Container
      |            |
      +-----+------+
            |
            v
   Resource Monitoring
            |
            v
     Execution Result
```

The architecture is a high-level target. Actual execution and isolation responsibilities must be implemented and integrated into the existing project.

---

## 10. Useful Official Documentation

- GraalVM Downloads: https://www.graalvm.org/downloads/
- GraalVM Sandboxing: https://www.graalvm.org/latest/security-guide/sandboxing/
- Docker Desktop Installation: https://docs.docker.com/desktop/setup/install/windows-install/
- WSL Installation: https://learn.microsoft.com/en-us/windows/wsl/install
- Maven Documentation: https://maven.apache.org/guides/

---

## Conclusion

This setup prepares the Windows development environment for the PolyglotMesh Security & Runtime Management module.

The next step is to configure GraalVM sandbox policies, implement execution limits, integrate Docker isolation, and connect runtime monitoring with the main execution pipeline.