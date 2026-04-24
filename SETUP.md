# GymBuddy Setup Guide

## Prerequisites

- **Java 21** — Required for Gradle 8.9 and AGP 8.7.0
- **Android SDK** — API 36 (compileSdk), API 33 (minSdk)
- **CMake 3.31.6+** — Install via Android Studio SDK Manager
- **NDK** — 30.0.14904198 (bundled with Android SDK)
- **Git LFS** — For model file storage (`brew install git-lfs` on macOS)

## Initial Setup

### 1. Clone the Repository

```bash
git clone <repo-url>
cd gym_buddy
git lfs install  # Only needed once per machine
git lfs pull      # Download model files
```

### 2. Llama.cpp Source

The app uses llama.cpp for on-device LLM inference. You need to clone llama.cpp and create a symlink:

```bash
# Clone llama.cpp (if not already done)
git clone https://github.com/ggerganov/llama.cpp.git ~/llama.cpp

# Create symlink in project root
ln -s ~/llama.cpp ./llama.cpp
```

**Why symlink?** The CMake build references `../../../../../../llama` from `app/lib/src/main/cpp/CMakeLists.txt`. This points to the llama.cpp source tree at the workspace root.

### 3. Model Files

Model files are stored in `app/src/main/assets/` and tracked via Git LFS:

- `gemma-3-270m-it-Q4_K_M.gguf` — Gemma 3 270M Q4_K_M quantization (~241MB)

These are automatically downloaded when you run `git lfs pull`.

### 4. Build

```bash
./gradlew assembleDebug
```

The build will compile llama.cpp from the symlinked source and generate CPU-specific kernels for:
- `arm64-v8a` (Android devices)
- `x86_64` (Android emulator)

## Build Configuration

| Component | Version |
|-----------|---------|
| AGP | 8.7.0 |
| Gradle | 8.9 |
| Kotlin | 2.0.21 |
| KSP | 2.0.21-1.0.28 |
| minSdk | 33 |
| compileSdk | 36 |
| Java | 21 |

## Troubleshooting

### KSP "unexpected jvm signature V"

If you encounter `java.lang.IllegalStateException: unexpected jvm signature V` during build, add this to `gradle.properties`:

```
ksp.allow.jvm.signature=V
```

This is a known compatibility workaround for KSP with Java 21.

### Llama.cpp Not Found

Ensure the symlink exists and points to valid llama.cpp source:

```bash
ls -la llama.cpp  # Should show: llama.cpp -> /Users/<you>/llama.cpp
```

### Model File Missing

If `git lfs pull` doesn't download the model, manually track and pull:

```bash
git lfs track "*.gguf"
git lfs pull
```

## Architecture

The LLM integration uses llama.cpp's `InferenceEngine` API:

- `LlmService.kt` — High-level service wrapping InferenceEngine
- `app/lib/` — Android library module with C++ JNI bindings to llama.cpp
- Model loaded from `assets/gemma-3-270m-it-Q4_K_M.gguf` at runtime
